%FactorFunctionRegistry is used for looking up and instantiating FactorFunction instances
%
% Used by FactorFunction and FactorGraph to construct factor function objects
% from a simple name.
%
% The default constructor FactorFunctionRegistry() returns the shared registry
% instance for the current dimple environment.
%
% This provides a simple MATLAB wrapper around the underlying Java
% implementation. It does not contain any state independent from the
% Java object.
%
% See also FactorFunction, FactorGraph, DimpleEnvironment.FactorFunctions

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012-2015 Analog Devices, Inc.
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

classdef FactorFunctionRegistry < handle
    properties
        %Names Read-only list of FactorFunction implementions in the registry.
        % Lists both unqualified and fully qualified versions of names of each function.
        Names;
        %Packages Read-only list of packages indexed by the registry.
        % Packages are listed in the order in which they were added, which is also
        % the order in which they are searched.
        Packages;
    end
    
    properties (Access=private)
        PRegistry;
    end
    
    methods
        function obj = FactorFunctionRegistry(pregistry)
            if nargin < 1
                env = DimpleEnvironment.active();
                pregistry = env.PEnvironment.factorFunctions();
            end
            validateattributes(pregistry, {'com.analog.lyric.dimple.matlabproxy.PFactorFunctionRegistry'}, {});
            obj.PRegistry = pregistry;
        end
        
        function addPackage(obj, package)
            %addPackage adds a factor function package to the end of the search list.
            %
            %    addPackage(package)
            %
            % package - is the fully qualfied name of a Java package that should be searched
            %           for Java FactorFunction implementations.
            obj.PRegistry.addPackage(package);
        end
        
        function proxy = getProxyObject(obj)
            %getProxyObject returns the underlying Java proxy object
            proxy = obj.PRegistry;
        end
        
        function names = get.Names(obj)
            obj.PRegistry.loadAll();
            names = cell(obj.PRegistry.getClasses());
        end
        
        function packages = get.Packages(obj)
            packages = cell(obj.PRegistry.getPackages());
        end
 
        function factorFunction = get(obj,name)
            %get returns a function handle that can be used to construct a Java FactorFunction
            % See also instantiate
            classname = char(obj.PRegistry.getClass(name));
            if isempty(classname)
                error('Cannot find factor function [%s]', name);
            else
                factorFunction = eval(['@' classname]);
            end
        end
        
        function factorFunction = instantiate(obj,name,varargin)
            %instantiate creates a new Java FactorFunction instance for given name and arguments. 
            % Intended for internal use within MATLAB FactorFunction and FactorGraph classes.
            % See also FactorFunction
            constructor = get(obj,name);
            factorFunction = constructor(varargin{:});
        end
    end
    
end

