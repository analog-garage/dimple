classdef MTableFactor < MFactor
    
    properties
       
    end
    
    methods
        
        function obj = MTableFactor(graph,tableId,varVector,funcName)
            
            id = graph.Solver.createTableFactor(graph.GraphId,tableId,varVector.VarIds,funcName);
            obj = obj@MFactor(graph,id);
        end

    end
    
end

