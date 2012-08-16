################################################################################
#   Copyright 2012 Analog Devices Inc.
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

def a2x(outputDir,type,fileName):
    #TODO: make sure a2x exists
    p = os.getcwd()
    shutil.copyfile(fileName,outputDir + "/" + fileName)
    os.chdir(outputDir)
    os.system("a2x -f " + type + " " + fileName)
    os.chdir(p)

def a2xpdf(fileName,outputDir):
    a2x(outputDir,"pdf",fileName + ".txt")
    shutil.copyfile(outputDir + "/" + fileName + ".pdf",fileName + ".pdf")

def a2xhtml(fileName,outputDir):
    a2x(outputDir,"xhtml",fileName + ".txt")

def asciidochtml(fileName,outputDir,singlePage=False):
    p = os.getcwd()
    shutil.copyfile(fileName + ".txt",outputDir + "/" + fileName+".txt")
    os.chdir(outputDir)
    if singlePage:
        os.system('asciidoc -a data-uri ' + fileName + '.txt')
    else:
        os.system('asciidoc -b xhtml11 ' + fileName + '.txt')
    os.chdir(p)

def producedoc(fileName,option):
    if option.html_doc:
        if option.pretty_doc:
            a2xhtml(fileName,option.output_dir)
        else:
            asciidochtml(fileName,option.output_dir,option.single_page)
    if option.pdf:
        a2xpdf(fileName,option.output_dir)

if __name__ == "__main__":
    
    parser = optparse.OptionParser()
    parser.add_option("-u","--user_doc",action="store_true",default=False,dest="user_doc")
    parser.add_option("-n","--quick_doc",action="store_true",default=False,dest="pretty_doc")
    parser.add_option("-d","--devel_doc",action="store_true",default=False,dest="devel_doc")
    parser.add_option("-x","--html_doc",action="store_true",default=False,dest="html_doc")
    parser.add_option("-p","--pdf",action="store_true",default=False,dest="pdf")
    parser.add_option("-f","--user_doc_filename",dest="user_doc_filename",default="DimpleUserDocumentation")
    parser.add_option("-r","--devel_doc_filename",dest="devel_doc_filename",default="DimpleDeveloperDocumentation")
    parser.add_option("-o","--output_dir",dest="output_dir",default="output")
    parser.add_option("-c","--clean",dest="clean",default=False,action="store_true")
    parser.add_option("-s","--single_page",dest="single_page",default=False,action="store_true")
                      
    (option,args) = parser.parse_args()

    if option.clean:
        if os.path.isdir(option.output_dir):
            shutil.rmtree(option.output_dir)
        for n in glob.glob("*.pdf"):
            os.remove(n)
 
    if option.user_doc or option.devel_doc:
        if not os.path.isdir(option.output_dir):
            os.mkdir(option.output_dir)
        if os.path.isdir(option.output_dir + "/images"):
            shutil.rmtree(option.output_dir + "/images")
        shutil.copytree("images",option.output_dir + "/images")
                     
    #Create user doc
    if option.user_doc:
        producedoc(option.user_doc_filename,option)
    if option.devel_doc:
        producedoc(option.devel_doc_filename,option)
