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

function [nErrors nWarnings nFailures nSkips] = testDimpleDemos(varargin)
% testDimpleDemos - Dimple Demos Execution Test
%
%   Signature:
%       [e w f s] = testDimpleDemos(flags & options)
%
%   Usage:
%       Can be called with several flags and options.
%
%       Flags:
%           exit
%               Exit matlab after test, passing the sum of the result of
%               this function as the return code.
%           output-all
%               Output from demos, errors, and warnings will be printed to
%               the console.
%           output-demos
%               Output from demos will be printed to the console.
%           output-errors
%               Captured errors will be printed to the console.
%           output-warnings
%               If a demo has warnings, the last warning generated will be
%               printed to the console.
%           silent
%               Disables console output. Overrides all output- flags.
%
%       Options:
%           demos {DEMO[, ...]}
%           demos [DEMO_INDICES]
%               Runs the demos provided in the cell array. For example,
%                   >> testDimpleDemos('demos', {'00_XorAndNot'});
%               would cause ONLY the demo named '00_XorAndNot' to be
%               tested. Additionally you can specify indices of demos to
%               run. For example,
%                   >> testDimpleDemos('demos', [0:5]);
%               would run demos #0 through #5. (Note: This is somewhat
%               dependent upon how the OS sorts files.)
%           exclude {DEMO[, ...]}
%           exclude [DEMO_INDICES]
%               Causes demos named in the provided cell array to be
%               skipped. For example,
%                   >> testDimpleDemos('exclude', {'00_XorAndNot'});
%               would cause the 0th demo to always be skipped.
%               Additionally you can specify indices of demos to skip. For
%               example,
%                   >> testDimpleDemos('exclude', [0:5]);
%               would skip demos #00 through #05. (Note: This is somewhat
%               dependent upon how the OS sorts files.)
%   Returns:
%       The number of demos with fatal errors [nErrors], warnings
%       [nWarnings], invalid output [nFailures], as well as those that had
%       to be skipped due other factors, e.g. missing tool boxes, [nSkips].
%
%   Note: Demos that fail to set the Dimple_DEMO_RESULT global (as described
%   below) will be assumed to pass if they have no errors or warnings.

%--------------------------------------------------------------------------

% Global Result
%
% Demos which adhere to the Dimple_DEMO_RESULT standard provide a set of
% standardized result codes which indicate how the demo performed. These
% codes in summary are:
%  -1 - Could not be run and was SKIPPED.
%   0 - Ran successfully without warnings or errors and produced verifiably
%       correct outputs; a SUCCESS.
%   1 - Ran without warning or error, however, failed to produce correct
%       outputs; a FAILURE.
global Dimple_DEMO_RESULT;

% Capture Start Time
timeStart = tic;

% Option Parsing
parser = TrivialOptions();

% Add Flags & Options
parser.addFlag('exit');
parser.addFlag('output-all');
parser.addFlag('output-demos');
parser.addFlag('output-errors');
parser.addFlag('output-warnings');
parser.addFlag('silent');
parser.addOption('demos');
parser.addOption('exclude');

% Parse Options
options = parser.parse(varargin);

% Set Output Flag
options.output = options.output_demos || options.output_demos || ...
    options.output_errors || options.output_warnings;

% If the output-all flag was passed, enable all outputs.
if options.output_all
    options.output_demos = true;
    options.output_errors = true;
    options.output_warnings = true;
end

% Determine Verbosity
verbosity = 1 - options.silent;

% Output Status
dtrace(verbosity, '================================================================================');
dtrace(verbosity, 'Dimple Demos Execution Test\n');
dtrace(verbosity, ' %d Arguments Passed\n', numel(varargin));
dtrace(verbosity, ' %d Options\n', numel(fieldnames(options)));
dtrace(verbosity, evalc('disp(options);'));

% Get ./demos & ./ directories.
demos_dir = fullfile(getenv('_Dimple_START_PATH'), 'demo');
start_dir = pwd;

% Output Status
dtrace(verbosity, ' Looking for demos in "%s".\n', demos_dir); 
dtrace(verbosity && ~options.output, ' Running Demos');

% Counting Variables
nDemos    = 0;
nErrors   = 0;
nFailures = 0;
nSkips    = 0;
nWarnings = 0;

% Get struct array of all directories in demos directory.
if ~isempty(options.demos) && iscell(options.demos)
    demos = struct('name', options.demos, 'isdir', true);
else
    demos = dir(demos_dir);
end

% Allocate Results Struct
results = struct( ...
    'demos',    {cell(numel(demos), 1)}, ...
    'errors',   {cell(numel(demos), 1)}, ...
    'failures', {cell(numel(demos), 1)}, ...
    'skips',    {cell(numel(demos), 1)}, ...
    'warnings', {cell(numel(demos), 1)});

