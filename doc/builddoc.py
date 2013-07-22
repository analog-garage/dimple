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

class cd:
    def __init__(self, newPath):
        self.newPath = newPath

    def __enter__(self):
        self.savedPath = os.getcwd()
        os.chdir(self.newPath)

    def __exit__(self, etype, value, traceback):
        os.chdir(self.savedPath)

def producedoc(fileName,option):
    #pdflatex "\def\forjava{}\input{DimpleUserManual.tex}"
    #TODO: add java user doc as an option
    os.system('pdflatex ' + fileName)
    os.system('pdflatex ' + fileName)
    os.system('pdflatex ' + fileName)

if __name__ == "__main__":
    
    parser = optparse.OptionParser()
    parser.add_option("-u","--user_doc",action="store_true",default=False,dest="user_doc")
    parser.add_option("-d","--devel_doc",action="store_true",default=False,dest="devel_doc")
    parser.add_option("-f","--user_doc_filename",dest="user_doc_filename",default="DimpleUserManual")
    parser.add_option("-F","--user_doc_dir",dest="user_doc_dir",default="DimpleUserManual")
    parser.add_option("-r","--devel_doc_filename",dest="devel_doc_filename",default="DimpleDeveloperDocumentation")
    parser.add_option("-R","--devel_doc_dir",dest="devel_doc_dir",default="DimpleDeveloperDocumentation")
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

    # user_doc  devel_doc options
    #    F          F              build both unless clean
    #    F          T              build devel_doc
    #    T          F              build user_doc
    #    T          T              build both

    if not option.clean:
        build_user_doc = True
        build_devel_doc = True
    else:
        build_user_doc = False
        build_devel_doc = False

    if option.user_doc and not option.devel_doc:
        build_user_doc = True
        build_devel_doc = False

    if option.devel_doc and not option.user_doc:
        build_user_doc = False
        build_devel_doc = True        

    if option.devel_doc and option.user_doc:
        build_user_doc = True
        build_devel_doc = True        

 
    # Create user doc
    if build_user_doc:
        with cd(option.user_doc_dir):
            producedoc(option.user_doc_filename,option)
        shutil.copyfile(option.user_doc_dir + '/' + option.user_doc_filename + ".pdf",
                        option.user_doc_filename + ".pdf")
    # Create developer doc
    if build_devel_doc:
        with cd(option.devel_doc_dir):
            producedoc(option.devel_doc_filename,option)
        shutil.copyfile(option.devel_doc_dir + '/' + option.devel_doc_filename + ".pdf",
                        option.devel_doc_filename + ".pdf")
