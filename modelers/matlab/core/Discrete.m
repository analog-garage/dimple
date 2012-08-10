classdef Discrete < DiscreteVariableBase
    
    properties 
        Input;
        Belief;
    end
    methods
        function obj = Discrete(domain,varargin)
            obj@DiscreteVariableBase(domain,varargin{:});
        end
        
        function x = get.Input(obj)
            x = obj.getInput();
        end
        function x = get.Belief(obj)
            x = obj.getBelief();
        end
        
        function var = createVariable(obj,domain,varMat,indices)
            var = Discrete(domain,'existing',varMat,indices);
        end
        
    end
    
    methods (Access = protected)
        
        
    end

end
