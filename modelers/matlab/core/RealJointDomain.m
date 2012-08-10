classdef RealJointDomain < Domain
    
    methods
        function obj = RealJointDomain(varargin)
            
            %should allow either a number or a vector of RealDomains
            if length(varargin) == 1 && isnumeric(varargin{1})
                domains = cell(varargin{1},1);
                for i = 1:length(domains)
                    domains{i} = RealDomain();
                end
            else
                domains = varargin;
            end
            
            idomains = cell(size(domains));
            for i = 1:length(idomains)
                idomains{i} = domains{i}.IDomain;
            end
            
            modeler = getModeler();
            obj.IDomain = modeler.createRealJointDomain(idomains);
        end
        
        function retval = isDiscrete(obj)
            retval = false;
        end
    end
end
