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

'''
Created on Nov 16, 2010

@author: schweitz
'''
import sys
import optparse 
import time
import os
import traceback
import re
import platform

global out


def stringlog(logval, logname):
    s = '++++++++++++++++++++%s log+++++++++++++++++++++++\n' % logname
    s += logval
    s += '\n--------------------%s log-----------------------\n' % logname
    return s

def oneantcmd(argout, phase, cmd, log, printlog):
    ok = False
    global out
    
    argout['phase'] = phase
    argout['phases tried'] += ' ' + argout['phase']
    argout[phase]['antcmd'] = cmd
    print('ant command: [%s]' % argout[phase]['antcmd'])
    
    print ("starting [%s] at %s" % (argout['phase'], time.asctime(time.localtime())))
    # On Ubuntu and maybe RedHat, /bin/sh is a weird shell we don't want, so force bash
    if platform.system() == 'Linux':
        finalCmdString = 'bash -c "' + argout[phase]['antcmd'] + '"'
    else:
        finalCmdString = argout[phase]['antcmd']

    print "running: " + finalCmdString
    antret = os.system(finalCmdString)
    print ("finished [%s] at %s" % (argout['phase'], time.asctime(time.localtime())))
    
    antlog = open(log, 'r')
    logval = antlog.read()
    antfailure = re.search('BUILD FAILED', logval, re.MULTILINE);

    argout[phase]['antlog'] = logval         
    
    err = ''
    if antfailure:
        ok = False
        err = '\nbeeERROR: %s failed.\n' % argout['phase']
        err += stringlog(logval, 'ant')
        err += '\nbeeERROR: %s failed.\n' % argout['phase']
        print(err)
    else:
        ok = True
        print('%s succeeded' % argout['phase'])
        if printlog:
            print(stringlog(logval, 'ant'))
    
    argout[phase]['error'] = err
    if not ok:
        argout['error'] += argout[phase]['error']

    return ok

def ant(opt, argout, suppress):    
    ok = True
    logpath = os.path.relpath(opt.log)

    pipe_suffix = ' | tee dimple.ant.txt'

    if opt.clean:
        phase = 'gradle clean'
        argout[phase] = {}

        antcmd = "gradle clean " + pipe_suffix
        ok = oneantcmd(argout, phase, antcmd, logpath, opt.printlog)
    
    if ok:
        phase = 'build'
        argout[phase] = {}
        antcmd = "gradle" + pipe_suffix
        ok = oneantcmd(argout, phase, antcmd, logpath, opt.printlog)
    
    if ok and (opt.junit or opt.testall):
        phase = 'gradle test'
        argout[phase] = {}
        antcmd = "gradle test" + pipe_suffix
        ok = oneantcmd(argout, phase, antcmd, logpath, opt.printlog)
        

    out['phase'] = phase
    return ok
    

def matlab(opt, argout, suppress):
    ok = True
    
    
    argout['phase'] = 'matlab'
    argout['phases tried'] += ' ' + argout['phase']
    argout['matlab'] = {}
    argout['matlab']['error'] = ''
    wait = ''
    if os.name == 'nt':
        wait = '-wait'
    
    matlabFlagFile = os.path.abspath('testDimpleexitDone.txt')
    if os.path.exists(matlabFlagFile):
        os.remove(matlabFlagFile)
    matlabLogName = 'dimple.matlab.txt'
    matlabcmd = "matlab -nodesktop -nosplash %s -r testDimpleexit -logfile %s %s" % (wait, matlabLogName, suppress)
    print('matlab command: [%s]' % matlabcmd)
    print ("starting matlab at %s" % time.asctime(time.localtime()))
    if platform.system() == 'Linux':
        finalCmdString = 'bash -c "' + matlabcmd + '"'
    else:
        finalCmdString = matlabcmd
    matlabret = os.system(finalCmdString)
    print ("finished matlab at %s" % time.asctime(time.localtime()))

    os.system('stty echo')

    if matlabret != 0:
        ok = False
        argout['matlab']['error'] += '\nbeeERROR: matlab process failed with %d\n' % matlabret

    matlabFailureFileName = 'testDimple.failures.txt'
    matlabFailureText = ''
    if os.path.exists(matlabFailureFileName):
        ok = False
        matlabFailureFile = open(matlabFailureFileName, 'r')
        matlabFailureText = matlabFailureFile.read()
        argout['matlab']['error'] += '\nbeeERROR: testDimple reported a failure\n'
        argout['matlab']['error'] += stringlog(matlabFailureText, 'testDimple failure')
        argout['matlab']['error'] += '\nbeeERROR: testDimple reported a failure\n'
    
    doneFileFound = os.path.exists(matlabFlagFile) 
    if not doneFileFound: 
        ok = False
        argout['matlab']['error'] += '\nbeeERROR: testDimple never wrote its \'done\' file\n'
    
    matlabLog = open(matlabLogName, 'r')
    matlabLogVal = matlabLog.read()
    matlabLogVal = stringlog(matlabLogVal, 'testDimple')
    
    if ok:
        print('testDimple succeeded')
        if opt.printlog:
            print(matlabLogVal)
    else:
       print(argout['matlab']['error'])
       argout['error'] += argout['matlab']['error']
   
    
    argout['matlab']['matlabcmd'] = matlabcmd
    argout['matlab']['doneFileFound'] = doneFileFound
    argout['matlab']['matlabret'] = matlabret
    argout['matlab']['matlabFailureText'] = matlabFailureText 
    argout['matlab']['matlabLogVal'] = matlabLogVal
    
    return ok

