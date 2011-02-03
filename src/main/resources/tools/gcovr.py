#! /usr/bin/env python
# 
# A report generator for gcov 3.4
#
# This routine generates a format that is similar to the format generated 
# by the Python coverage.py module.  This code is similar to the 
# data processing performed by lcov's geninfo command.  However, we
# don't worry about parsing the *.gcna files, and backwards compatibility for
# older versions of gcov is not supported.
#
# Copyright (2008) Sandia Corporation. Under the terms of Contract
# DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government
# retains certain rights in this software.
# Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits réservés.
# Author(s) : Franck Bonin, Neticoa SAS France.
# 
# Outstanding issues
#   - verify that gcov 3.4 or newer is being used
#   - verify support for symbolic links
#
#  _________________________________________________________________________
#
#  FAST: Python tools for software testing.
#  Copyright (c) 2008 Sandia Corporation.
#  Copyright (c) 2010 Franck Bonin.
#  This software is distributed under the BSD License.
#  Under the terms of Contract DE-AC04-94AL85000 with Sandia Corporation,
#  the U.S. Government retains certain rights in this software.
#  For more information, see the FAST README.txt file.
#  _________________________________________________________________________
#

import sys
from optparse import OptionParser
import subprocess
import glob
import os
import re
import copy
import xml.dom.minidom

__version__ = "2.0.prerelease"
gcov_cmd = "gcov"

#
# Container object for coverage statistics
#
class CoverageData(object):

    def __init__(self, fname, uncovered, covered, branches, noncode):
        self.fname=fname
        # Shallow copies are cheap & "safe" because the caller will
        # throw away their copies of covered & uncovered after calling
        # us exactly *once*
        self.uncovered = copy.copy(uncovered)
        self.covered   = copy.copy(covered)
        self.noncode   = copy.copy(noncode)
        # But, a deep copy is required here
        self.all_lines = copy.deepcopy(uncovered)
        self.all_lines.update(covered.keys())
        self.branches = copy.deepcopy(branches)

    def update(self, uncovered, covered, branches, noncode):
        self.all_lines.update(uncovered)
        self.all_lines.update(covered.keys())
        self.uncovered.update(uncovered)
        self.noncode.intersection_update(noncode)
        for k in covered.keys():
            self.covered[k] = self.covered.get(k,0) + covered[k]
        for k in branches.keys():
            for b in branches[k]:
                d = self.branches.setdefault(k, {})
                d[b] = d.get(b, 0) + branches[k][b]
        self.uncovered.difference_update(self.covered.keys())

    def uncovered_str(self):
        if options.show_branch:
            # Don't do any aggregation on branch results
            tmp = []
            for line in self.branches.keys():
                for branch in self.branches[line]:
                    if self.branches[line][branch] == 0:
                        tmp.append(line)
                        break

            tmp.sort()
            return ",".join([str(x) for x in tmp]) or ""
        
        tmp = list(self.uncovered)
        if len(tmp) == 0:
            return ""

        tmp.sort()
        first = None
        last = None
        ranges=[]
        for item in tmp:
            #print "HERE",item
            if last is None:
                first=item
                last=item
            elif item == (last+1):
                last=item
            else:
                if len(self.noncode.intersection(range(last+1,item))) \
                       == item - last - 1:
                    last = item
                    continue
                
                if first==last:
                    ranges.append(str(first))
                else:
                    ranges.append(str(first)+"-"+str(last))
                first=item
                last=item
        if first==last:
            ranges.append(str(first))
        else:
            ranges.append(str(first)+"-"+str(last))
        return ",".join(ranges)

    def coverage(self):
        if ( options.show_branch ):
            total = 0
            cover = 0
            for line in self.branches.keys():
                for branch in self.branches[line].keys():
                    total += 1
                    cover += self.branches[line][branch] > 0 and 1 or 0
        else:
            total = len(self.all_lines)
            cover = len(self.covered)
            
        percent = total and str(int(100.0*cover/total)) or "--"
        return (total, cover, percent)

    def summary(self,prefix):
        if prefix is not None:
            if prefix[-1] == os.sep:
                tmp = self.fname[len(prefix):]
            else:
                tmp = self.fname[(len(prefix)+1):]
        else:
            tmp=self.fname
        tmp = tmp.ljust(40)
        if len(tmp) > 40:
            tmp=tmp+"\n"+" "*40

        (total, cover, percent) = self.coverage()
        return ( total, cover,
                 tmp + str(total).rjust(8) + str(cover).rjust(8) + \
                 percent.rjust(6) + "%   " + self.uncovered_str() )


