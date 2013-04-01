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

classdef Real < VariableBase
    methods
        function obj = Real(varargin)
            obj@VariableBase([],[]);
            
            numArgs = length(varargin);
            
            if length(varargin) == 4 && strcmp('existing',varargin{2})
                obj.Domain = varargin{1};
                obj.VectorObject = varargin{3};
                obj.VectorIndices = varargin{4};
            else
                
                if (numArgs == 0)
                    varargin = {1};
                end
                
                varargIndex = 1;
                arg = varargin{varargIndex};
                if (isnumeric(arg) && (length(arg) == 2))
                    domain = RealDomain(arg(1),arg(2));
                    varargIndex = varargIndex + 1;
                else
                    domain = RealDomain(-Inf,Inf);
                end
                obj.Domain = domain;
                if (varargIndex > length(varargin))
                    varargin = [varargin {1}];
                end
                
                
                arg = varargin{varargIndex};
                if (isa(arg, 'function_handle'))
                    input = func2str(arg);
                    varargIndex = varargIndex + 1;
                elseif (ischar(arg))
                    input = arg;
                    varargIndex = varargIndex + 1;
                elseif (isa(arg, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    input = arg;
                    varargIndex = varargIndex + 1;
                elseif (isa(arg, 'FactorFunction'))
                    input = arg.get();
                    varargIndex = varargIndex + 1;
                elseif (iscell(arg))
                    input = FactorFunction(arg{:}).get();
                    varargIndex = varargIndex + 1;
                else
                    input = [];
                end
                if (varargIndex > length(varargin))
                    varargin = [varargin {1}];
                end
                
                dimArgs = varargin(varargIndex:end);
                
                modeler = getModeler();
                dims = [dimArgs{:}];
                if size(dims) == 1
                    dimArgs = {dimArgs{1}, dimArgs{1}};
                    dims = [dimArgs{:}];
                end
                
                numEls = prod(dims);
                
                VectorObject = modeler.createRealVariableVector(class(obj),domain.IDomain,input,numEls);
                
                obj.VectorObject = VectorObject;
                
                obj.VectorIndices = 0:(numEls-1);
                
                if (length(dimArgs) > 1)
                    obj.VectorIndices = reshape(obj.VectorIndices,dimArgs{:});
                end
                obj.Input = input;
                
                
            end
            
        end
        
        
        
        
    end
    
    methods (Access=protected)
        function var = createObject(obj,vectorObject,VectorIndices)
            var = Real(obj.Domain,'existing',vectorObject,VectorIndices);
        end
        
        
        function setInput(obj,factorFunction)
            if (isa(factorFunction, 'FactorFunction'))
                factorFunction = factorFunction.get();
            elseif (iscell(factorFunction))
                factorFunction = FactorFunction(factorFunction{:}).get();
            end
            
            v = obj.VectorIndices;
            varids = reshape(v,numel(v),1);
            obj.VectorObject.setInput(varids,factorFunction);
            
        end
        function retval = getInput(obj)
            varids = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
            b = cell(obj.VectorObject.getInput(varids));
            
            v = obj.VectorIndices;
            
            %1x1 - Leave priors as is
            %1xN - Transpose
            %Nx1 - Leave as is
            %Anything else - Add extra dimension
            sz = size(v);
            isvector = numel(v) == length(v) && numel(v) > 1;
            isrowvector = isvector && sz(1) == 1;
            iscolvector = isvector && sz(2) == 1;
            
            if isscalar(v)
                retval = b{1};
            elseif isrowvector
                retval = b';
            elseif iscolvector
                retval = b;
            else
                sz = size(v);
                sz = [sz numel(b)/numel(v)];
                retval = reshape(b,sz);
            end
        end
        function b = getBelief(obj)
            sz = size(obj);
            
            b = cell(sz);
            
            a = cell(obj.VectorObject.getBeliefs(obj.VectorIndices));
            
            if prod(sz) == 1
                b = a{1};
            else
                for i = 1:numel(b)
                    b{i} = a{i};
                end
            end
        end
        
        function v = getValue(obj)
            error('not implemented');
        end

        function setFixedValue(obj,value)
           fixedValues = MatrixObject.pack(value,obj.VectorIndices);
           fixedValues = reshape(fixedValues,numel(fixedValues),1);
           varids = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
           obj.VectorObject.setFixedValues(varids, fixedValues);
        end
        
        function x = getFixedValue(obj)
            varids = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
            fixedValues = obj.VectorObject.getFixedValues(varids);
            x = MatrixObject.unpack(fixedValues,obj.VectorIndices);
        end        

    end
    
    
end
