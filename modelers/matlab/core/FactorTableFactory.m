classdef FactorTableFactory < handle
    properties
        Name2FunctionEntry;
    end
    methods
        function obj = FactorTableFactory()
            obj.Name2FunctionEntry = FunctionEntryDictionary();
        end
        
        %function factorTable = createFactorTable(name,domainLists,constants,funcPointer)

        function [table,isNewTable] = getTable(obj,funcPointer,domains,constants)
            %Get name of function
            name = func2str(funcPointer);
            
            %Look through map to see if table exists
            fe = obj.Name2FunctionEntry.get(name);
            
            if fe == 0
                fe = FunctionEntry(funcPointer);
                obj.Name2FunctionEntry.add(name,fe);
            end
            
            [table,isNewTable] = fe.getFactorTable(name,domains,constants);            
        end
    end
end
