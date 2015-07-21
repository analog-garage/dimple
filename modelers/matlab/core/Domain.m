% Domain is the base Dimple domain class
%
% The actual implementation is provided by an underlying Java object.

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

classdef Domain < handle
    
    properties
        % Underlying Java proxy object that provides the actual implementation.
        IDomain;
    end
    
    methods

        function proxy = getProxyObject(obj)
            % Returns underlying Java proxy object.
            % 
            % This returns the value of the IDomain property.
            %
            % See also unwrapProxyObject, IDomain
            proxy = obj.IDomain;
        end
    end
    
    methods (Abstract)
       result = isDiscrete(obj); 
    end
    
end
    
