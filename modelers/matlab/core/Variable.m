classdef Variable < Discrete
    
    methods
        
        function obj = Variable(domain,varargin)
            obj@Discrete(domain,varargin{:});
        end
        
        function var = createVariable(obj,domain,varMat,indices)
            var = Variable(domain,'existing',varMat,indices);
        end

        
    end
    
    methods (Access = protected)
        
        
    end

end
