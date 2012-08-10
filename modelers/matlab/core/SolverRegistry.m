classdef SolverRegistry < handle
    properties
        Map;
        Names;
    end
    
    methods
        function obj = SolverRegistry()
            obj.Map = containers.Map();
            obj.initializeSolvers();
        end
        
        function initializeSolvers(obj)
            obj.register('CSolver',@CSolver);
            obj.register('particleBP',@com.analog.lyric.dimple.solvers.particleBP.Solver);
        end
        
        
        function names = get.Names(obj)
            names = obj.Map.keys;
        end
        
        function register(obj,name,solver)
            name = lower(name);
            if obj.Map.isKey(name)
                error('Name has already been registered: %s\n',name);
            end
            obj.Map(name) = solver;
        end
        
        function unregister(obj,name)
            name = lower(name);
            if ~obj.Map.isKey(name)
                error('Solver has not been registered: %s',name);
            end
            obj.Map.remove(name);
        end
        
        function solver = get(obj,name)
            name = lower(name);
            if ~obj.Map.isKey(name)
                composedName = ['com.analog.lyric.dimple.solvers.' name '.Solver']; 
                if exist(composedName, 'class') == 8
                    solver = eval(['@' composedName]);                    
                else
                    error('Solver [%s] composed from [%s] is not a class', composedName, name);
                end
            else
            	solver = obj.Map(name);
            end
        end
    end
    
end