def update(opt, argout):
    ok = True

    argout['phase'] = 'update'
    argout['phases tried'] += ' ' + argout['phase']
    argout['update'] = {}
    argout['update']['error'] = '' 
    
    oldDir = os.getcwd()
    os.chdir(opt.updatedir)

    print ("starting git pull at %s" % time.asctime(time.localtime()))
    
    from subprocess import Popen, PIPE
    cmd = ['git', 'pull']
    
    p = Popen(cmd, stdout=PIPE, stderr=PIPE)
    stdout, stderr = p.communicate()
    ok = (p.returncode == 0)
    
    print ("finished git pull at %s" % time.asctime(time.localtime()))
    argout['update']['rev'] = stdout  + stderr
    print argout['update']['rev']
    
    os.chdir(oldDir)
    
    if not ok:
        argout['error'] += argout['update']['error']        
    
    return ok

def oneResult(key, value, tabs = 1):
    tabstr = '\t' * tabs
    s = ''
    s += tabstr+ '---------------------------\n'
    s += tabstr + '[%s] = ' % str(key)
    if isinstance(value, dict):
        s += '\n'
        for subkey, subvalue in value.iteritems():
            s += oneResult(subkey, subvalue, tabs + 1)
    elif isinstance(value, list):
        s += '\n'
        for subvalue in value:
            s += tabstr + '\t[%s]\n' % str(subvalue)
    else:
        s += '[%s]\n' % str(value)
    return s
    
def results(startDir, sargs, opt, argout):
    summary = 'bee: '
    if argout['ok']:
        summary += '[Success]'
    else:
        summary += '[FAILURE] in [%s]' % argout['phase']
        if len(opt.whichbee):
            summary += '-[%s]' % opt.whichbee
        
    s = ''
    try:
        s += '\n=============================================================\n'
        s += summary
        s += '\n\nat %s' % time.asctime(time.localtime())
        s += '\n\nfrom %s' % os.path.abspath(startDir)
        s += '\n\ncomment: %s' % opt.comment
        s += '\n=============================================================\n'
        
        if not argout['ok']:
            s += '\n=============================================================\n'
            s += 'Error string: [\n' 
            s += argout['error']
            s += '\n]\n'
            s += '\n=============================================================\n'
        
        s += '\n=============================================================\n'
        s += sargs
        s += '\n=============================================================\n'

        s += '\n=============================================================\n'
        s += 'raw results map\n'
        for key, value in argout.iteritems():
            s += oneResult(key, value)
        s += '\n=============================================================\n'

        pollen = open('bee.out.txt', 'w')
        pollen.write(s)
        pollen.close()
    except Exception, e:
        s += "\n\nEXCEPTION: printing results[\n%s\n]\n" % str(e)
        print(s)
        traceback.print_exc()
        print("\n")
    except:
        s += "\nUNKNOWN EXCEPTION printing results\n"
        print(s)
        traceback.print_exc()
        print("\n")
    return (summary, s)

