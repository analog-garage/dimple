%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function failed = testDimple(varargin)
    % 
    % testDimple: top level dimple test script. 
    %   
    %   ==Running== 
    %
    %   >> testDimple
    %   
    %   OR
    %
    %   >> testDimple({'log', 1})
    %
    %   OR
    %
    %   >> testDimple({'one_algo', 'SumProduct'})
    %
    %   OR
    %
    %   >> testDimple({'csolver',1})
    %       
    %   ==Installing==
    %
    %   - Install Matlab
    %   - Checkout svn://svn/lyric/trunk/dimple/current
    %   - Copy this:    /Volumes/lyric/installs/MATLAB_code/matlab_xunit_2.0.1
    %          to:      .../MATLAB/matlab_xunit_2.0.1
    %     (Copy it to your MATLAB dir)
    %   - in Matlab's startup.m add the following, fixing up paths
    %     for local differences as appropriate:
    %
    %       ---------------------FOR STARTUP.m------------------------
    %       
    %       %NOTE: update following to be correct on your machine
    %       default_dimple_start_path = '~/Documents/Workspace/dimple';
    %
    %       setenv('_Dimple_START_PATH', default_dimple_start_path);
    %
    %       addpath([getenv('_Dimple_START_PATH') '/solvers/cpp'])
    %       addpath([getenv('_Dimple_START_PATH') '/modelers/matlab'])
    %       addpath([getenv('_Dimple_START_PATH') '/modelers/matlab/core'])
    %       addpath([getenv('_Dimple_START_PATH') '/modelers/matlab/lib'])
    %       addpath([getenv('_Dimple_START_PATH') '/modelers/matlab/tests']);
    %       addpath([getenv('_Dimple_START_PATH')
    %       '/modelers/matlab/tests/util']);
    %
    %       addpath([pwd '/matlab_xunit_2.0.1/matlab_xunit/xunit']);
    %      
    %       %NOTE: update following to be correct on your machine
    %       javaaddpath('/Volumes/LyricStore/Workspace/dimplesolvers/bin/');
    %        
    %       ---------------------FOR STARTUP.m------------------------
    %   
    %   
    %  ==Adding tests==
    %
    %  Unit test framework is 'mtest' or now matlab_xunit
    %
    %  Start here:
    %           .../MATLAB/matlab_xunit_2.0.1/matlab_xunit/ReadMe.html
    %           OR
    %           http://www.mathworks.com/matlabcentral/fx_files/22846/1/content/doc/index.html
    %  
    %  Basically:
    %   - you add a file called 'testXXX.m' to the test directory
    %       and the framework calls it. 
    %   - The framework provides things like
    %       assertEqual, assertAlmostEqual etc....
    %   - You can use test classes or multiple functions per file. 
    %   - See help above for more info.
    % 
    %
    %  To core:  
    %       Add test file to /tests/core. 
    %       This test will now be called for every algorithm/solver pair. 
    %   
    %
    %  For one algorithm:
    %       Add test file to /tests/algoAlgoName dir
    %   
    % 
    %  To add an aglorithm:
    %       Copy /tests/algoDummy
    %       Change 'Dummy' to name of your algorithm. 
    %       Modify getSolvers.m to return the solver(s) you want to use. 
    %       Modify testDummy.m to name of your test, and add test
    %
    %  What if not all solvers support all features?
    %       Hack it, at least for now. 
    %       For exmaple, C++ Max Product solver supports nested graphs, 
    %           but Java Max Product solver does not. 
    %           For the moment, we hacked this by having those tests
    %           check 'if strcmp(class(CSolver), class(getSolver())'
    %           before running netsted tests. 
    
    
    clc;
    
    dtrace(1, '======================================================================');
    dtrace(1, '++testDimple\n');
    
    % Parsing function options.
    parser = TrivialOptions();
    
    % Add options to parser.
    parser.addFlag('csolver');
    parser.addFlag('exit');
    parser.addFlag('core_only');
    parser.addFlag('log');
    parser.addFlag('verbose');      % Synonymous with 'log'
    parser.addOption('one_algo');

    % Parse options and get struct.
    options = parser.parse(varargin);
    bLog          = options.log || options.verbose;
    one_algo      = options.one_algo;
    core_only     = options.core_only;
    bStoreAndExit = options.exit;
    test_csolver  = options.csolver;
    
    global DIMPLE_TEST_VERBOSE;
    DIMPLE_TEST_VERBOSE = bLog;
    
    if ~isempty(varargin)
        dtrace(1, '%u args:', length(varargin));

        for idx = 1:length(varargin)
            arg = sprintf('%s', char(varargin{idx}));
            arg_class  = class(varargin{idx});
            dtrace(1, '\t%u: [%s], arg class [%s]', idx, arg, arg_class);
        end

        dtrace(1, '\nparsed params:');
        disp(options);
        dtrace(1, '\n');
    end
    
    initial_dir = pwd;
    initial_solver = getSolver();
    
    if test_csolver
       disp('building CSolver...');
       makereturn; 
    end
    
    dimple_matlab_start_dir = getenv('_Dimple_START_PATH');
    dimple_test_dir = [dimple_matlab_start_dir '/tests'];
    dimple_test_dirs = [{dimple_test_dir} getDimpleTestDir()];
    core_dir = [dimple_test_dir '/core'];

    if ~isempty(one_algo)
        one_algo_str = sprintf('%s', one_algo);
        %algo_dir = [dimple_test_dir '/algo' one_algo_str];
        found = false;
        for idx = 1:length(dimple_test_dirs)
           test_dir = dimple_test_dirs{idx}; 
           nm = [test_dir '/algo' one_algo_str];
           if exist(nm)
               found = true;
               break;
           end
        end

        if ~found
            error('could not find algorithm in any of the test directories');
        end

        algo_dir = nm;
        
        
        start_dir = pwd();
        [passed, failed, ran, faults] = test_algorithm(core_dir, algo_dir, test_csolver,bLog);
        cd(start_dir);
    elseif core_only
        [passed, failed, ran, faults] = test_core_only(core_dir, bLog);
    else
        dtrace(bLog, 'Starting tests from [%s]\n', dimple_test_dir);     

        [passed, failed, ran, faults] = test_algorithms(dimple_test_dirs, core_dir, test_csolver,bLog);
    end
    
    cd(initial_dir);
    setSolver(initial_solver);
    dtrace(1, '\n\n**********************************************************************');
    if (failed == 0)
        dtrace(1, 'PASSED ALL TESTS');
    else
        dtrace(1, 'FAILED TESTS');
    end
    dtrace(1, '%d of %d tests passed, %d failed', passed, ran, failed);
    dtrace(1, '**********************************************************************');

    failureFileName = 'testDimple.failures.txt';
    if exist(fullfile(pwd, failureFileName), 'file')
        delete(failureFileName);
    end

    if failed ~= 0
        writeFaultsToFile(passed, failed, ran, faults, failureFileName);
    end
    
    doneFile = fopen('testDimpleexitDone.txt', 'w');
    fprintf(doneFile, 'testDimple complete at %s\n', datestr(now, 0, 'local'));
    fclose(doneFile);
    
    dtrace(1, '\n--testDimple');
    dtrace(1, '======================================================================');
    
    if bStoreAndExit        
        exit
    end
    
