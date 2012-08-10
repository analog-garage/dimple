classdef NVarFiniteFieldPlusCache < SymmetricRelationTreeGraphCache
    methods
        function [graph_out,external_vars] = createGraph(obj,poly,n)
            [graph_out,external_vars] = ...
                createGraph@SymmetricRelationTreeGraphCache(obj,@finiteFieldAdd,@FiniteFieldVariable,poly,n);
        end
    end
    
    methods (Access=protected)
        function key = createKey(obj,poly,n)
            key = createKey@SymmetricRelationTreeGraphCache(obj,@finiteFieldAdd,@FiniteFieldVariable,poly,n);
        end
        
    end
end