def email_results(opt, argout, summary, results):
    try:
        import nightlybuildtools.tmail as tmail
        tmail.trivial_email(serverURL = 'who.lyricsemi.hdq', 
                                  sender = opt.sender,
                                  to = opt.recipient, 
                                  subject = summary, 
                                  content = results, 
                                  attachment_file_name='',
                                  monospaced = True)
        print ("finished email at %s" % time.asctime(time.localtime()))
    except Exception, e:
        s = "\n\nEXCEPTION: emailing results[\n%s\n]\n" % str(e)
        print(s)
        traceback.print_exc()
        print("\n")
    except:
        s = "\nUNKNOWN EXCEPTION emailing results\n"
        print(s)
        traceback.print_exc()
        print("\n")
 
def getparser():
    usage =     "\n\n" 
    usage = usage + "Syntax:\n"
    usage = usage + "\tpython bee.py [optional stuff]\n\n"
    usage = usage + "Examples:\n\n"
    
    usage = usage + "\tpython bee.py -c\n"
    usage = usage + "\tapprox 'ant clean'\n\n"
    usage = usage + "\tpython bee.py -j\n"
    usage = usage + "\tjunit tests - approx 'ant test'\n\n"
    
    usage = usage + "\tpython bee.py -t\n"
    usage = usage + "\tmatlab and junit tests'\n\n"

    usage = usage + "\tpython bee.py -m '-v'\n"
    usage = usage + "\tpasses '-v' flag to ant\n\n"

    parser = optparse.OptionParser(usage,version="%prog 1.0")
    parser.add_option("-c", "--clean", action="store_true", dest="clean", help="passes \'clean'\ target to ant", default=False)
    parser.add_option("-j", "--junit", action="store_true", dest="junit", help="passes \'test'\ target to ant", default=False)
    parser.add_option("-t", "--testall", action="store_true", dest="testall", help="passes \'test'\ target to ant and runs matlab tests", default=False)
    parser.add_option("-p", "--printlog", action="store_true", dest="printlog", help="whether to print ant, matlab logs to screen even on success", default=False)
    parser.add_option("-u", "--update", action="store_true", dest="update", help="whether to svn update before run", default=False)
    parser.add_option("-a", "--auto", action="store_true", dest="auto", help="equivalent to -u -c -t -s --kit-test", default=False)
    parser.add_option("-v", "--verbose",action="store_true", dest="verbose", help="does not suppress output",default=False)
    
    parser.add_option("-s", "--sendall", action="store_true", dest="sendall", help="whether email all results", default=False)
    parser.add_option("-e", "--senderrors", action="store_true", dest="senderrors", help="whether email only error results", default=False)
    parser.add_option("", "--wtf", action="store_true", dest="wtf", help="hey dumb developers please fix me", default=False)
    parser.add_option("-x", "--extraantargs", type="string", dest="extraantargs", help="more command line to options pass to ant. ", default="")
    parser.add_option("-l", "--log", type="string", dest="log", help="ant log file", default="dimple.ant.txt")
    parser.add_option("-d", "--sender", type="string", dest="sender", help="source address for email", default="dimple.build.phantom.account@lyricsemi.com")
    parser.add_option("-r", "--recipient", type="string", dest="recipient", help="who gets results emailed to them", default="sw@groups.lyricsemiconductor.com")
    parser.add_option("-m", "--comment", type="string", dest="comment", help="comment added to build output", default="No comment")
    parser.add_option("-i", "--updatedir", type="string", dest="updatedir", help="where we cd to to update", default="../..")    
    parser.add_option("-k", "--kitdir", type="string", dest="kitdir", help="where we cd to build kit",default="../../build")
    parser.add_option("-w", "--whichbee", type="string", dest="whichbee", help="for calling multiple bees", default="")
    parser.add_option("", "--kit", action="store_true", dest="kit", help="build the Dimple kit", default=False)
    parser.add_option("", "--kit-backup", action = "store", help = "backup the kit zip file on completion", metavar = "PATH", type = "string")
    parser.add_option("", "--kit-backup-salt", action = "store", help = "time format string to be inserted before extension", metavar = "FORMAT", type = "string")
    parser.add_option("", "--kit-developer", action="store_true", dest="kit_developer", help="build the Dimple developer kit", default=False)
    parser.add_option("", "--kit-test", action="store_true", dest="kit_test", help="unzip and test", default=False)
    parser.add_option("", "--kit-unzip", action="store_true", dest="kit_unzip", help="unzip", default=False)
    parser.add_option("-n", "--no-build", action="store_false",dest="build",help="run build")
    parser.add_option("-b", "--build", action="store_false",dest="build",help="run build",default=True)
    
    
    return parser