end
    
function [passed, failed, ran, faults] = testDir(dirName)
	cd(dirName);
	suite = TestSuite.fromPwd();
	logger = CommandWindowTestRunDisplayLyric;
	suite.run(logger);
	ran = logger.TestCaseCount;
	failed = logger.NumFailures + logger.NumErrors;
	passed = ran - failed;
    faults = logger.getFaults();
end
    
function [passed, failed, ran, faults] = test_core_only(core_dir, bLog)
    solver = getSolver();
    dtrace(bLog, '\n++++++++++++++++++++++++++++++++++++++++++++++++');
    dtrace(bLog, 'Testing solver [%s]', char(class(solver)));
    setSolver(solver);
    
    dtrace(bLog, 'Core tests in dir [%s]', core_dir);
    [passed, failed, ran, faults] = testDir(core_dir);

    dtrace(bLog, '------------------------------------------------\n');    
end

function [passed, failed, ran, faults] = test_solver(core_dir, algo_dir, solver, bLog)
    dtrace(bLog, '\n++++++++++++++++++++++++++++++++++++++++++++++++');
    dtrace(bLog, 'Testing solver [%s]', char(class(solver)));
    setSolver(solver);

    dtrace(bLog, 'Core tests in dir [%s]', core_dir);
    [passed, failed, ran, faults] = testDir(core_dir);

    dtrace(bLog, '\nAlgorithm tests in dir [%s]', algo_dir);
    [passed2, failed2, ran2, faults2] = testDir(algo_dir);

    passed = passed + passed2;
    failed = failed + failed2;
    ran = ran + ran2;
    faults = [faults faults2];

    dtrace(bLog, '%d of %d tests passed, %d failed\n', passed, ran, failed);
    dtrace(bLog, '------------------------------------------------\n');
end

function algo_dirs = get_algorithm_dirs(parent_dir, bLog)
    %dtrace(bLog, '++get_algorithm_dirs')
    cd(parent_dir);
    contents = dir(pwd());
    algo_dirs = {};
    for idx = 1 : length(contents)
        if contents(idx).isdir == 1
            dir_name = contents(idx).name;
            prefix = 'algo';
            algo_offset = strfind(dir_name, prefix);
            is_dummy = strcmp(dir_name, 'algoDummy');
            if numel(algo_offset) == 1 && algo_offset == 1 && is_dummy == 0
                full_dir = [char(parent_dir), char('/'), char(dir_name)];
                algo_dirs = appendCell(algo_dirs, full_dir);
            end
        end
    end
    %dtrace(bLog, '--get_algorithm_dirs')
