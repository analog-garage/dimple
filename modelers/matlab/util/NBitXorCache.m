classdef NBitXorCache < SymmetricRelationTreeGraphCache
    
    methods
        function [graph_out,external_vars] = createGraph(obj,n)
            [graph_out,external_vars] = ...
                createGraph@SymmetricRelationTreeGraphCache(obj,@xorDelta,@Variable,[0 1],n);
        end
    end
    
    methods (Access=protected)
        function key = createKey(obj,n)
            key = createKey@SymmetricRelationTreeGraphCache(obj,@xorDelta,@Variable,[0 1],n);
        end
        
    end
    
end

