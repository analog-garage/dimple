%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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
        IDiscreteFactor;
        FactorTable;
        Belief;
        Domain;
        Indices;
        FullBelief;
    end
    
    methods
        function obj = DiscreteFactor(discreteFactor)
            obj@Factor(discreteFactor);
            obj.IDiscreteFactor = discreteFactor;
        end
        
        
        function [retval] = get.FactorTable(obj)
            table = obj.IDiscreteFactor.getFactorTable();
            retval = FactorTable(table);
        end
                
        function belief = get.Belief(obj)
            belief = obj.IFactor.getBelief();
        end
        
        function indices = get.Indices(obj)
            indices = obj.IFactor.getPossibleBeliefIndices()+1;
        end
        
        function d = get.Domain(obj)
            indices = obj.Indices;
            vars = cell(obj.IFactor.getConnectedVariables());
            
            d = cell(size(indices));
            
            for r = 1:size(indices,1)
                for c = 1:size(indices,2)
                    domain = cell(vars{c}.getDomain().getElements());
                    d{r,c} = domain{indices(r,c)};
                end
            end
            
        end        
        
        function fullBelief = get.FullBelief(obj)
            
            vars = cell(obj.IFactor.getConnectedVariables());
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
    
    
end

