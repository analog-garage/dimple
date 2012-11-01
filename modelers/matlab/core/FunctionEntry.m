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

classdef FunctionEntry < handle
    properties
        FuncPointer;
        Tables = {};
    end
    methods
        function obj = FunctionEntry(funcPointer)
            obj.FuncPointer = funcPointer;
        end
        function [table,isnew] = getFactorTable(obj,name,domainLists,constants)
            
            %Look through all existing tables to see if there are matching
            %domains
            for i = 1:length(obj.Tables)
                
                
                table = obj.Tables{i};
                tableDomains = table{1};
                tableConstants = table{2};
                tableFactorFunction = table{3};
                
                
                if length(domainLists) == length(tableDomains)
                    
                    
                    if ~all(tableConstants == constants)
                        match = 0;
                    else

                        match = 1;

                        for domainListIndex = 1:length(domainLists)
                            tableDomainList = tableDomains{domainListIndex};
                            domainList = domainLists{domainListIndex};

                            if length(tableDomainList) == length(domainList)

                                for domainIndex = 1:length(tableDomainList)

                                    tdomain = tableDomainList{domainIndex}.Elements;
                                    domain = domainList{domainIndex}.Elements;

                                    if length(tdomain) ~= length(domain)
                                        match = 0;
                                        break;
                                    end

                                    for domainItemIndex = 1:length(tdomain)
                                        if ~isequal(tdomain{domainItemIndex},domain{domainItemIndex})
                                            match = 0;
                                            break;
                                        end
                                    end
                                    if match == 0
                                        break;
                                    end

                                end

                            else
                                match = 0;
                            end

                            if match == 0
                                break;
                            end

                        end
                    end
                    
                    if match == 1
                        isnew = 0;
                        return;
                    end
                end
            end
            
            table = FunctionEntry.createFactorTable(name,domainLists,constants,obj.FuncPointer);
            obj.Tables{length(obj.Tables)+1} = table;
            isnew = 1;
        end
    end
    
    methods (Static)
        
        function factorTable = createFactorTable(name,domainLists,constants,funcPointer)
            
            domains = {};
            domainElements = {};
            newconstants = [];
            
            dimensions = cell(numel(domainLists),1);
            
            inputs = cell(numel(domainLists),1);
            domainSizes = [];
            %isVector = [];
            
            for i = 1:length(domainLists)
                dimensions{i} = size(domainLists{i});
                inputs{i} = zeros(size(domainLists{i}));
                
                for j = 1:prod(dimensions{i})
                    ind = length(domains)+1;
                    domains{ind} = domainLists{i}{j};
                    domainElements{ind} = domains{ind}.Elements;
                    domainSizes(ind) = length(domainElements{ind});
                    newconstants(ind) = constants(i);
                    
                end
            end
            
            funcPointer = funcPointer;
            numRows = 1;
            numIndices = length(domainElements);
            for i = 1:numIndices
                numRows = numRows * length(domainElements{i});
            end
            
            table = zeros(numRows,numIndices);
            values = zeros(numRows,1);
            indices = domainSizes;
            
            curRow = 1;
            
            lookup = ones(numIndices,2);
            
            curInputIndex = 1;
            curArrayIndex = 1;
            
            isVector = zeros(size(domainElements));
            
            for j = 1:numIndices
                lookup(j,:) = [curInputIndex,curArrayIndex];
                
                if isequal(dimensions{curInputIndex},1)
                    curInputIndex = curInputIndex + 1;
                    curArrayIndex = 1;
                    isVector(j) = 0;
                else
                    isVector(j) = 1;
                    
                    curArrayIndex = curArrayIndex + 1;
                    if curArrayIndex > prod(dimensions{curInputIndex});
                        curInputIndex = curInputIndex + 1;
                        curArrayIndex = 1;
                    end
                end
            end
            
            
            for i = 1:numRows
                
                %START time critical code
                %This is the tight inner loop that must run fast
                for j = 1:numIndices
                    
                    tmp = indices(j)+1;
                    if tmp > domainSizes(j)
                        index = 1;
                    else
                        index = tmp;
                    end
                    
                    indices(j) = index;
                    
                    val = domainElements{j}{index};                    
                    index2inputs = lookup(j,:);
                    
                    if ~isVector(j) == 1
                        inputs{index2inputs(1)} = val;
                    else
                        inputs{index2inputs(1)}(index2inputs(2)) = val;
                    end
                    
                    if index ~= 1
                        break;
                    end
                end
                %END time critical code.
                
                value = funcPointer(inputs{:});
                
                if value ~= 0
                    table(curRow,:) = indices-1;
                    values(curRow) = value;
                    curRow = curRow+1;
                end
            end
            
            numEntries = curRow - 1;
            
            if numEntries == 0
                error('Factor must return non zero for at least one combination of inputs');
            end
            
            table = table(1:numEntries,:);
            values = values(1:numEntries);
            
            %TODO: do I want this global here?
            modeler = getModeler();
            %TODO: name
            domainobjs = cell(size(domains));
            for i = 1:length(domains)
                domainobjs{i} = domains{i}.IDomain;
            end
            
            %remove columns from table and from the domain lists
            table = table(:,logical(1-newconstants));
            domainobjs = domainobjs(logical(1-newconstants));
            
            if isempty(domainobjs)
                error('addFactor must contain at least one variable');
            end
            
            tableFactor = modeler.createTableFactorFunction(name,table,values,domainobjs);
            factorTable = {domainLists,constants,tableFactor};
        end
    end
end
