classdef FactorGraphStream < handle
    properties
        IFactorGraphStream;
        hasNext;
        BufferSize;
    end
    methods
        function obj = FactorGraphStream(intf)
            obj.IFactorGraphStream = intf;
        end
        
        function next = get.hasNext(obj)
           next = obj.IFactorGraphStream.hasNext(); 
        end
        
        function advance(obj)
            obj.IFactorGraphStream.advance();
        end
        
        function set.BufferSize(obj,sz)
            obj.IFactorGraphStream.setBufferSize(sz);
        end
        
        function sz = get.BufferSize(obj)
            sz = obj.IFactorGraphStream.getBufferSize();
        end
    end
end
