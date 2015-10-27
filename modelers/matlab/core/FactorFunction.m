%FactorFunction
%
% See also FactorFunctionRegistry

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013-2015 Analog Devices, Inc.
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

classdef FactorFunction < handle
    properties
        IFactorFunction;
    end
    methods
        function obj = FactorFunction(functionName, varargin)
            if ischar(functionName)
                % Create a Java FactorFunction
                registry = FactorFunctionRegistry();
                obj.IFactorFunction = registry.instantiate(functionName, varargin{:});
            else
                error('First argument must be a string');
            end
        end
        
        %get returns the underlying Java factor function instance.
        function factorFunction = get(obj)
            factorFunction = obj.IFactorFunction;
        end
    end
end