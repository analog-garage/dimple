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

