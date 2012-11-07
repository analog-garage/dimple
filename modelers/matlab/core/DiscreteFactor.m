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

classdef DiscreteFactor < Factor
    
    properties
        FactorTable;
        Belief;
        FullBelief;
        Indices;
        Domain;
    end
    
    methods
        function obj = DiscreteFactor(vectorObject,indices)
            if nargin < 2
                indices = 0:(vectorObject.size()-1);
            end
            obj@Factor(vectorObject,indices);
        end
        
        function [retval] = get.FactorTable(obj)
            table = obj.VectorObject.getFactorTable();
            retval = FactorTable(table);
        end
        
        function beliefs = get.Belief(obj)
            beliefs = obj.VectorObject.getDiscreteBeliefs(obj.VectorIndices);
            beliefs = MatrixObject.unpack(beliefs,obj.VectorIndices);
        end
        function indices = get.Indices(obj)
            var = obj.getSingleNode();
            indices = var.getPossibleBeliefIndices(0)+1;
        end
        function d = get.Domain(obj)
            indices = obj.Indices;
            vars = obj.VectorObject.getVariables();
            
            d = cell(size(indices));
            
            for r = 1:size(indices,1)
                for c = 1:size(indices,2)
                    domain = cell(vars.getSlice(c-1).getDomain().getElements());
                    d{r,c} = domain{indices(r,c)};
                end
            end
            
        end
        
        function fullBelief = get.FullBelief(obj)
            
            vars = cell(obj.VectorObject.getVariables());
            dims = zeros(1,numel(vars));
            for i = 1:length(vars)
                v = vars{i};
                dims(i) = numel(v.getDomain());
            end
            
            x = zeros(dims);
            
            beliefs = obj.Belief;
            
            for i = 1:size(obj.Indices,1)
                %Is there a better way?
                tmp = cell(1,size(obj.Indices,2));
                for j = 1:size(obj.Indices,2)
                    tmp{j} = obj.Indices(i,j);
                end
                x(tmp{:}) = beliefs(i);
            end
            
            if nargin > 1
                x = subsref(x,subs);
            end
            
            fullBelief=x;
            
        end
        
    end
    methods (Access = protected)
        
        function retval = createObject(obj,vectorObject,indices)
            retval = DiscreteFactor(vectorObject,indices);
        end
    end
    
    
end

