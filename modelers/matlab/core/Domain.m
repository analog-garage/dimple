classdef Domain < handle
    
    properties
        IDomain;
    end
    
    methods (Abstract)
       result = isDiscrete(obj); 
    end
    
end
    
