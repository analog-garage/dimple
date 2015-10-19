% CustomFactors holds custom solver factor creation associations
% for those solvers that allow it through an appropriate 'customFactors'
% option.
%
% Custom factors must be implemented as a Java subtype of the appropriate
% solver class and/or interface for that type of solver.
%
% For example, if you have implemented a factor function in Java with the
% fully qualified class name 'mypackage.MyFunction' and have implemented an
% optimized update for sum-product in another Java class with the fully
% qualified name 'mypackage.MyCustomFactor', then you can cause Dimple to
% use this custom factor implementation for all FactorGraphs in the active
% environment by writing:
%
%    env = DimpleEnvironment.active();
%    cf = CustomFactors('SumProductOptions.customFactors', ...
%             'mypackage.MyFunction', ...
%             'mypackage.MyCustomFactor');
%    env.setOption('SumProductOptions.customFactors', cf);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2015 Analog Devices, Inc.
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

classdef CustomFactors < handle
    properties
        % The Java proxy object that provides the underlying
        % implementation.
        ICustomFactors;
    end
    
    methods
        function obj = CustomFactors(firstArg,varargin)
            %CustomFactors constructor
            %
            %    CustomFactors(optionName)
            %       creates empty CustomFactors for specified option
            %    CustomFactors(optionName, addArgs)
            %       creates empty CustomFactors for specified option and
            %       add mappings
            %    CustomFactors(proxyObject)
            %       creates wrapper for PCustomFactors Java proxy object
            %
            % optionName - is the name of the custom factors option to
            % which the custom factor mappings is to be set. By convention
            % these should have the unqualified name 'customFactors' and be
            % defined in the option namespace for the corresponding solver.
            % Supported custom factors options include:
            %
            %      'GibbsOptions.customFactors'
            %      'SumProductOptions.customFactors'
            %      'MinSumOptions.customFactors'
            %
            % addArgs - are additional arguments that will be passed to the
            % add method.
            %
            % proxyObject - is an existing instance of the underlying Java proxy
            % object. This form of constructor is primarily for internal use.
            %
            % See also add
            
            if isa(firstArg, 'com.analog.lyric.dimple.matlabproxy.PCustomFactors')
                obj.ICustomFactors = firstArg;
                narginchk(1,1);
            else
                validateattributes(firstArg, {'char'}, {'vector'}, 'CustomFactors', 'optionName');
                modeler = getModeler();
                obj.ICustomFactors = modeler.createCustomFactors(firstArg);
                if ~isempty(varargin)
                    obj.add(varargin{:});
                end
            end
        end
        
        function proxy = getProxyObject(obj)
            %getProxyObject returns the Java proxy object that provides the underlying
            % implementation.
            proxy = obj.ICustomFactors;
        end
        
        function add(obj,varargin)
            %add custom solver factor mappings
            %
            %    add(functionName, customClass)
            %       adds a mapping from a function to its custom class
            %    add(mappings)
            %       adds multiple mappings from a n x 2 cell array
            %    add('builtins',__)
            %       adds builtin mappings
            %    add('first',__)
            %       mappings will be prepended ahead of others for the same
            %       factor function
            %
            % functionName - 
            narginchk(1,3);
            if isequal(varargin{1}, 'builtins')
                obj.ICustomFactors.addBuiltins();
                varargin = varargin(2:end);
            end
            first = false;
            if numel(varargin) > 0 && isequal(varargin{1}, 'first')
                first = true;
                varargin = varargin(2:end);
            end
            switch numel(varargin)
                case 0
                case 1
                    mappings = varargin{1};
                    validateattributes(mappings, {'cell'}, {'2d', 'ncols', 2}, ...
                        'CustomFactors.add', 'mappings');
                    if ~iscellstr(mappings)
                        error('mappings contains entry that is not a string');
                    end
                    obj.ICustomFactors.add(first, mappings);
                case 2
                    functionName = varargin{1};
                    customClass = varargin{2};
                    validateattributes(functionName, {'char'}, {'vector'});
                    validateattributes(customClass, {'char'}, {'vector'});
                    obj.ICustomFactors.add(first, functionName,customClass);
            end
        end
        
        function disp(obj)
            disp(obj.ICustomFactors);
        end
    end
end