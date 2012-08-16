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

classdef TrivialOptions < handle
% TrivialOptions - Function variable argument parser.
%
%   Defines argument parser for functions that take a variable number of
%   arguments.
%
%   To use this class to parse function arguments, you must begin by
%   creating an instance of the TrivialOptions class.
% 
%       % Create a parser.
%       trivial = TrivialOptions();
% 
%   Next you'll need to add some 'parameters'. A'command' let you pass
%   arguments to an inner command. A 'flag' is a simple true/false
%   indicators. An 'option' take exactly one value. They are created as
%   follows.
% 
%       % Add a command named 'switch'.
%       trivial.addCommand('switch');
% 
%       % Add a flag named 'version'.
%       trivial.addFlag('version');
% 
%       % Add an option named 'output-path'.
%       trivial.addOption('output-path');
% 
%   It is also possible to associate multiple strings with a particular
%   parameter. This is done simply by passing multiple arguments to a call
%   of addCommand, addFlag, or addOption. For example,
% 
%       % Add multiple strings for the same command.
%       trivial.addCommand('s', 'start', 'init');
% 
%   would add the strings 's', 'start', and 'init' and associate them,
%   equivalently, as the same command.
% 
%   Having added some parameters, you can then parse a cell array of
%   arguments.
% 
%       % Parse the arguments.
%       options = trivial.parse(varargin);
% 
%   The return value (assigned to 'options') is a struct where the fields
%   correspond to the parameters. You thus can access the values of the
%   arguments:
% 
%       % Check if the version flag was set.
%       show_version = options.version;
% 
%       % Get the output path.
%       output_path = options.output_path;
% 
%   NOTE: The dash in the name was converted to an underscore. Dashes are
%   the only character which are automatically converted. All other invalid
%   characters will cause errors when parse() is called.
% 
%   NOTE: If a parameter has multiple strings associated with it (see
%   example above) then the field name in the options struct for that
%   command will match the first string that is longer than one character
%   in length. If no string is longer than one character, the first string
%   that was passed to create the parameter is used.
% 
%   You can thus pass arguments to functions by passing in strings that
%   correspond to flags and string/value pairs for options.
% 
%       % Example of passing the 'version' flag to a function.
%       my_function('version');
% 
%       % Example of passing the 'output-path' option to a function.
%       my_function('output-path', '/path/to/output');
% 
%   NOTE: You can also use MATLAB's alternative function syntax for a more
%   natural execution of flags and options.
% 
%       % Alternative example of passing the version flag.
%       my_function version;
% 
%       % Alternative example of passing the output-path option.
%       my_function output-path /path/to/output;
% 
% TrivialOptions Methods:
%   addCommand - Add a command
%   addFlag    - Add a flag
%   addOption  - Add an option
%   parse      - Parse options

    properties(Access = private)
        commands = {};
        % Cell array of command strings.
        
        flags = {};
        % Cell array of flag strings.
        
        options = {};
        % Cell array of option strings.
    end
    
    methods(Access = public)
        function obj = TrivialOptions()
        % Public Constructor
        end
        
        function addCommand(obj, varargin)
        % addCommand - Add a valid command. A command is followed by zero
        % or more options for that command. All arguments which follow the
        % command are returned in a cell array as the value of the command.
        %
        %   Usage:
        %       To begin using commands, first you must add one to your
        %       parser. For example,
        %
        %           % Add a simple switch command.
        %           trivial.addCommand('switch');
        %
        %       would add the command 'switch' to the parser.
        %
        %       If a function (my_function) is defined as follows, consider
        %       the parsed value of the 'switch' command.
        %
        %           % Sample function which implements a 'switch' command.
        %           function result = my_function(varargin)
        %               trivial = TrivialOptions();
        %               trivial.addFlag('help');
        %               trivial.addCommand('switch');
        %               options = trivial.parse(varargin);
        %               result = options.switch;
        %           end
        %
        %           % Collect sample results.
        %           a = my_function('switch');
        %           b = my_function('switch', 'Dev1');
        %           c = my_function('switch', 'Dev1', 'Dev2');
        %           d = my_function('switch', 'Dev1', 'help');
        %
        %       The results, a-e, can be produced identically as follows:
        %
        %           % Produce idential sample results.
        %           a = {'switch'};
        %           b = {'switch', 'Dev1'};
        %           c = {'switch', 'Dev1', 'Dev2'};
        %           d = {'switch', 'Dev1', 'help'};
        %
        %       NOTE: The first element of the value array will always be
        %       the name of the command. This is so one can use isempty()
        %       to determine if a command was called or not.
        %
        %       NOTE: Even though help is a valid flag, it was taken as
        %       an argument for switch. All arguments encountered after a
        %       command are taken; no attempt to parse them is made.
        %
        %       NOTE: A command must be the first argument passed if it is
        %       to be parsed correctly.
        
            obj.addParameter('commands', varargin);
        end
        
        function addFlag(obj, varargin)
        % addFlag - Add a valid flag. A flag should not be followed by a
        % value. The presence of the flag string in the arguments will
        % correspond to a value of true.
        %
        %   Usage:
        %       % Add a simple help flag.
        %       trivial.addFlag('h', 'help');
        %
        %       % A function that uses this parser would then expect either
        %       % of the following.
        %       my_function('h');
        %       my_function('help');
        
            obj.addParameter('flags', varargin);
        end
        
        function addOption(obj, varargin)
        % addOption - Add a valid option. An option must be followed by a
        % single value.
        %
        %   Signature:
        %       obj.addOption(identifier[, ...]);
        %
        %   Usage:
        %       % Add a simple help option.
        %       trivial.addOption('m', 'mode');
        %
        %       % A function that uses this parser would then expect either
        %       % of the following.
        %       my_function('m', value);
        %       my_function('mode', value);
        
            obj.addParameter('options', varargin);
        end
        
        function [parameters] = parse(obj, arguments)
        % parse - Parse options.
        %
        %   Signature:
        %       options = obj.parse(arguments);
        %
        %   Usage:
        %       % Accepts cell array of options - you can pass the standard
        %       % varargin array which is automatically created by MATLAB.

            % Initialize output options struct.
            parameters = struct();
            
            % Keep track of how many times we've parsed each option.
            numbers = struct();
            
            % Set all commands to empty cell arrays.
            for parameter = [obj.commands{:}]
                primary_string = obj.getPrimaryString(parameter);
                parameters.(primary_string) = {};
            end
            
            % Set all flags to false.
            for parameter = [obj.flags{:}]
                primary_string = obj.getPrimaryString(parameter);
                parameters.(primary_string) = false;
            end
            
            % Set all options to empty cell arrays.
            for parameter = [obj.options{:}]
                primary_string = obj.getPrimaryString(parameter);
                parameters.(primary_string) = {};
                numbers.(primary_string) = 0;
            end

            argumentNo = 1;
            while (argumentNo <= length(arguments))
                % For convenience, get the curret argument.
                argument = arguments{argumentNo};
                
                % Check if the current argument is a command.
                if argumentNo == 1 && obj.isParameter(argument, 'commands')
                    % Create value array.
                    parameters.(obj.getPrimaryString(argument)) = ...
                        arguments(argumentNo:end);
                    
                    % Break out of the loop because nothing can come after
                    % a command.
                    break;
                
                % Check if current argument is an option.
                elseif obj.isParameter(argument, 'options')
                    if length(arguments) < argumentNo + 1
                        error('TrivialOptions:MissingValue', ...
                            'Option "%s" must be followed by a value.', argument);
                    end
                    
                    % Get the primary parameter string.
                    primary_string = obj.getPrimaryString(argument);
                    
                    % If the option hasn't been parsed before, replace the
                    % value with the passed value. If it has been parsed
                    % before, append the passed value.
                    if numbers.(primary_string) == 0
                        parameters.(primary_string) = ...
                            arguments{argumentNo + 1};
                    else
                        if numbers.(primary_string) == 1
                            parameters.(primary_string) = ...
                                {parameters.(primary_string)};
                        end
                        
                        parameters.(primary_string){end + 1} = ...
                            arguments{argumentNo + 1};
                    end
                    
                    % Increment the number of times this parameter has been
                    % parsed. Increment the argument index.
                    numbers.(primary_string) = ...
                        numbers.(primary_string) + 1;
                    argumentNo = argumentNo + 1;
                    
                % Check if current argument is a flag.
                elseif obj.isParameter(argument, 'flags')
                    parameters.(obj.getPrimaryString(argument)) = true;
                    
                % If neither an option nor a flag, we throw an error.
                elseif ischar(argument)
                    error('TrivialOptions:InvalidParameter', ...
                        'Invalid parameter "%s" in position #%d.', argument, argumentNo);
                else
                    error('TrivialOptions:UnexpectedValue', ...
                        'Unexpected value encountered in position #%d.', argumentNo);
                end
                
                argumentNo = argumentNo + 1;
            end
        end
    end
    
    methods(Access = private)
        function addParameter(obj, type, strings)
        % General argument addition.
        
            % Check that we got at least one identifier.
            if isempty(strings)
                error('TrivialOptions:MissingParameterStrings', ...
                    'You must specify at least one parameter string.');
            % Check that our identifiers are all strings.
            elseif ~iscellstr(strings)
                error('TrivialOptions:InvalidParameterString', ...
                    'Parameter strings must be character arrays.');
            end
            
            % If no other identifiers have been added, skip ahead.
            if ~isempty(obj.(type))
                % Check that for each new identifier being added, no
                % identical identifier has been added previously.
                for string = strings
                    if obj.isParameter(char(string))
                        error('TrivialOptions:DuplicateParameterString', ...
                            'The parameter string "%s" has already been added.', char(string));
                    end
                end
            end
            
            % Add the identifiers.
            obj.(type){end + 1} = strings;
        end
        
        function primary_string = getPrimaryString(obj, parameter_string)
        % Get the primary parameter string for a parameter.
        
            % Search for the primary string.
            for parameter_strings = [obj.commands obj.flags obj.options]
                if sum(strcmp(parameter_string, parameter_strings{:}))
                    for string = parameter_strings{:}
                        if length(char(string)) > 1
                            primary_string = strrep(char(string), '-', '_');
                            return;
                        end
                    end
                    primary_string = char(parameter_strings{:}{1});
                    return;
                end
            end
            
            % If we couldn't find the primary string, throw an error.
            error('TrivialOptions:UnknownParameter', ...
                'Parameter with identifier "%s" must be added before the primary string can be determined.', parameter_string);
        end
        
        function result = isParameter(obj, parameter_string, type)
        % Check if the parameter string exists.
        
            % Search both flags and options if no type is specified.
            if nargin < 3
                parameters = [obj.commands obj.flags obj.options];
            else
                parameters = [obj.(type)];
            end
            
            % Assume we don't have a parameter.
            result = 0;
        
            % Search for the parameter.
            for parameter_strings = parameters
                if sum(strcmp(parameter_string, parameter_strings{:}))
                    result = 1;
                    break;
                end
            end
        end
    end
end