end

function [passed, failed, ran, faults] = test_algorithm(core_dir, algo_dir, test_csolver, bLog)

    
    dtrace(bLog, '\n++test_algorithm~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~');
    cd(algo_dir);
    solvers = getSolvers();
    dtrace(bLog, '%u Solvers in dir [%s]:', length(solvers), algo_dir);
    for idx = 1 : length(solvers)
        solver = solvers{idx};
        class_str = char(class(solver));
        dtrace(bLog, '\t%s', class_str);
    end    
    passed = 0;
    failed = 0;
    ran = 0;
    faults = [];
    %dtrace(bLog, pwd);
    %dtrace(bLog, solvers);
    for idx = 1 : length(solvers)
        solver = solvers{idx};

        if ~test_csolver && isequal(class(solver),'CSolver')
            dtrace(bLog,'...skipping CSolver...');
        else

            
            [passed1, failed1, ran1, faults1] = test_solver(core_dir, algo_dir, solver, bLog);

            passed = passed + passed1;
            failed = failed + failed1;
            ran = ran + ran1;
            faults = [faults faults1];
        end
    end
    dtrace(bLog, '%d of %d tests passed, %d failed\n', passed, ran, failed);
    dtrace(bLog, '--test_algorithm~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~');
end

function [passed, failed, ran, faults] = test_algorithms(parent_dirs, core_dir, test_csolver, bLog)
    passed = 0;
    failed = 0;
    ran = 0;
    faults = [];

    for parent_dir_idx = 1:length(parent_dirs)
        parent_dir = parent_dirs{parent_dir_idx};
        dtrace(bLog, '++test_algorithms');
        dtrace(bLog, 'Top test directory [%s]\n', parent_dir);
        start_dir = pwd();
        algo_dirs = get_algorithm_dirs(parent_dir, bLog);
        if bLog
            dtrace(bLog, '%u algo_dirs:', length(algo_dirs));
            for idx = 1:length(algo_dirs)
                dtrace(bLog, '\t%u:\t%s', idx, algo_dirs{idx});
            end
        end
        for idx = 1 : length(algo_dirs)
            algo_dir = char(algo_dirs(idx));

            [passed1, failed1, ran1, faults1] = test_algorithm(core_dir, algo_dir, test_csolver, bLog);

            passed = passed + passed1;
            failed = failed + failed1;
            ran = ran + ran1;
            faults = [faults faults1];
        end
        cd(start_dir());

        dtrace(bLog, '%d of %d tests passed, %d failed\n', passed, ran, failed);
        dtrace(bLog, '--test_algorithms');
    end
end

function new_stack = filterStack(stack)
    mtest_directory = fileparts(which('runtests'));
    last_keeper = numel(stack);
    have_left_mtest_directory = false;
    for k = 1:numel(stack)
        directory = fileparts(stack(k).file);
        if have_left_mtest_directory
            if strcmp(directory, mtest_directory)
                % Stack trace has reentered mtest directory.
                last_keeper = k - 1;
                break;
            end
        else
            if ~strcmp(directory, mtest_directory)
                have_left_mtest_directory = true;
            end
        end
    end

    new_stack = stack(1:last_keeper);
            
end


function writeStackToFile(stack, file)
for k = 1:numel(stack)
    filename = stack(k).file;
    linenumber = stack(k).line;
    href = sprintf('matlab: opentoline(''%s'',%d)', filename, linenumber);
    fprintf(file, '%s at <a href="%s">line %d</a>\n', filename, href, linenumber);
end
end

function writeFaultsToFile(passed, failed, ran, faults, failureFileName)
    failureFile = fopen(failureFileName, 'w');
    fprintf(failureFile, 'testDimple had errors. %d of %d tests passed, %d failed\n', passed, ran, failed);
    
    for k = 1:numel(faults)
        faultData = faults(k);
        if strcmp(faultData.Type, 'failure')
            str = 'Failure';
        else
            str = 'Error';
        end
        fprintf(failureFile, '\n===== Test Case %s =====\nLocation: %s\nName:     %s\n\n', str, ...
            faultData.TestCase.Location, faultData.TestCase.Name);
        newStack = filterStack(faultData.Exception.stack);
        writeStackToFile(newStack, failureFile);
        fprintf(failureFile, '\n%s\n', faultData.Exception.message);

        fprintf(failureFile, '\n');
    end
    fclose(failureFile);
end    
