# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public
# License along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02

import os
import sys

_FILE_LINES = []
_RULES = []

def UpdateAndGetRuleNumber(rulecheck):
    cnt = 0
    for rule in _RULES:
        if rulecheck in rule:
            cnt += 1
        
    nameofrule = rulecheck + "-" + str(cnt)
    _RULES.append(nameofrule)
    
    return nameofrule
        
def WriteUpdateFile(filename):
    global _FILE_LINES
    filetowrite = open(filename, 'w')   
    for linei in _FILE_LINES:                   
        if linei.startswith('        if category in _ERROR_CATEGORIES:'):
            filetowrite.write('        for value in _ERROR_CATEGORIES:\n')
            filetowrite.write('          if category.startswith(value):\n')
            filetowrite.write('            category = value\n')
        if linei.startswith('  return (linenum in _error_suppressions.get(category, set()) or'):
            filetowrite.write('  for key in _error_suppressions.keys():\n')
            filetowrite.write('    if key and category.startswith(key):\n')
            filetowrite.write('      category = key\n')
        filetowrite.write(linei)
                                                        
    filetowrite.close()

def CheckIfCommentIsValid(line, linenumber):
             
    index = line.find("#")    
    if index == -1:
        return -1
        
    if line.find("'") == -1:       
        return index
            
    indexcnt = index
    found = 0
    while indexcnt > 0 and found == 0:
        indexcnt = indexcnt - 1         
        if line[indexcnt] == "'":
            found = 1

    if found == 0:      
        return index

    found = 0
    indexcnt = index
    while indexcnt < len(line) and found == 0:         
        if line[indexcnt] == "'":
            found = 1
        indexcnt = indexcnt + 1

    if found == 0:  
        return index

    return -1
        
       
def GetDefinition(line_index, strip=0):
    methodcomplete = 0                    
    allmethod = ''
    linecnt = 0
    
    # initial line
    linereplace = _FILE_LINES[line_index + linecnt].rstrip().lstrip()     
     
    while methodcomplete == 0:
        
        # we need to clear the comments from the end of the line
        line = _FILE_LINES[line_index + linecnt].rstrip().lstrip()

        indexcomment = CheckIfCommentIsValid(line, line_index + linecnt)
        if indexcomment > 0:
            line = line[0:indexcomment - 1]
                        
        allmethod += line 

        if allmethod[len(allmethod) - 1] == ")":
            methodcomplete = 1
        
        # clear the line
        if linecnt > 0:           
            _FILE_LINES[line_index + linecnt] = ""
                                                  
        linecnt += 1
        
    elems = allmethod.split(",")    
    if "-" not in elems[2]:
        # get rule and update
        elemsfinal = elems[2].split("'")
        newmethod = UpdateAndGetRuleNumber(elemsfinal[1])
        allmethod = allmethod.replace(elemsfinal[1], newmethod)
    
    _FILE_LINES[line_index] = _FILE_LINES[line_index].replace(linereplace, allmethod)    
    return (allmethod, linecnt)

def usage():
    return 'Usage: %s <cpplint.py> .... txt file can be obtain by running python cpplint.py --filter=' % sys.argv[0]

if len(sys.argv) != 2:
    print usage()
    exit()

# open and parse cpplint parse file
# ## read file into memory for fixing
absfilepath = os.path.abspath(sys.argv[1])
# load file into memory
fcpp = open(os.path.abspath(absfilepath), "r")
_FILE_LINES = fcpp.readlines()
fcpp.close()

# open file for writing
filenamew = "cpplint.xml"        
filetowrite = open(filenamew, 'w')
filetowrite.write("<?xml version=\"1.0\" encoding=\"ASCII\"?>\n")         
filetowrite.write("<rules>\n")

index = 0
for linei in _FILE_LINES:
    if "error(filename, " in linei or "Error(filename, " in linei and "def Error(filename, " not in linei:
        (method, linecnt) = GetDefinition(index)
        method = method.replace("error(", "")
        method = method.replace("Error(", "")
        method = method[:-1]       
        # update rule in file
        elemsofrule = method.split(",")
        incc = 0
        i = 0
        for i, c in enumerate(method):
            if "," == c:
                incc = incc + 1            
                if incc == 4:
                    description = method[i + 1:len(method)]

        key = elemsofrule[2].lstrip().rstrip()
        key = key[1:len(key) - 1]
        descr = description.lstrip().rstrip()
        descr = descr.replace("'", " ")
        descr = descr.replace("&", "&amp;")
        descr = descr.replace(">", "&gt;")
        descr = descr.replace("<", "&lt;")      
        
        name = descr                   
        if len(descr) > 200:
            name = descr[0:200]

        elems = key.split("/")
        category = elems[0]
                                
        filetowrite.write("    <rule key=\"" + key + "\">\n")
        filetowrite.write("        <name><![CDATA[" + name + "]]></name>\n")
        filetowrite.write("        <configKey><![CDATA[" + key + "@CPP_LINT]]></configKey>\n")
        filetowrite.write("        <category name=\"" + category + "\" />\n")
        filetowrite.write("        <description><![CDATA[ " + descr + " ]]></description>\n")
        filetowrite.write("    </rule>\n") 
      
    index += 1
    
filetowrite.write("</rules>\n")
        
filetowrite.close()

# now update the file  with the new rules
WriteUpdateFile("cpplint_mod.py")
