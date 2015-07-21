% Base for Dimple parameterized message objects.
%
% These objects are used to represent distributions for use as 
% variable priors and posterior beliefs.
%
% See also VariableBase.Prior

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

classdef ParameterizedMessage < handle
    properties
        % The underlying Java object that provides the implementation.
        IParameters;
    end
    
    methods
        function proxy = getProxyObject(obj)
            % Returns underlying Java proxy object.
            % 
            % This returns the value of the IParameters property.
            %
            % See also unwrapProxyObject, IParameters
            proxy = obj.IParameters;
        end
        
        % For backward compatibility
        function weight = eval(obj, value)
            weight = exp(-obj.evalEnergy(value));
        end
        
        % For backward compatibility
        function energy = evalEnergy(obj, value)
            energy = obj.IParameters.evalEnergy(value);
        end
    end
end
