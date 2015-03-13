################################################################################
#   Copyright 2012 Analog Devices, Inc.
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
import shutil
import glob
import sys

class cd:
    def __init__(self, newPath):
        self.newPath = newPath

    def __enter__(self):
        self.savedPath = os.getcwd()
        os.chdir(self.newPath)

    def __exit__(self, etype, value, traceback):
        os.chdir(self.savedPath)

def producedoc(fileName,option):
    #pdflatex "\def\version{0.04}\input{\def\formatlab{}\input{DimpleUserManual.tex}}"
    os.system('pdflatex ' + fileName)
    os.system('pdflatex ' + fileName)
    os.system('pdflatex ' + fileName)

def produceUserDoc(fileName,flag,versionString):
    #forjava or formatlab
    command = 'pdflatex "\def\\version{' + versionString + '}\input{\def\\' + flag + '{}\input{' + fileName + '}}"'
    print command
    os.system(command)
    os.system(command)
    os.system(command)


def extractVersionString():
    f = file('../VERSION','r')
    version = f.readlines()[0].strip()
    return version

if __name__ == "__main__":
    
    parser = optparse.OptionParser()
    parser.add_option("-u","--user_doc",action="store_true",default=False,dest="user_doc")
    parser.add_option("-j","--java_user_doc",action="store_true",default=False,dest="java_user_doc")
    parser.add_option("-m","--matlab_user_doc",action="store_true",default=False,dest="matlab_user_doc")
    parser.add_option("-f","--user_doc_filename",dest="user_doc_filename",default="DimpleUserManual")
    parser.add_option("-F","--user_doc_dir",dest="user_doc_dir",default="DimpleUserManual")
    parser.add_option("-c","--clean",dest="clean",default=False,action="store_true")
                      
    (option,args) = parser.parse_args()

    if option.clean:
        for n in glob.glob("*.pdf"):
            os.remove(n)
        for n in glob.glob("*/*.pdf"):
            os.remove(n)
        for n in glob.glob("*/*.aux"):
            os.remove(n)
        for n in glob.glob("*/*.dvi"):
            os.remove(n)
        for n in glob.glob("*/*.toc"):
            os.remove(n)
        for n in glob.glob("*/*.log"):
            os.remove(n)
        for n in glob.glob("*/*.out"):
            os.remove(n)

    if len(sys.argv) == 1:
        build_java_user_doc = True
        build_matlab_user_doc = True
    else:
        build_java_user_doc = False
        build_matlab_user_doc = False

    if option.user_doc:
        build_java_user_doc = True
        build_matlab_user_doc = True
    if option.java_user_doc:
        build_java_user_doc = True
    if option.matlab_user_doc:
        build_matlab_user_doc = True

    versionString = extractVersionString()
 
    # Create user doc
    if build_java_user_doc:
        with cd(option.user_doc_dir):
            produceUserDoc(option.user_doc_filename,'forjava',versionString)
        shutil.copyfile(option.user_doc_dir + '/' + option.user_doc_filename + ".pdf",
                        option.user_doc_filename + "_v" + versionString + "_Java_API.pdf")

    if build_matlab_user_doc:
        with cd(option.user_doc_dir):
            produceUserDoc(option.user_doc_filename,'formatlab',versionString)
        shutil.copyfile(option.user_doc_dir + '/' + option.user_doc_filename + ".pdf",
                        option.user_doc_filename + "_v" + versionString + "_MATLAB_API.pdf")
