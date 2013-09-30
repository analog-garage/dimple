################################################################################
#   Copyright 2012-2013 Analog Devices, Inc.
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#################################################################################

import optparse 
import os
import re
import shutil
import zipfile
import datetime
import time

def make_zip_file(dest):
    print('Making zip file...')
    #make zip file
    fileList = []
    rootdir = dest
    for root, subFolders, files in os.walk(rootdir):
        for f in files:
            fileList.append(os.path.join(root,f))

    name = dest + '.zip'
    if os.path.exists(name):
        os.remove(name)
    z = zipfile.ZipFile(dest + '.zip','w')

    for f in fileList:
        if not os.path.isdir(f):
            z.write(f)

def remove_uncompressed_kit(dest):
    print('Removing uncompressed kit...')
    #remove the kit
    shutil.rmtree(dest)
    
def uncompress_kit(dest):
    print('Uncompressing kit...')
    if os.path.exists(dest):
        shutil.rmtree(dest)

    z = zipfile.ZipFile(dest + '.zip')
    z.extractall('.')

def builddoc():
    #if exists ../doc/builddoc.py
    print "building documentation"
    p = os.getcwd()
    os.chdir(p)
    if os.path.exists('../doc/builddoc.py'):
        os.chdir('../doc')
        os.system('python builddoc.py -c -u')
    os.chdir(p)

