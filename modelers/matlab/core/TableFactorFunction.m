classdef TableFactorFunction < handle 
    properties
        %Indices;
        %Weights;
        TableFactor;
        ITableFactorFunction;
    end
    methods
        function obj = TableFactorFunction(varargin)
            error('blah');
            if length(varargin) < 1
                error('FactorTable requires more arguments');
            end
            
            if isa(varargin{1},'Domain')
                constructFromDomains(obj,varargin);
            elseif isa(varargin{1},'double')
                if numel(varargin) < 2
                    error('FactorTable requires two or more arguments when first argument is a double');
                end
                if isa(varargin{2},'double')
                    constructFromIndicesAndValues(varargin{1},varargin{2},varargin{3:end});
                else
                    constructFromNDimensionalArray(varargin{1},varargin{3:end});
                end
            else
                constructFromITable(obj,varargin{1});
            end
            
        end

        
        %{
        function indices = get.Indices(obj)
            if ~isempty(obj.ITableFactorFunction)
                indices = obj.ITableFactorFunction.getFactorTable().getIndices();
            else
                indices = obj.Indices;
            end
        end
        
        function set.Indices(obj,indices)
            if ~isempty(obj.ITableFactorFunction)
                obj.ITableFactorFunction.getFactorTable().changeIndices(indices);
            else
                obj.Indices = indices;
            end
        end
        
        function values = get.Weights(obj)
            if ~isempty(obj.ITableFactorFunction)
                values = obj.ITableFactorFunction.getFactorTable().getWeights();
            else
                values = obj.Weights;
            end
        end
        
        function set.Weights(obj,weights)
            if ~isempty(obj.ITableFactorFunction)
                obj.ITableFactorFunction.getFactorTable().changeWeights(weights);
            else
                obj.Weights = weights;
            end
        end
        %}
        function factorTable = get.TableFactor(obj)
           factorTable = FactorTable(obj.ITableFactorFunction.getFactorTable());
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
        
    end
    
    methods(Access=private)
        function constructFromITable(obj,itable)
            obj.ITableFactorFunction  = itable;
        end
        function constructFromNDimensionalArray(obj,values,domains)
            modeler = getModeler();            
            idomains = obj.domains2idomains(domains);            
            obj.ITableFactorFunction = modeler.createTableFactorFunction('table',values,idomains);            
        end
        function constructFromIndicesAndValues(obj,indices,values,domains)
            modeler = getModeler();            
            idomains = obj.domains2idomains(domains);            
            obj.ITableFactorFunction = modeler.createTableFactorFunction('table',indices,values,idomains);
        end
        
        function idomains = domains2idomains(obj,domains)
            idomains = cell(length(domains),1);
            
            for i = 1:length(domains)
                idomains{i} = domains{i}.IDomain;
            end

        end 
        
        function constructFromDomains(obj,domains)
            modeler = getModeler();            
            idomains = obj.domains2idomains(domains);            
            obj.ITableFactorFunction = modeler.createTableFactorFunction('table',idomains);
        end

    end
end
