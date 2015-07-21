% VariableBlock represents a block of variables that may be used to define
% block schedule entries and other block operations. A variable block is a
% child of the FactorGraph for which it was defined but is not a Node.

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

classdef VariableBlock < FactorGraphChild
    properties
        % Enumerates the variables in the block
        Variables;
    end
    
    methods
        function obj = VariableBlock(vectorObject,varargin)
            narginchk(1,2);
            if nargin > 1
                vectorIndices = varargin{2};
            else
                vectorIndices = 0:(vectorObject.size()-1);
            end
            obj@FactorGraphChild(vectorObject, vectorIndices);
        end
        
        function vars = get.Variables(obj)
            vars = wrapProxyObject(obj.VectorObject.getVariables());
        end
        
        function proxy = getProxyObject(obj)
            % Returns underlying Java proxy object.
            % 
            % This returns the value of the VectorObject property.
            %
            % See also unwrapProxyObject, VectorObject
            proxy = obj.Variables;
        end
    end
    
    methods(Access=protected)
        function retval = createObject(~,vectorObject,VectorIndices)
            retval = VariableBlock(vectorObject,VectorIndices);
        end
        function verifyCanConcatenate(~,~)
        end
    end
end