def search_file(expr, path=None, abspath=False, follow_links=False):
    """
    Given a search path, recursively descend to find files that match a
    regular expression.

    Can specify the following options:
       path - The directory that is searched recursively
       executable_extension - This string is used to see if there is an
           implicit extension in the filename
       executable - Test if the file is an executable (default=False)
       isfile - Test if the file is file (default=True)
    """
    ans = []
    pattern = re.compile(expr)
    if path is None or path == ".":
        path = os.getcwd()
    elif os.path.exists(path):
        raise IOError, "Unknown directory '"+path+"'"
    for root, dirs, files in os.walk(path, topdown=True):
        for name in files:
           if pattern.match(name):
                name = os.path.join(root,name)
                if follow_links and os.path.islink(name):
                    ans.append( os.path.abspath(os.readlink(name)) )
                elif abspath:
                    ans.append( os.path.abspath(name) )
                else:
                    ans.append( name )
    return ans


#
# Get the list of datafiles in the directories specified by the user
#
def get_datafiles(flist, options, ext="gcda"):
    allfiles=[]
    for dir in flist:
        if options.verbose:
            print "Scanning directory "+dir+" for "+ext+" files..."
        files = search_file(".*\."+ext, dir, abspath=True, follow_links=True)
        if options.verbose:
            print "Found %d files " % len(files)
        allfiles += files
    return allfiles


def process_gcov_data(file, covdata, options):
    INPUT = open(file,"r")
    #
    # Get the filename
    #
    line = INPUT.readline()
    segments=line.split(":")
    fname = (segments[-1]).strip()
    if fname[0] != os.sep:
        #line = INPUT.readline()
        #segments=line.split(":")
        #fname = os.path.dirname((segments[-1]).strip())+os.sep+fname
        fname = os.path.abspath(fname)
    #
    # Return if the filename does not match the filter
    #
    if options.filter is not None and not options.filter.match(fname):
        if options.verbose:
            print "  Ignoring coverage data for file "+fname
        return
    #
    # Return if the filename matches the exclude pattern
    #
    for i in range(0,len(options.exclude)):
        if options.exclude[i].match(fname):
            if options.verbose:
                print "  Ignoring coverage data for file "+fname
            return
    #
    # Parse each line, and record the lines
    # that are uncovered
    #
    noncode   = set()
    uncovered = set()
    covered   = {}
    branches  = {}
    #first_record=True
    lineno = 0
    for line in INPUT:
        segments=line.split(":")
        tmp = segments[0].strip()
        try:
            lineno = int(segments[1].strip())
        except:
            pass # keep previous line number!
            
        if tmp[0] == '#':
            uncovered.add( lineno )
        elif tmp[0] in "0123456789":
            covered[lineno] = int(segments[0].strip())
        elif tmp[0] == '-':
            # remember certain non-executed lines
            code = segments[2].strip()
            if len(code) == 0 or code == "{" or code == "}" or \
               code.startswith("//") or code == 'else':
                noncode.add( lineno )
        elif tmp.startswith('branch'):
            fields = line.split()
            try:
                count = int(fields[3])
                branches.setdefault(lineno, {})[int(fields[1])] = count
            except:
                # We ignore branches that were "never executed"
                pass
        elif tmp.startswith('call'):
            pass
        elif tmp.startswith('function'):
            pass
        elif tmp[0] == 'f':
            pass
            #if first_record:
                #first_record=False
                #uncovered.add(prev)
            #if prev in uncovered:
                #tokens=re.split('[ \t]+',tmp)
                #if tokens[3] != "0":
                    #uncovered.remove(prev)
            #prev = int(segments[1].strip())
            #first_record=True
        elif options.verbose:
            print "UNKNOWN LINE DATA:",tmp
    #
    # If the file is already in covdata, then we
    # remove lines that are covered here.  Otherwise,
    # initialize covdata
    #
    #print "HERE",fname
    #print "HERE uncovered",uncovered
    #print "HERE   covered",covered
    if not fname in covdata:
        covdata[fname] = CoverageData(fname,uncovered,covered,branches,noncode)
    else:
        #print "HERE B uncovered",covdata[fname].uncovered
        #print "HERE B   covered",covdata[fname].covered
        covdata[fname].update(uncovered,covered,branches,noncode)
        #print "HERE A uncovered",covdata[fname].uncovered
        #print "HERE A   covered",covdata[fname].covered
    INPUT.close()

#
# Process a datafile and run gcov with the corresponding arguments
#
def process_datafile(file, covdata, options):
    #
    # Launch gcov
    #
    (dir,base) = os.path.split(file)
    (name,ext) = os.path.splitext(base)
    prevdir = os.getcwd()
    os.chdir(dir)
    (head, tail) = os.path.split(name)
