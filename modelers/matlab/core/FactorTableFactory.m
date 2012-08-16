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