def build_kit(opt, argout):
    '''
    Build the Dimple kit.
    '''
    argout['phase'] = 'kit'
    argout['phases tried'] += ' ' + argout['phase']
    argout['kit'] = {}
    argout['kit']['command'] = 'python build.py'

    curdir = os.getcwd();
    os.chdir(opt.kitdir)
    argout['kit']['build-dir'] = os.getcwd();
    
    if opt.kit_developer:
        argout['kit']['command'] += ' --developer'
    if opt.kit_test:
        argout['kit']['command'] += ' --test'
    if opt.kit_unzip:
        argout['kit']['command'] += ' --unzip'
    if opt.kit_backup:
        argout['kit']['command'] += ' --backup "' + opt.kit_backup + '"'
    if opt.kit_backup_salt:
        argout['kit']['command'] += ' --backup-salt "' + opt.kit_backup_salt + '"'
    
    os.system(argout['kit']['command'])
    os.chdir(curdir)
    
    return True

def bee(args):
    '''run ant and testDimple...
\talthough ant can call external programs, its flow-control tools are very hacky.
\tso to do stuff outside of ant, we use python. 
    '''
    print ("\n################## %s ##################" % os.path.basename(__file__))
    print ("start at %s\n" % time.asctime(time.localtime()))
    print ("args: %s\n" % args)
    
    parser = getparser()
    
    (opt, args_left_after_parse) = parser.parse_args(args)
    global out
    
    out = {'ok' : True,
           'phase' : '',
           'phases tried' : '',
           'error' : ''}
    startDir = os.path.abspath(os.getcwd())
    
    # Change to the directory containing bee.py
    os.chdir(sys.path[0])
        
    try:
        if opt.auto:
            opt.update = True
            opt.clean = True
            opt.testall = True
            opt.sendall = True
            opt.kit_test = True
        
        if opt.testall:
            if not opt.clean:
                print('forcing clean on to ensure matlab tests are happy')
                opt.clean = True
        
        # If we were passed any kit option, we build the kit.
        if opt.kit_developer or opt.kit_test or opt.kit_unzip or opt.kit_backup or opt.kit_backup_salt:
            opt.kit = True
    
        sargs = ''
        print("\n===============================================")
        
        if len(args_left_after_parse):
            sargs += "Remaining unparsed args:\n"
            sargs += "\t%s\n\n" % args_left_after_parse
    
        sargs += "Parsed args:\n"
        for name in sorted(opt.__dict__.keys()):
            sargs += "%30s:\t{%s}\n" % (name, getattr(opt, name))
    
        print("===============================================\n")
        
        print(sargs)

        if opt.update:
            out['ok'] = update(opt, out)
            
            
        suppress = ''

        if out['ok'] and opt.build:
            out['ok'] = ant(opt, out, suppress)
        
        if out['ok'] and opt.testall:
            out['ok'] = matlab(opt, out, suppress)        
            out['phase'] = 'matlab'
            
        if out['ok'] and opt.kit:
            out['ok'] = build_kit(opt, out)
            out['phase'] = 'kit'
            
    except Exception, e:
        out['ok'] = False
        s = "\n\nEXCEPTION: [\n%s\n]\n" % str(e)
        print(s)
        out['error'] += s
        out['exception'] = (s, e)  
        traceback.print_exc()
        print("\n")
    except:
        out['ok'] = False
        s = "\nUNKNOWN EXCEPTION\n"
        print(s)
        out['error'] += s
        out['exception'] = (s, None)  
        traceback.print_exc()
        print("\n")

    os.chdir(startDir)
    #out['phase'] = 'results'
    out['phases tried'] += ' ' + out['phase']
    summary, s = results(startDir, sargs, opt, out)    
    
    if opt.sendall or (not out['ok'] and (opt.senderrors or opt.wtf)):
        if opt.wtf:
            summary = summary + '[WTF]'
        email_results(opt, out, summary, s)
               
    print ("\ndone at %s" % time.asctime(time.localtime()))
    print ("################## %s done #############\n" % os.path.basename(__file__))
    return (out, summary, s)
    
if __name__ == '__main__':
    bee(sys.argv)
