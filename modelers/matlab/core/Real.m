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
    properties 
        Input;
        Belief;
    end
    methods
        function obj = Real(varargin)
            numArgs = length(varargin);
            
            if length(varargin) == 4 && strcmp('existing',varargin{2})
                obj.Domain = varargin{1};
                obj.VarMat = varargin{3};
                obj.Indices = varargin{4};
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
                elseif (isa(arg, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    input = arg;
                    varargIndex = varargIndex + 1;
                elseif (ischar(arg))
                    input = arg;
                    varargIndex = varargIndex + 1;
                else
                    input = [];
                end
                obj.Input = input;
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
                
                varMat = modeler.createRealVariableVector(class(obj),domain.IDomain,input,numEls);
                    
                obj.VarMat = varMat;
                
                obj.Indices = 0:(numEls-1);
                
                if (length(dimArgs) > 1)
                    obj.Indices = reshape(obj.Indices,dimArgs{:});
                end
                                
            end
                        
        end
        
        function x = get.Input(obj)
            x = obj.getInput();
        end
        function x = get.Belief(obj)
            x = obj.getBelief();
        end
        
        function var = createVariable(obj,domain,varMat,indices)
            var = Real(domain,'existing',varMat,indices);
        end
    end
    
    methods (Access=protected)
        

        function b = getBelief(obj)
            sz = size(obj);            
            
            b = cell(sz);
            
            a = cell(obj.VarMat.getBeliefs(obj.Indices));

            if prod(sz) == 1
                b = a{1};
            else
                for i = 1:numel(b)
                   b{i} = a{i}; 
                end
            end
            
        end
        
        function setInput(obj,factorFunction)
            v = obj.Indices;
            varids = reshape(v,numel(v),1);
            obj.VarMat.setInput(varids,factorFunction);
        end
        
        function retval = getInput(obj)
            varids = reshape(obj.Indices,numel(obj.Indices),1);
            b = cell(obj.VarMat.getInput(varids));
            
            v = obj.Indices;
            
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
        
        
        function values = getValue(obj)
            % TODO: Not implemented yet  ******************************
            values = 0;
        end
                
    end
    
end