#    cmd = gcov_cmd + \
#          " --branch-counts --branch-probabilities --preserve-paths " + tail
    cmd = gcov_cmd + \
          " --branch-counts --branch-probabilities " + tail
    output = subprocess.Popen( cmd.split(" "),
                               stdout=subprocess.PIPE ).communicate()[0]
    #output = subprocess.call(cmd)
    #print "HERE",cmd
    #print "X",output
    #
    # Process *.gcov files
    #
    for fname in glob.glob("*.gcov"):
        process_gcov_data(fname, covdata, options)
        if not options.keep:
            os.remove(fname)
    os.chdir(prevdir)
    if options.delete:
        os.remove(file)

#
# Produce the classic gcovr text report
#
def print_text_report(covdata):
    def _num_uncovered(key):
        (total, covered, percent) = covdata[key].coverage()
        return total - covered
    def _percent_uncovered(key):
        (total, covered, percent) = covdata[key].coverage()
        if covered:
            return -1.0*covered/total
        else:
            return total or 1e6
    def _alpha(key):
        return key

    total_lines=0
    total_covered=0
    # Header
    print "-"*78
    a = options.show_branch and "Branch" or "Lines"
    b = options.show_branch and "Taken" or "Exec"
    print "File".ljust(40) + a.rjust(8) + b.rjust(8)+ "  Cover   Missing"
    print "-"*78

    # Data
    keys = covdata.keys()
    keys.sort(key=options.sort_uncovered and _num_uncovered or \
              options.sort_percent and _percent_uncovered or _alpha)
    for key in keys:
        (t, n, txt) = covdata[key].summary(options.root)
        total_lines += t
        total_covered += n
        print txt

    # Footer & summary
    print "-"*78
    percent = total_lines and str(int(100.0*total_covered/total_lines)) or "--"
    print "TOTAL".ljust(40) + str(total_lines).rjust(8) + \
          str(total_covered).rjust(8) + str(percent).rjust(6)+"%"
    print "-"*78

#
# Produce an XML report in the Cobertura format
#
def print_xml_report(covdata):

    if options.root is not None:
        if options.root[-1] == os.sep:
            prefix = len(options.root)
        else:
            prefix = len(options.root) + 1
    else:
        prefix = 0

    impl = xml.dom.minidom.getDOMImplementation()
    docType = impl.createDocumentType(
        "coverage", None,
        "http://cobertura.sourceforge.net/xml/coverage-03.dtd" )
    doc = impl.createDocument(None, "coverage", docType)
    root = doc.documentElement

    if options.root is not None:
        source = doc.createElement("source")
        source.appendChild(doc.createTextNode(options.root))
        sources = doc.createElement("sources")
        sources.appendChild(source)
        root.appendChild(sources)

    packageXml = doc.createElement("packages")
    root.appendChild(packageXml)
    packages = {}

    keys = covdata.keys()
    keys.sort()
    for f in keys:
        data = covdata[f]
        (dir, fname) = os.path.split(f)
        dir = dir[prefix:]
        
        package = packages.setdefault(
            dir, [ doc.createElement("package"), {},
                   0, 0, 0, 0 ] )
        c = doc.createElement("class")
        lines = doc.createElement("lines")
        c.appendChild(lines)

        class_lines = 0
        class_hits = 0
        class_branches = 0
        class_branch_hits = 0
        for line in data.all_lines:
            hits = data.covered.get(line, 0)
            class_lines += 1
            if hits > 0:
                class_hits += 1
            l = doc.createElement("line")
            l.setAttribute("number", str(line))
            l.setAttribute("hits", str(hits))
            branches = data.branches.get(line)
            if branches is None:
                l.setAttribute("branch", "false")
            else:
                b_hits = 0
                for v in branches.values():
                    if v > 0:
                        b_hits += 1
                coverage = 100*b_hits/len(branches)
                l.setAttribute("branch", "true")
                l.setAttribute( "condition-coverage",
                                "%i%% (%i/%i)" %
                                (coverage, b_hits, len(branches)) )
                cond = doc.createElement('condition')
                cond.setAttribute("number", "0")
                cond.setAttribute("type", "jump")
                cond.setAttribute("coverage", "%i%%" % ( coverage ) )
                class_branch_hits += b_hits
                class_branches += float(len(branches))
                conditions = doc.createElement("conditions")
                conditions.appendChild(cond)
                l.appendChild(conditions)
                
            lines.appendChild(l)

        className = fname.replace('.', '_')
        c.setAttribute("name", className)
        c.setAttribute("filename", dir+os.sep+fname)
        c.setAttribute("line-rate", str(class_hits / (1.0*class_lines or 1.0)))
        c.setAttribute( "branch-rate",
                        str(class_branch_hits / (1.0*class_branches or 1.0)) )
        c.setAttribute("complexity", "0.0")

        package[1][className] = c
        package[2] += class_hits
        package[3] += class_lines
        package[4] += class_branch_hits
        package[5] += class_branches

    for packageName, packageData in packages.items():
        package = packageData[0];
        packageXml.appendChild(package)
        classes = doc.createElement("classes")
        package.appendChild(classes)
        classNames = packageData[1].keys()
        classNames.sort()
        for className in classNames:
            classes.appendChild(packageData[1][className])
        package.setAttribute("name", packageName.replace(os.sep, '.'))
        package.setAttribute("line-rate", str(packageData[2]/(1.0*packageData[3] or 1.0)))
        package.setAttribute( "branch-rate", str(packageData[4] / (1.0*packageData[5] or 1.0) ))
        package.setAttribute("complexity", "0.0")


    xmlString = doc.toprettyxml()
    print xmlString
    #xml.dom.ext.PrettyPrint(doc)


