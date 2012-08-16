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
