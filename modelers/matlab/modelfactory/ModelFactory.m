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

classdef ModelFactory < handle
    properties
        Solver;
    end
    methods
        function obj = ModelFactory()
            obj.Solver = getSolver();
        end
        function output = createVariableVector(obj,className,domain,numEls)
           output = MVariableVector(className,domain,numEls,obj.Solver); 
        end
        
        function output = createGraph(obj,varVector)
            %Deal with case where variables are passed as an array.
            if iscell(varVector)
               tmp = varVector{1};
               varids = [];
               for i = 1:length(varVector)
                   varids = [varids varVector{i}.VarIds];
               end
               tmp.VarIds = varids;
               varVector = tmp;
            end
            
            output = MFactorGraph(varVector,obj.Solver);

        end
        function setSolver(obj,solver)
            obj.Solver = solver;
        end
        
        function domain = createDiscreteDomain(obj,newDomain)
            domain = MDiscreteDomain(newDomain);
        end
        
        function tableFactor = createFactorTable(obj,table,values,domainobjs)
            tableFactor = MComboTable('table',table,values,domainobjs);
        end
        
        function tableFactor = createTableFactorFunction(obj,name,table,values,domainobjs)
            tableFactor = MComboTable(name,table,values,domainobjs);
        end
    end
    
end