##
## MAIN
##

#
# Create option parser
#
parser = OptionParser()
parser.add_option("--version",
        help="Print the version number, then exit",
        action="store_true",
        dest="version",
        default=False)
parser.add_option("-v","--verbose",
        help="Print progress messages",
        action="store_true",
        dest="verbose",
        default=False)
parser.add_option("-o","--output",
        help="Print output to this filename",
        action="store",
        dest="output",
        default=None)
parser.add_option("-k","--keep",
        help="Keep temporary gcov files",
        action="store_true",
        dest="keep",
        default=False)
parser.add_option("-d","--delete",
        help="Delete the coverage files after they are processed",
        action="store_true",
        dest="delete",
        default=False)
parser.add_option("-f","--filter",
        help="Keep only the data files that match this regular expression",
        action="store",
        dest="filter",
        default=None)
parser.add_option("-e","--exclude",
        help="Exclude data files that match this regular expression",
        action="append",
        dest="exclude",
        default=[])
parser.add_option("-r","--root",
        help="Defines the root directory.  This is used to filter the files, and to standardize the output.",
        action="store",
        dest="root",
        default=None)
parser.add_option("-x","--xml",
        help="Generate XML instead of the normal tabular output.",
        action="store_true",
        dest="xml",
        default=None)
parser.add_option("-b","--branches",
        help="Tabulate the branch coverage instead of the line coverage.",
        action="store_true",
        dest="show_branch",
        default=None)
parser.add_option("-u","--sort-uncovered",
        help="Sort entries by increasing number of uncovered lines.",
        action="store_true",
        dest="sort_uncovered",
        default=None)
parser.add_option("-p","--sort-percentage",
        help="Sort entries by decreasing percentage of covered lines.",
        action="store_true",
        dest="sort_percent",
        default=None)
parser.usage="gcovr [options]"
parser.description="A utility to run gcov and generate a simple report that summarizes the coverage"
#
# Process options
#
(options, args) = parser.parse_args(args=sys.argv)
if options.version:
    print "gcovr "+__version__
    print ""
    print "Copyright (2008) Sandia Corporation. Under the terms of Contract "
    print "DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government "
    print "retains certain rights in this software."
    sys.exit(0)
#
# Setup filter
#
for i in range(0,len(options.exclude)):
    options.exclude[i] = re.compile(options.exclude[i])
if options.filter is not None:
    options.filter = re.compile(options.filter)
elif options.root is not None:
    #if options.root[0] != os.sep:
    #    dir=os.getcwd()+os.sep+options.root
    #    dir=os.path.abspath(dir)
    #    options.root=dir
    #else:
    #    options.root=os.path.abspath(options.root)
    options.root=os.path.abspath(options.root)
    #print "HERE",options.root
    options.filter = re.compile(options.root.replace("\\","\\\\"))
#
# Get data files
#
if len(args) == 1:
    datafiles = get_datafiles(["."], options)
else:
    datafiles = get_datafiles(args[1:], options)
#
# Get coverage data
#
covdata = {}
for file in datafiles:
    process_datafile(file,covdata,options)
if options.verbose:
    print "Gathered coveraged data for "+str(len(covdata))+" files"
#
# Print report
#
if options.xml:
    print_xml_report(covdata)
else:
    print_text_report(covdata)
