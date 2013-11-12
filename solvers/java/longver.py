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

import shutil
import os
import subprocess
f = file('../../VERSION')
version = f.readlines()[0].strip()
branch = subprocess.Popen('git rev-parse --abbrev-ref HEAD'.split(),stdout=subprocess.PIPE).communicate()[0].strip()
date = subprocess.Popen('git log -n 1 --format=format:%ci'.split(),stdout=subprocess.PIPE).communicate()[0].strip()
f.close()

f = file('src/main/resources/VERSION','w')
s = version + ' ' + branch + ' ' + date
h = hash(s)
nouns = file('nouns.txt','r').readlines()
adjs = file('adj.txt','r').readlines()
advs = file('adv.txt','r').readlines()
s +=     ' "' + advs[h % len(advs)].strip().capitalize() + \
         ' ' + adjs[h % len(adjs)].strip().capitalize() + \
         ' ' + nouns[h % len(nouns)].strip() + '"'
f.write(s)
f.close()
shutil.copy('src/main/resources/VERSION','../../LONG_VERSION')

