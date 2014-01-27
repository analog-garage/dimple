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

classdef FactorTable < handle
    properties (SetAccess=private)
        Indices;
        Weights;
        Domains;
        ITable;
    end
    methods
        function obj = FactorTable(varargin)
            if length(varargin) < 1
                error('FactorTable requires more arguments');
            end
            
            if isa(varargin{1},'Domain') || iscell(varargin{1})
                constructFromDomains(obj,varargin);
            elseif isa(varargin{1},'double')
                if numel(varargin) < 2
                    error('FactorTable requires two or more arguments when first argument is a double');
                end
                if isa(varargin{2},'double')
                    obj.constructFromIndicesAndValues(varargin{1},varargin{2},{varargin{3:end}});
                else
                    obj.constructFromNDimensionalArray(varargin{1},{varargin{2:end}});
                end
            else
                try
                    varargin{1}.isFactorTable();
                catch exception
                    error('Must specify either domains or indices, values, and domains');
                end
                
                obj.constructFromITable(varargin{1});
            end
        end
        
        function value = get(obj,varargin)
            indices = obj.getIndicesFromDomainValues(varargin);
            value = obj.ITable.get(indices);
        end
        
        function set(obj,varargin)
            
            if ~iscell(varargin{1});
                obj.setSingle(varargin{:});
            else
                for i = 1:length(varargin)
                    obj.setSingle(varargin{i}{:});
                end
            end
            
        end
        
        function domains = get.Domains(obj)
            if isempty(obj.Domains)
                pdomains = obj.ITable.getDomains();
                domains = cell(numel(pdomains),1);
                for i =1:length(domains)
                    domains{i} = DiscreteDomain(pdomains(i).getElements());
                end
                obj.Domains = domains;
                %obj.ITable.getDomains());
            end
            domains = obj.Domains;
        end
        
        
        function indices = get.Indices(obj)
            if ~isempty(obj.ITable)
                indices = obj.ITable.getIndices();
            else
                indices = obj.Indices;
            end
            indices = double(indices);
        end
        
        function set.Indices(obj,indices)
        	error('Cannot directly set FactorTable.Indices. Use change(indices,weights) instead.');
        end
        
        function values = get.Weights(obj)
            if ~isempty(obj.ITable)
                values = obj.ITable.getWeights();
            else
                values = obj.Weights;
            end
        end
        
        function set.Weights(obj,weights)
            if ~isempty(obj.ITable)
                obj.ITable.changeWeights(weights);
            else
                obj.Weights = weights;
            end
        end
        
        function plot(obj)
            table = obj;
            mx = max(table.Indices(:));
            colormap('bone');
            sz = size(colormap(),1);
            subplot(1,2,1);
            %[1 size(table.Indices,2)]*10,[1 size(table.Indices,1)],
            image(fliplr(table.Indices*sz/mx));
            %axis equal;
            %axis tight;
            mx = max(table.Weights);
            subplot(1,2,2);
            colormap('default');
            image(1:2,length(table.Weights),table.Weights*sz/mx);
            axis equal;
            axis tight;
        end
        
        function change(obj,indices,weights)
            if ~isempty(obj.ITable)
                obj.ITable.change(indices,weights);
            else
                obj.Indices = indices;
                obj.Weights = weights;
            end
        end
        
        function normalize(obj,directedTo)
            obj.ITable.normalize(directedTo);
        end
        
        
    end
    
    
    
    methods(Access=private)
        
        function setSingle(obj,varargin)
            %extract domains
            domains = {varargin{1:end-1}};
            %extract value
            value = varargin{end};
            %extract indices from domains
            indices = obj.getIndicesFromDomainValues(domains);
            
            %call set given indices
            obj.ITable.set(indices,value);
            
        end
        
        function idomains = processDomains(obj,domains)
            for i = 1:length(domains)
                if iscell(domains{i})
                    domains{i} = DiscreteDomain(domains{i});
                end
            end
            obj.Domains = domains;
            idomains = obj.domains2idomains(domains);
        end
        function indices = getIndicesFromDomainValues(obj,domains)
            
            if isempty(obj.Domains)
                error('feature not yet supported');
            end
            
            if length(domains) ~= length(obj.Domains)
                error('incorrect number of domains');
            end
            indices = zeros(length(domains),1);
            for i = 1:length(domains)
                di = domains{i};
                found = false;
                
                for j = 1:length(obj.Domains{i}.Elements)
                    element = obj.Domains{i}.Elements{j};
                    if isequal(element,di)
                        found = true;
                        indices(i) = j-1;
                        break;
                    end
                end
                
                if ~found
                    error(['invalid domain element at index ' num2str(i)]);
                end
                
            end
        end
        
        function constructFromITable(obj,itable)
            obj.ITable  = itable;
        end
        function constructFromNDimensionalArray(obj,values,domains)
            modeler = getModeler();
            idomains = obj.processDomains(domains);
            obj.ITable = modeler.createFactorTable(values,idomains);
        end
        function constructFromIndicesAndValues(obj,indices,values,domains)
            modeler = getModeler();
            idomains = obj.processDomains(domains);
            obj.ITable = modeler.createFactorTable(indices,values,idomains);
        end
        
        
        function constructFromDomains(obj,domains)
            modeler = getModeler();
            idomains = obj.processDomains(domains);
            obj.ITable = modeler.createFactorTable(idomains);
        end
        
        function idomains = domains2idomains(obj,domains)
            idomains = cell(length(domains),1);
            
            for i = 1:length(domains)
                idomains{i} = domains{i}.IDomain;
            end
            
        end
    end
end