% Loop over all demo directories...
for i = 1:numel(demos)
    % Capture Start Time
    timeStartDemo = tic;
    
    % Get the directory information struct.
    demo = demos(i);

    % If this demo directory is a file or starts with '.', skip it.
    if ~demo.isdir || demo.name(1) == '.'
        continue;
    % If we are only running one demo, skip all others.
    elseif ~isempty(options.demos) && iscell(options.demos) && ~sum(strcmp(demo.name, options.demos))
        continue;
    % If this demo directory has been excluded, skip it.
    elseif ~isempty(options.exclude) && iscell(options.exclude) && sum(strcmp(demo.name, options.exclude))
        continue;
    % Final check for numeric demos specifiers.
    elseif isvector(options.demos) && ~sum(options.demos == nDemos)
        continue;
    % Final check for numeric exclude specifiers.
    elseif isvector(options.exclude) && sum(options.exclude == nDemos)
        continue;
    end
    
    % Count how many demos we've run.
    nDemos = nDemos + 1;
    results.demos{nDemos} = demo.name;

    % Reset the last warning.
    lastwarn('', '');

    % Output Status
    if options.output
        dtrace(verbosity, ...
            '|------------------------------------------------------------------------------|');
        dtrace(verbosity, ...
            '|>> %-74s |', sprintf('%s', demo.name));
        dtrace(verbosity, ...
            '|------------------------------------------------------------------------------|');
    else
        dtrace(verbosity, ...
            ' > %-36s', demo.name, '...');
    end

    % Attempt to run demo.
    try
        % Lookout! Here be nonsense! We have to use evalin() so that the
        % variables created by demos don't mess up variables in this scope.
        % But we also have to use evalc() so that we can hide demo output.
        % The result is an apostrophe fiesta!
        command = regexprep(sprintf([ ...
            'global Dimple_DEMO_RESULT Dimple_TESTING_DEMOS;' ...
            'Dimple_DEMO_RESULT = 0;' ...
            'Dimple_TESTING_DEMOS = 1;' ...
            'close all;' ...
            'cd(fullfile(''%s'', ''%s''));' ...
            'run;' ...
            'cd(''../..'');' ...
            'close all;'], demos_dir, demo.name), '''', '''''');
        output = evalc(['evalin(''base'', ''' command ''')']);
        
        if options.output_demos
            dtrace(verbosity, [' ' regexprep(output, '\n', '\n ')]);
        end
        
        % Handle Demo Result
        if Dimple_DEMO_RESULT == -1
            nSkips = nSkips + 1;
            results.skips{nSkips} = demo.name;
        elseif Dimple_DEMO_RESULT == 1
            nFailures = nFailures + 1;
            results.failures{nFailures} = demo.name;
        end

        % Check if we caught a warning.
        [lastmsg, lastid] = lastwarn;
        if ~strcmp(lastmsg, '') && ~strcmp(lastid, '')
            if options.output_warnings
                dtrace(verbosity, ' Caught %s Warning', lastid);
                dtrace(verbosity, '   %s', regexprep([lastmsg char(10)], '\n', '\n   '));
            end
            nWarnings = nWarnings + 1;
            results.warnings{nWarnings} = demo.name;
        end

        % Output Status
        if options.output
            dtrace(verbosity, ' Finished %-30s%39s', demo.name, sprintf('(Execution time: %.2f seconds)', toc(timeStartDemo)));
        else
            dtrace(verbosity, '%39s', sprintf('(Execution time: %.2f seconds)', toc(timeStartDemo)));
        end
    % Check if we caught an error.
    catch exception
        if ~options.output
            dtrace(verbosity, '%39s', sprintf('(Execution time: %.2f seconds)', toc(timeStartDemo)));
        end
        
        if options.output_errors
            dtrace(verbosity, ' Caught %s Exception', exception.identifier);
            dtrace(verbosity, '   Line #%d in %s\n', exception.stack(end).line, exception.stack(end).file);
            dtrace(verbosity, '   %s', regexprep([exception.message char(10)], '\n', '\n   '));
        end

        if options.output
            dtrace(verbosity, ' Finished %-30s%39s', demo.name, sprintf('(Execution time: %.2f seconds)', toc(timeStartDemo)));
        end
        
        nErrors = nErrors + 1;
        results.errors{nErrors} = demo.name;
    end
end

% Output Status Spacer
dtrace(verbosity && options.output, ...
    '|------------------------------------------------------------------------------|');

% Close all figure windows.
close all;

% Output status.
dtrace(verbosity, '\n Finished running %d demos in %.2f seconds.', nDemos, toc(timeStart));
dtrace(verbosity, '\n   Total Errors: %d', nErrors);
for i = 1:nErrors
    dtrace(verbosity, '     [%s]', results.errors{i});
end
dtrace(verbosity, '\n   Total Warnings: %d', nWarnings);
for i = 1:nWarnings
    dtrace(verbosity, '     [%s]', results.warnings{i});
end
dtrace(verbosity, '\n   Total Failures: %d', nFailures);
for i = 1:nFailures
    dtrace(verbosity, '     [%s]', results.failures{i});
end
dtrace(verbosity, '\n   Total Skips: %d', nSkips);
for i = 1:nSkips
    dtrace(verbosity, '     [%s]', results.skips{i});
end

% Restore current directory.
cd(start_dir);
    
% Clear Testing Flags
clear('Dimple_DEMO_RESULT');
evalin('base', 'clear(''Dimple_TESTING_DEMOS'', ''Dimple_DEMO_RESUL'');');

% Exit if flag was set.
if options.exit
    exit(nErrors + nWarnings + nFailures + nSkips);
end

%--------------------------------------------------------------------------
