%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
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

classdef CSolver < handle
    properties
        NumIterations;
    end
    
    methods
        
        function gid = createGraph(obj,varIds)
            graphIds = ones(size(varIds))*-1;
            gid = DimpleEntry('newgraph',graphIds,varIds);
        end
        
        function varIds = createVariable(obj,domain)
            error('Not supported');
        end
        
        function varIds = createVariables(obj,name,domain,numVariables)
            [varIds] = DimpleEntry('newvars',numVariables,length(domain));                        
        end

        
        function retVal = customFactorExists(obj,graphId,funcName)
            retVal = 0;
        end
        
        function addGraph(obj,parentGraphId,childGraphId,varIds)
            graphIds = ones(size(varIds))*-1;
            DimpleEntry('nestgraph',parentGraphId,childGraphId,graphIds,varIds);
        end
        
        function createCustomFactor(obj,graphId,funcName, varIds)
            error('not supported');
        end

        function id = createTable(obj,graphId,table,values)
            id = DimpleEntry('createTable',graphId,table,values);
        end
        
        function id = createTableFactor(obj,graphId,tableId,varIds,dummyFunctionNameIgnored)
            %id = obj.createTable(graphId,table
            graphIds = ones(size(varIds))*-1;
            id = DimpleEntry('createTableFunc',graphId,tableId,graphIds,varIds);           
        end
        
        function setVariableInput(obj,graphId,varId,priors)
            error('not supported');
            
        end
        
        function getVariableInput(obj,graphId,varId)
            error('not supported');
            
        end
        
        function initialize(obj,graphId)
            DimpleEntry('initgraph',graphId);
            
        end
        
        function solve(obj,graphId)
            DimpleEntry('solve',graphId);
            
        end
        
        function getVariableBelief(obj,graphId,varId)
            error('not supported');
            
        end
        
        function setCustomSchedule(obj, graphId,fromIsVariable,frmIds, toIds)
            error('not supported');
            
        end
        
        function iterate(obj,graphId,N)
            if nargin < 3
                N = 1;
            end
            DimpleEntry('iterate',graphId,N);
        end
        
        function beliefs = getMultipleVariableBelief(obj,varIds)
            %//		//<command> <fg ids> <varIds>
            graphIds = ones(size(varIds))*-1;
            beliefs = DimpleEntry('getbeliefs',graphIds,varIds);
        end
        
        function priors = getMultipleVariableInput(obj,varIds)
            error('not supported');
        end
        
        function setMultipleVariableInput(obj,varIds,priors)
            graphIds = ones(size(varIds))*-1;
            DimpleEntry('setpriors',graphIds,varIds,priors);
        end
        
        function setNumIterations(obj,numIter)
            DimpleEntry('setnumiter',numIter);
            obj.NumIterations = numIter;
            
        end
        
        function numiter = getNumIterations(obj)
            numiter = obj.NumIterations;
        end
        
        function varIds = getGraphVariables(obj,graphId)            
            varIds = DimpleEntry('getgraphvars',graphId);
        end
        function funcIds = getGraphFunctions(obj,graphId)
            funcIds = DimpleEntry('getgraphfunctions',graphId);
        end
        function varIds = getConnectedVariableVector(obj,graphId,funcId)
            varIds = DimpleEntry('getconnectedvariables',graphId,funcId);
        end
        
        %function addGraphRef(obj,graphId)
        %end
        function removeGraphRef(obj, graphId)
        end
        %function addVariableRef(obj, varId)
        %end
        %function removeVariableRef(obj, varId)
        %end
        function addMultipleVariableRef(obj,varIds)
        end
        function removeMultipleVariableRef(obj, varIds)
        end
        function getVariableIds(obj)
            error('not supported');
        end
        function getFactorGraphIds()
            error('not supported');
        end
        function domain = getVariableDomain(obj,ids)
            domain = {};
        end
    end
end
