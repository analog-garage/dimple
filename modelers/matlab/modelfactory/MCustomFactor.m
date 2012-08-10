classdef MCustomFactor < handle
    
    properties
        TableFactor;
    end
    
    methods
        
        function obj = MCustomFactor(graph,funcName,varVector)
            obj.TableFactor = graph.Solver.createCustomFunc(graph.GraphId,funcName,varVector.VarIds);
        end

    end
    
end

