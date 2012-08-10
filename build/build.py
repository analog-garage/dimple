import sys
from buildutils import *

if __name__ == "__main__":
    builder = Builder()
    versionFile = '../VERSION'
    javaBuildDir = '../solvers/java'
    javaTargetDir = '../solvers/lib'
    progName = 'dimple'
    copyRoot = '..'
    pFileToCheck = '/modelers/matlab/core/FactorGraph'
    buildCommand = 'gradle'
    builder.build(sys.argv,__file__,versionFile,javaBuildDir,javaTargetDir,progName,copyRoot,pFileToCheck,buildCommand)
