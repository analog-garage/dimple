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

classdef Factor < Node
    
    properties
        Variables;
        DirectedTo;
    end
    
    methods
        function obj = Factor(vectorObject,indices)
            if nargin < 2
                indices = 0:(vectorObject.size()-1);
            end
            obj@Node(vectorObject,indices);
        end
        
        function set.DirectedTo(obj,variables)
           if ~iscell(variables)
               variables = {variables};
           end
           indices = cell(size(variables));
           for i = 1:length(variables)
               indices{i} = MatrixObject.pack(variables{i}.VectorIndices,obj.VectorIndices);
               variables{i} = variables{i}.VectorObject;
           end
           obj.VectorObject.setDirectedTo(variables,indices);
        end
        
        function variables = get.DirectedTo(obj)
            pvarvector = obj.VectorObject.getDirectedToVariables();
            variables = wrapProxyObject(pvarvector);
        end
        
        
        %get the combination table associated with this
        
        function variables = get.Variables(obj)
            vars = obj.VectorObject.getVariables();
            
            variables = cell(vars.size(),1);
            
            for i = 1:length(variables)
                var = vars.getSlice(i-1);
                domain = cell(var.getDomain());
                indices = 0;
                variables{i} = Variable(domain,'existing',var,indices);
            end
            
            
        end
        
    end
    
    
    methods (Access = protected)
        
        function retval = createObject(obj,vectorObject,indices)
            retval = Factor(vectorObject,indices);
        end
        
        function verifyCanConcatenate(obj,otherObjects)
            
        end
    end
    
end

