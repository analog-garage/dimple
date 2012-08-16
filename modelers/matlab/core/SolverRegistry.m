%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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

