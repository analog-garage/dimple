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

classdef RealJoint < VariableBase
    
    
    methods
        function obj = RealJoint(varargin)
            obj@VariableBase([],[]);
            %first arg can be a number
            %or it can be a domain
            %for now, only take one argument
            
            
            if length(varargin) == 4 && strcmp('existing',varargin{2})
                obj.Domain = varargin{1};
                obj.VectorObject = varargin{3};
                obj.VectorIndices = varargin{4};
            else
                
                % First argument can be either a RealJointDomain or
                % the number of dimensions in the joint varaible
                if (isa(varargin{1}, 'RealJointDomain'));
                    domain = varargin{1};
                    nextArg = 2;
                elseif (isnumeric(varargin{1}) && (nargin > 1) && isa(varargin{2}, 'RealJointDomain'))
                    % This case is for Complex, where size 2 is always
                    % passed as the first argument
                    domain = varargin{2};
                    if (varargin{1} ~= domain.NumElements);
                        error('Number of domain elements must match the number of joint variable elements');
                    end
                    nextArg = 3;
                else
                    domain = RealJointDomain(varargin{1});
                    nextArg = 2;
                end
                obj.Domain = domain;
               
                % Determine the size of the array of RealJoint variables
                if nargin < nextArg
                    dims = 1;
                else
                    dimArgs = varargin(nextArg:end);
                    dims = [dimArgs{:}];
                    if size(dims) == 1
                        dimArgs = {dimArgs{1}, dimArgs{1}};
                        dims = [dimArgs{:}];
                    end
                end
                
                numEls = prod(dims);
                modeler = getModeler();
                varMat = modeler.createRealJointVariableVector(class(obj),domain.IDomain,numEls);
                obj.VectorObject = varMat;
                obj.VectorIndices = 0:(numEls-1);
                if length(dims)==1
                    dims = [dims 1];
                end
                obj.VectorIndices = reshape(obj.VectorIndices,dims);
            end
            
        end
        
    end
    
    methods (Access=protected)
        
        function var = createObject(obj,varMat,VectorIndices)
            var = RealJoint(obj.Domain,'existing',varMat,VectorIndices);
        end
        
        function b = getBelief(obj)
            sz = size(obj);
            
            b = cell(sz);
            
            varids = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
            a = cell(obj.VectorObject.getBeliefs(varids));
            
            if (isa(a{1}, 'com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters'))
                if prod(sz) == 1
                    m = MultivariateNormalParameters(0,0);
                    m.IParameters = a{1};
                    b = m;
                else
                    for i = 1:numel(b)
                        m = MultivariateNormalParameters(0,0);
                        m.IParameters = a{i};
                        b{i} = m;
                    end
                end
            else % A different form of beleif
                if prod(sz) == 1
                    b = a{1};
                else
                    for i = 1:numel(b)
                        b{i} = a{i};
                    end
                end
            end
        end
        
        
        function v = getValue(obj)
            varids = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
            values = obj.VectorObject.getValues(varids);
            v = MatrixObject.unpack(values,obj.VectorIndices);
        end
        

        
        function setInput(obj,input)
            v = obj.VectorIndices;
            
            if isa(input,'ParameterizedMessage')
                input = input.IParameters;
            elseif (isa(input, 'FactorFunction'))
                input = input.get();
            elseif iscell(input)
                if (isa(input{1}, 'FactorFunction'))
                    for i=1:length(input)
                        input{i} = input{i}.get();
                    end
                elseif (iscell(input{1}))
                    for i=1:length(input)
                        input{i} = FactorFunction(input{i}{:}).get();
                    end
                elseif (ischar(input{1}))
                    input = FactorFunction(input{:}).get();
                end
            end
            
            varids = reshape(v,numel(v),1);
            obj.VectorObject.setInput(varids,input);
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
        
        function setFixedValue(obj,value)
           fixedValues = MatrixObject.pack(value,obj.VectorIndices);
           arraySize = prod(size(obj.VectorIndices));
           numElements = obj.Domain.NumElements;
           fixedValues = reshape(fixedValues,arraySize,numElements);
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
