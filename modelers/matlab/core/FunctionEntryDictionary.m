classdef FunctionEntryDictionary < handle
    properties
        dictionary = {}; 
    end
    methods
        function add(obj,name,functionEntry)
           entry.name = name;
           entry.functionEntry = functionEntry;
           obj.dictionary{length(obj.dictionary)+1} = entry;
        end
        function retval = get(obj,name)
            retval = 0;
            for i = 1:length(obj.dictionary)
                if isequal(obj.dictionary{i}.name, name)
                    retval = obj.dictionary{i}.functionEntry;
                    break;
                end
            end
        end
    end
end
