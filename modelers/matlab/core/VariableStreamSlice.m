classdef VariableStreamSlice < IVariableStreamSlice
    properties
        IVariableStreamSlice;
    end
    methods
        function obj = VariableStreamSlice(intf)
            obj.IVariableStreamSlice = intf;            
        end        
        
        function next = getNext(obj)
            next = wrapProxyObject(obj.IVariableStreamSlice.getNext());
        end
        
        function ret = hasNext(obj)
           ret = obj.IVariableStreamSlice.hasNext();
        end
    end
end
