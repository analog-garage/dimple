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

classdef DiscreteVariableBase < VariableBase
    methods
        function obj = DiscreteVariableBase(domain,varargin)
            obj = obj@VariableBase([],[]);
            
            if ~isa(domain,'Domain')
                domain = DiscreteDomain(domain);
            end
            
            
            
            if length(varargin) == 3 && strcmp('existing',varargin{1})
                
                VectorIndices = varargin{3};
                varMat = varargin{2};
                
                
            else
                modeler = getModeler();
                dims = [varargin{:}];
                if size(dims) == 1
                    varargin = {varargin{1}, varargin{1}};
                    dims = [varargin{:}];
                end
                
                numEls = prod(dims);
                
                varMat = modeler.createVariableVector(class(obj),domain.IDomain,numEls);
                
                VectorIndices = 0:(numEls-1);
                
                if (length(varargin) > 1)
                    VectorIndices = reshape(VectorIndices,varargin{:});
                end
                
                
            end
            
            obj.Domain = domain;
            obj.VectorObject = varMat;
            obj.VectorIndices = VectorIndices;
        end
            
    end
    
    methods(Access=protected)
        %These methods are used instead of property accessors because
        %property accessors can not be overriden.
        function setInput(obj,b)
            
            d = obj.Domain.Elements;
            v = obj.VectorIndices;
            
            
            if (numel(b) == numel(d))
                b = reshape(b,1,numel(d));
                b = repmat(b,numel(v),1);
                
                
            else
                
                if length(v) == numel(v)
                    v = reshape(v,numel(v),1);
                    
                    if ~all(size(b) == [length(v) length(d)])
                        error('dims must match');
                    end
                else
                    if numel(d) == 1
                        if ~all(size(b) == size(v))
                            error('dims must match');
                        end
                    else
                        if ~all(size(b) == [size(v)  numel(d)])
                            error('dims must match');
                        end
                    end
                    
                    b = reshape(b,numel(b)/length(d),length(d));
                end
            end
            
            varids = reshape(v,numel(v),1);
            
            obj.VectorObject.setInput(varids,b);
            
        end
        
        function b = getInput(obj)
            varids = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
            input = obj.VectorObject.getInput(varids);
            
            %TODO: reuse this code from getBeliefs
            b = zeros(numel(obj.VectorIndices),length(obj.Domain.Elements));
            
            for i = 1:size(input,1)
                b(i,:) = input(i,:);
            end
            v = obj.VectorIndices;
            
            %1x1 - Leave priors as is
            %1xN - Transpose
            %Nx1 - Leave as is
            %Anything else - Add extra dimension
            sz = size(v);
            isvector = numel(v) == length(v) && numel(v) > 1;
            isrowvector = isvector && sz(1) == 1;
            iscolvector = isvector && sz(2) == 1;
            
            if isscalar(v) || iscolvector
                
            elseif isrowvector
                b = b';
            else
                sz = size(v);
                sz = [sz numel(b)/numel(v)];
                b = reshape(b,sz);
                
            end
        end
        
        function b = getBelief(obj)
            
            v = obj.VectorIndices;
            varids = reshape(v,numel(v),1);
            
            b = obj.VectorObject.getDiscreteBeliefs(varids);
            
            %1x1 - Leave priors as is
            %1xN - Transpose
            %Nx1 - Leave as is
            %Anything else - Add extra dimension
            sz = size(v);
            isvector = numel(v) == length(v) && numel(v) > 1;
            isrowvector = isvector && sz(1) == 1;
            iscolvector = isvector && sz(2) == 1;
            
            if isscalar(v) || iscolvector
                
            elseif isrowvector
                b = b';
            else
                sz = size(v);
                sz = [sz numel(b)/numel(v)];
                b = reshape(b,sz);
                
            end
        end
        
        function values = getValue(obj)
            v = obj.VectorIndices;
            
            varIds = reshape(v,numel(v),1);
            
            valueIndices = obj.VectorObject.getValueIndices(varIds);
            values = obj.Domain.Elements(valueIndices);
            
            domainIsScalars = all(cellfun(@isscalar,obj.Domain.Elements));
            if domainIsScalars
                values = cell2mat(values);
            end;
            
            if numel(values) == numel(v)
                values = reshape(values,size(v));
            elseif numel(v) > 1
                sz = size(values);
                sz = sz(1:(end)-1);
                sz = [sz size(v)];
                values = reshape(values,sz);
            end
            
            if ~domainIsScalars && numel(values) == 1
                values = values{1};
            end
            
        end
        
        function setFixedValue(obj,value)
            domainIsScalars = all(cellfun(@isscalar,obj.Domain.Elements));
            if ~ domainIsScalars
                error('Only scalar domains currently supported');
            end
            
            values = MatrixObject.pack(value,obj.VectorIndices);
            
            values = repmat(values,1,numel(obj.Domain.Elements));
            domains = repmat(cell2mat(obj.Domain.Elements),size(values,1),1);
            [~,i] = max(values==domains, [], 2); % i will be the matching domain index
            fixedValueIndices = i - 1;           % Zero-based indices for Java
            varids = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
            obj.VectorObject.setFixedValueIndices(varids, fixedValueIndices);
        end
        
        function x = getFixedValue(obj)
            varids = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
            fixedValueIndices = obj.VectorObject.getFixedValueIndices(varids);
            fixedValues = obj.Domain.Elements(fixedValueIndices + 1);   % One-based indices
            fixedValues = reshape(fixedValues,numel(fixedValues),1);
            
            domainIsScalars = all(cellfun(@isscalar,obj.Domain.Elements));
            if domainIsScalars
                fixedValues = cell2mat(fixedValues);
            end;

            x = MatrixObject.unpack(fixedValues,obj.VectorIndices);
        end        
        
    end
    
    
end
