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

def producedoc(fileName,option):
    os.system('pdflatex ' + fileName)

if __name__ == "__main__":
    
    parser = optparse.OptionParser()
    parser.add_option("-u","--user_doc",action="store_true",default=False,dest="user_doc")
    parser.add_option("-d","--devel_doc",action="store_true",default=False,dest="devel_doc")
    parser.add_option("-f","--user_doc_filename",dest="user_doc_filename",default="DimpleUserDocumentation")
    parser.add_option("-r","--devel_doc_filename",dest="devel_doc_filename",default="DimpleDeveloperDocumentation")
    parser.add_option("-c","--clean",dest="clean",default=False,action="store_true")
                      
    (option,args) = parser.parse_args()

    if option.clean:
        for n in glob.glob("*.pdf"):
            os.remove(n)
 
                     
    #Create user doc
    if option.user_doc:
        producedoc(option.user_doc_filename,option)
    if option.devel_doc:
        producedoc(option.devel_doc_filename,option)
