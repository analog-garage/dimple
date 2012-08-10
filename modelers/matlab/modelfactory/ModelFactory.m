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

