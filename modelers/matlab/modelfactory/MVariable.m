classdef MVariable < handle
    properties
        Id;
        Solver;
    end
    methods
        function obj = MVariable(solver,id)
            obj.Solver = solver;
            obj.Id = id;
        end
        function id = getId(obj)
           id = obj.Id; 
        end
    end
end
