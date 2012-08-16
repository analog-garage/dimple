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

classdef MFactorGraph < handle
    
    properties
        Solver;
        GraphId;
    end
    
    methods
        function obj = MFactorGraph(varVector,solver)
           obj.Solver = solver;
           
           varids = [];
           if ~isempty(varVector)
               varids = varVector.VarIds;
           end
           
           obj.GraphId = obj.Solver.createGraph(varids);

        end
       
        function exists = customFactorExists(obj,funcName)
            exists = obj.Solver.customFactorExists(obj.GraphId,funcName);

        end
        

    
        function initialize(obj)
            obj.Solver.initialize(obj.GraphId);
        end
        
        function iterate(obj,numIterations)
            obj.Solver.iterate(obj.GraphId,numIterations);
        end
        
        function iters = getNumIterations(obj)
            obj.Solver.getNumIterations();
        end
        
        function setNumIterations(obj,numIterations)
            obj.Solver.setNumIterations(numIterations);
        end
        
        function solve(obj,initialize)
            if initialize == false
                error('not supported');
            end
            obj.Solver.solve(obj.GraphId);
        end
        
        
        function result = addGraph(obj,childGraph,varVector)
            obj.Solver.addGraph(obj.GraphId,childGraph.GraphId,varVector.VarIds);
            result = [];
        end
        
        function varIds = getGraphVariables(obj)
            varIds = obj.Solver.getGraphVariables(obj.GraphId);
            
        end
        
        function funcids = getGraphFunctions(obj)
            funcids = obj.Solver.getGraphFunctions(obj.GraphId);
        end
        
        function connectedVarIds = getConnectedVariableVector(obj,funcId)
            connectedVarIds = obj.Solver.getConnectedVariableVector(obj.GraphId,funcId);
        end
        
        function setScheduler(obj,schedule)
            obj.Solver.setScheduler(obj.GraphId,schedule);
        end
        
        function delete(obj)
           obj.Solver.removeGraphRef(obj.GraphId); 
        end
        
        function graph = getGraph(obj)
            graph = obj.Solver.getGraph(obj.GraphId);
        end
        
        function output = createFactor(obj,factorFunction,vars)
            id = obj.Solver.createTable(obj.GraphId,factorFunction.Table,factorFunction.Values);            
            output = obj.createTableFactor(id,vars,factorFunction.Name);
        end

                
        function result = createTable(obj,table,value)
            id = obj.Solver.createTable(obj.GraphId,table,value);
            result = MComboTable(id);
        end
        
        function output = createTableFactor(obj,tableId,varVector,funcName)
            output = MTableFactor(obj,tableId,varVector,funcName);
        end
       
        function func = createCustomFactor(obj,funcName,varVector)
            func = MCustomFactor(obj,funcName,varVector);
        end        
        
        function solver = getSolver(obj)
            solver = obj.Solver;
        end
        
        function output = getVariableVector(obj)
            varIds = obj.Solver.getGraphVariables(obj.GraphId);
            output = MVariableVector(varIds,obj.Solver);
        end
        
        function output = getFactors(obj)
            funcIds = obj.Solver.getGraphFunctions(obj.GraphId);
            output = cell(size(funcIds));
            for i = 1:length(output)
                output{i} = MFactor(obj,funcIds(i));
            end
        end

    end
    
    
    
end

