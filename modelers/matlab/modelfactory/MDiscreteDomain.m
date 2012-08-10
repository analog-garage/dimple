classdef MDiscreteDomain < handle
    properties
        Elements;
    end
    methods
        function obj = MDiscreteDomain(newdomain)
            obj.Elements = newdomain;
        end
    end
end
