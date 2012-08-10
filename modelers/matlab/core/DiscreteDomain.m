classdef DiscreteDomain < Domain
    properties
        Elements;
    end
    
    methods
        function obj = DiscreteDomain(elements)
            if ~iscell(elements)
                newdomain = cell(1,numel(elements));
                for i = 1:numel(elements);
                    newdomain{i} = elements(i);
                end
                elements = newdomain;
            end
            obj.Elements = elements;
            
            modeler = getModeler();
            try
                obj.IDomain = modeler.createDiscreteDomain(elements);
            catch exception
                newdomain = cell(size(elements));
                
                for i = 1:length(newdomain)
                    newdomain{i} = sprintf('domainitem%d',i);
                end
                
                obj.IDomain = modeler.createDiscreteDomain(newdomain);
            end
            
        end
        function val = isequal(obj,other)
            if ~isa(other,'DiscreteDomain')
                val = false;
            else
                val = isequal(obj.Elements,other.Elements);
            end
        end
        
        function result = isDiscrete(obj)
            result = true;
        end
    end
    
end
