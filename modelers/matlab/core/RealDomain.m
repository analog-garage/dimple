classdef RealDomain < Domain
    
    properties
       LB;
       UB;
    end
    
    methods
        function obj = RealDomain(lowerBound,upperBound)
            if nargin < 2
                upperBound = Inf;
            end
            if nargin < 1
                lowerBound = -Inf;
            end
            obj.UB = upperBound;
            obj.LB = lowerBound;
            modeler = getModeler();
            obj.IDomain = modeler.createRealDomain(lowerBound,upperBound);
            
        end
        
        function result = isDiscrete(obj)
            result = false;
        end        
        
        function val = isequal(obj,other)
            if ~isa(other,'RealDomain')
                val = false;
            else
                val = isequal(obj.LB,other.LB) && isequal(obj.UB,other.UB);
            end
        end

    end
end
