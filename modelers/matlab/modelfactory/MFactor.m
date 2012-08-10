classdef MFactor < handle
    
    properties
        Graph;
        FuncId;
    end
    
    methods        
        function obj = MFactor(graph,funcId)
            obj.Graph = graph;
            obj.FuncId = funcId;
        end
        function vars = getConnectedVariableVector(obj)
            varIds = obj.Graph.Solver.getConnectedVariableVector(obj.Graph.GraphId,obj.FuncId);
            vars = MVariableVector(varIds,obj.Graph.Solver);
        end
        
        function id = getId(obj)
            id = obj.FuncId;
        end
    end
end

