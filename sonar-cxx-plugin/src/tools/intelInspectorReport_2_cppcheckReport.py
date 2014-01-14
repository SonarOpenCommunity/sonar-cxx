import os
import sys
from os.path import join

_FILE_LINES = []

# Intel Rules Definition, should be inline with Sonar profile. If not, it will add rule to
_ERROR_CATEGORIES = [
  'cross-thread stack access',
  'data race',
  'deadlock',
  'gdi resource leak',
  'incorrect memcpy call',
  'invalid deallocation',
  'invalid memory access',
  'invalid partial memory access',
  'kernel resource leak',
  'lock hierarchy violation',
  'memory growth',
  'memory leak',
  'memory not deallocated',
  'mismatched allocation/deallocation',
  'missing allocation',
  'thread exit information',
  'thread start information',
  'unhandled application exception',
  'uninitialized memory access',
  'uninitialized partial memory access'
  ]

_ERROR_CATEGORIES_KEYS = [
  'CrossThreadStackAccess',
  'DataRace',
  'DeadLock',
  'GDIResourceLeak',
  'IncorrectMemcpyCall',
  'InvalidDeallocation',
  'InvalidMemoryAccess',
  'InvalidPartialMemoryAccess',
  'KernelResourceLeak',
  'LockHierarchyViolation',
  'MemoryGrowth',
  'MemoryLeak',
  'MemoryNotDeallocated',
  'MismatchedAllocation/Deallocation',
  'MissingAllocation',
  'ThreadExitInformation',
  'ThreadStartInformation',
  'UnhandledApplicationException',
  'UninitializedMemoryAccess',
  'UninitializedPartialMemoryAccess'
  ]

def usage():
    return 'Usage: %s <IntelCsvFile> <OutputFile> <RootFolder> <Executable>' % sys.argv[0]

if len(sys.argv) != 5:
    print usage()
    exit()

absfilepath = os.path.abspath(sys.argv[1])
fcpp = open(os.path.abspath(absfilepath), "r")
_FILE_LINES = fcpp.readlines()
fcpp.close()

filenamew = sys.argv[2]
filetowrite = open(filenamew, 'w')
filetowrite.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
filetowrite.write("<results>\n")

root_workspace = sys.argv[3]
executable = sys.argv[4]

index = 0
for linei in _FILE_LINES:
    if index == 0:
        index += 1
        continue

    elems = linei.split(",")

    if len(elems) < 5:
        continue

    source = elems[0].replace("\"", "")

    file_name = os.path.basename(source)

    # check if file exist on disk, if it does not exist than no need to check this
    if os.path.exists(source):
        found = 1
        for root, dirs, files in os.walk(root_workspace):
            if found == 0:
                break

            for file in files:
                filewithroot = root.lower().replace("\\", "/") + "/" + file.lower()
                sourcelower = source.lower().replace("\\", "/")
                if sourcelower == filewithroot:
                    source = "." + root.replace(root_workspace, "").replace("\\", "/") + "/" + file
                    found = 0
                    break


    line = int(elems[1].replace("\"", ""), 0)

    # we are likely in presence of adress instead of line
    if(line > 1000000):
        line = 0

    severity = elems[2].replace("\"", "")
    id = elems[3].replace("\"", "") + "." + elems[4].replace("\"", "")
    type = elems[5].replace("\"", "").strip()
    description = elems[6].replace("\"", "")
    function = elems[7].replace("\"", "")
    module =elems[8].replace("\"", "")
    msg = "[" + executable + "] " + type + " : " + description

    if type.lower() in _ERROR_CATEGORIES:
        index = _ERROR_CATEGORIES.index(type.lower())
        linestr = ('\t<error file=\"%s\" line=\"%i\" id=\"intelXe.%s\" severity=\"%s\" msg=\"%s\"/>\n' % (source, line, _ERROR_CATEGORIES_KEYS[index], severity, msg))
    else:
        msg = "Rule Not Found In Sonar Profile: " + msg
        linestr = ('\t<error file=\"%s\" line=\"%i\" id=\"intelXe.%s\" severity=\"%s\" msg=\"%s\"/>\n' % (source, line, severity, severity, msg))

    filetowrite.write(linestr)
    index += 1

filetowrite.write("</results>\n")
filetowrite.close()
