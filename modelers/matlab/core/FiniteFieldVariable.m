classdef FiniteFieldVariable < Discrete
    
    properties
    end
    
    methods
        function obj = FiniteFieldVariable(poly,varargin)
            
            if numel(poly) ~= 1
                error('FiniteFieldVariable expects a polynomial to be specified as a single decimal number');
            end
            
            poly = double(dec2bin(poly)-'0');
            domain = 0:2^(length(poly)-1)-1;

            obj@Discrete(domain,varargin{:});
            
            
            for i = 0:obj.VarMat.size()-1
                var = obj.VarMat.getVariable(i);
                var.setProperty('primitivePolynomial',poly);
            end
        end
    end
    
end