class Builder:
    def __init__(self):
        pass

    def build(self,args,scriptName,versionFile,javaBuildDir,javaTargetDir,progName,
              copyRoot,pFileToCheck,buildCommand, buildCommandWithTest):

        parser = optparse.OptionParser(self.usage(scriptName),version="%prog 1.0")
        parser.add_option('-t', '--test', action = 'store_true', help = 'unzip and test', default=False)
        parser.add_option('-u', '--unzip', action = 'store_true', help = 'unzip', default=False)
        parser.add_option('-d', '--developer', action = 'store_true', help="developer", default=True)
	parser.add_option('-r', '--reuse',action='store_true',help="reuse",default=False)
        parser.add_option('-n', '--no_doc',action='store_true',default=False)
        (opt, args_left_after_parse) = parser.parse_args(args)
        
        exclude_file_name = 'files_to_exclude.txt'
        regex_exclude_file_name = 'regex_to_exclude.txt'
        mfiles_to_keep_file_name = 'mfiles_to_keep.txt'

        dest_file_name = progName + '_v' + file(versionFile).readlines()[0].replace('.','_').rstrip()

        self.main(exclude_file_name,regex_exclude_file_name,
             mfiles_to_keep_file_name,
             dest_file_name,
             opt.unzip,
             opt.test,
             opt.developer,
             javaBuildDir,
             javaTargetDir,
             copyRoot,
                  pFileToCheck,
                  buildCommand,
                  buildCommandWithTest,
                  opt.reuse,
                  opt.no_doc)
        

    def main(self,exclude_file_name,
             regex_to_exclude_file_name,
             mfiles_to_keep_file_name,
             dest,
             unzip,
             test,
             developer,
             javaBuildDir,
             javaTargetDir,
             copyRoot,
             pFileToCheck,
             buildCommand,
             buildCommandWithTest,
             reuse,
             no_doc):

            if not no_doc:
                builddoc()

            self.build_java_solver(javaBuildDir,javaTargetDir,buildCommand,buildCommandWithTest,test,reuse)
            
            (fexcludes, 
             rexcludes, 
             mfiles_to_keep) = self.load_exclude_files(
                exclude_file_name,
                regex_to_exclude_file_name,
                mfiles_to_keep_file_name)

            self.make_dest_directory(dest)
            
            self.copy_files_to_destination(fexcludes,rexcludes,dest,copyRoot)
            
            javadocBuildDir = javaBuildDir + '/build/docs/javadoc'
            if not no_doc and os.path.exists(javadocBuildDir):
                javadocDestDir = dest + '/doc/javadoc'
                self.make_dest_directory(javadocDestDir)
                self.copy_files_to_destination([],[],javadocDestDir,javadocBuildDir)

            if not developer:
                self.convert_mfiles_to_pfiles(mfiles_to_keep,dest,pFileToCheck)


            make_zip_file(dest)

            remove_uncompressed_kit(dest)
            

            #optionally uncompress the kit
            if unzip or test:
                uncompress_kit(dest)
                
            #optionally test
            if test:
                self.test_kit(dest)

    

    def usage(self,scriptName):
        usage = 	"\n\n" 
        usage = usage + "Syntax:\n"
        usage = usage + "\tpython " + scriptName + " [options]\n\n"
        usage = usage + "Examples:\n\n"
        usage = usage + "\tBuild release AND unzip it:\n"
        usage = usage + "\tpython " + scriptName + " -u\n\n"
        usage = usage + "\tBuild release, unzip, and test:\n"
        usage = usage + "\tpython " + scriptName + " -t\n\n"
        return usage

    def build_java_solver(self,java_build_dir,target_dir,buildCommand,buildCommandWithTest,test,reuse):
        print('building java...')
        curdir = os.getcwd()
        os.chdir(java_build_dir)

        if not reuse:
            os.system(buildCommand + ' clean')
        
            #Make sure clean succeeded
            if os.path.exists(target_dir):
                raise Exception('clean failed!  Aborting')
        
        if test:
            os.system(buildCommandWithTest)
        else:
            os.system(buildCommand)

        os.chdir(curdir)

        #Make sure java build succeeded
        if not os.path.exists(target_dir):
            raise Exception('failed to build java solver!  Aborting')

    def load_exclude_files(self,exclude_file_name,regex_to_exclude_file_name,mfiles_to_keep_file_name):


        print('Loading exclude files...')
        #load file of excludes
        f = file(exclude_file_name)
        fexcludes = f.readlines()
        fexcludes = set([l.strip() for l in fexcludes])

        #load file of regex to exclude
        f = file(regex_to_exclude_file_name)
        rexcludes = f.readlines()
        rexcludes = [re.compile(l.strip()) for l in rexcludes]

        #load files we don't want to turn into pfiles
        f = file(mfiles_to_keep_file_name)
        mfiles_to_keep = f.readlines()
        mfiles_to_keep = set([l.strip() for l in mfiles_to_keep])
    
  
        return (fexcludes, rexcludes, mfiles_to_keep)


    def copy_files_to_destination(self,fexcludes,rexcludes,dest,copyRoot):
        print('Copying all files...')
        #First copy and exclude
        start = copyRoot
        self.do_copy_recursion(start,fexcludes,rexcludes,dest)


    def do_copy_recursion(self,adir,fexcludes,rexcludes,dest):

        #list directory
        file_names = os.listdir(adir)
    
        #for each file
        for f in file_names:
        
            src = adir + '/' + f
            dest_file = dest + '/' + f

            if self.should_be_excluded(src,fexcludes,rexcludes):
                #print('Excluding: ' + adir + '/' + f)
                pass
            else:
               #print('Including: ' + src)
                isdir = os.path.isdir(src)
        
                if isdir:
                   os.mkdir(dest_file)
                   self.do_copy_recursion(src ,fexcludes,rexcludes,dest_file)
                else:
                    shutil.copyfile(src,dest_file)
        
       


    def should_be_excluded(self,name,fexcludes,rexcludes):
        name = name[3:]
        if name in fexcludes:
            return True

        for r in rexcludes:
            if r.match(name) != None:
                return True

        return False


    def convert_mfiles_to_pfiles(self,mfiles_to_keep,dest,pFileToCheck):
        print('Converting m files to p files...')

        matfile = file('gen_pfiles.m','w')
        #matfile.write("disp('hello');")

        self.gen_pfile_script(dest,'',matfile,mfiles_to_keep)

        matfile.write("quit;");
        #recursively go through dest and build script
        #execute script convert to p files

        matfile.close()

        ntextras = ''
        if os.name == 'nt':
            ntextras = '-wait'
    
        matlab_cmd = 'matlab -nodesktop -nosplash -r "cd %s; gen_pfiles();" %s -logfile %s' \
            % (os.getcwd(), ntextras, 'matlab.log')
        print(matlab_cmd)

        matlab_ret = os.system(matlab_cmd)
    
        os.system('stty echo')
    
        #recursively g through dest and delete m files
        self.delete_mfiles(dest,'',mfiles_to_keep)

        #Check to see this all worked by seeing if a single m file has been turned into a p file
        if pFileToCheck:
            if not os.path.exists(dest + pFileToCheck + '.p'):
                raise Exception('Failed to convert m files to pfiles!  Aborting')
            if os.path.exists(dest + pFileToCheck + '.m'):
                raise Exception('Failed to delete m files after creating pfiles! Aborting')
    
    


    def delete_mfiles(self,root_dir,local_dir,mfiles_to_keep):
        file_names = os.listdir(root_dir + '/' + local_dir)

        for f in file_names:

            if len(local_dir) > 0:
                local_name = local_dir + '/' + f
            else:
                local_name = f
                
            full_name = root_dir + '/' + local_name

            if local_name in mfiles_to_keep:
                pass
            else:
                isdir = os.path.isdir(full_name)
            
                if isdir:
                    self.delete_mfiles(root_dir,local_name,mfiles_to_keep)
                elif full_name.endswith('m'):
                    #script.write('pcode ' + full_name + ' -inplace;\n')
                    os.remove(full_name)


    def gen_pfile_script(self,root_dir,local_dir,script,mfiles_to_keep):
        file_names = os.listdir(root_dir + '/' + local_dir)
    
        for f in file_names:

            if len(local_dir) > 0:
                local_name = local_dir + '/' + f
            else:
                local_name = f
            full_name = root_dir + '/' + local_name


            if local_name in mfiles_to_keep:
                pass
            else:
                isdir = os.path.isdir(full_name)
            
                if isdir:
                    self.gen_pfile_script(root_dir,local_name,script,mfiles_to_keep)
                elif full_name.endswith('m'):
                    script.write('pcode ' + full_name + ' -inplace;\n')
        
    
    def make_dest_directory(self,dest):
        print('Making the destination directory...')
        #Make the dest directory
        if os.path.exists(dest):
            shutil.rmtree(dest)

        os.mkdir(dest)


    def test_kit(self,dest):
        print ('Testing kit...')
        curdir = os.getcwd()
        os.chdir(dest)

        f = file('runtest.m','w')
        f.write('testDimple;')
        f.write('exit;')
        f.close()

        #raise Exception('not supported yet')
        wait = ''
        if os.name == 'nt':
            wait = '-wait'

        suppress = ''
        #suppress = '>& /dev/null'
        matlabLogName = '../dimple.matlab.txt'
        matlabFlagFile = os.path.abspath('testDimpleexitDone.txt')
        if os.path.exists(matlabFlagFile):
            os.remove(matlabFlagFile)

        matlabcmd = "matlab -nodesktop -nosplash %s -r runtest -logfile %s %s" % (wait, matlabLogName, suppress)
        print('matlab command: [%s]' % matlabcmd)
        print ("starting matlab at %s" % time.asctime(time.localtime()))
        matlabret = os.system(matlabcmd)
        print ("finished matlab at %s" % time.asctime(time.localtime()))

        os.system('stty echo')
        
        os.remove(matlabFlagFile)
        os.remove('runtest.m')
        os.chdir(curdir)
    




