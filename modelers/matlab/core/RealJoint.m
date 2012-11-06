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
                
                if numel(varargin) ~= 1
                    raise('only support one arg for now');
                end
                domain = RealJointDomain(varargin{1});
                obj.Domain = domain;
                numEls = 1;
                modeler = getModeler();
                varMat = modeler.createRealJointVariableVector(class(obj),domain.IDomain,numEls);
                obj.VectorObject = varMat;
                obj.VectorIndices = 0;
            end
            
        end
        
    end
    
    methods (Access=protected)
        
        function var = createObject(obj,varMat,VectorIndices)
            var = RealJoint(obj.Domain,'existing',varMat,VectorIndices);
        end
        
        
        function x = getValue(obj)
            error('not implemented');
        end
        
        function x = getInput(obj)
            error('not implemented');
        end
        function b = getBelief(obj)
            sz = size(obj);
            
            b = cell(sz);
            
            a = cell(obj.VectorObject.getBeliefs(obj.VectorIndices));
            
            if prod(sz) == 1
                m = MultivariateMsg(0,0);
                m.IMsg = a{1};
                b = m;
            else
                for i = 1:numel(b)
                    m = MultivariateMsg(0,0);
                    m.IMsg = a{i};
                    b{i} = m;
                end
            end
        end
        
        function setInput(obj,input)
            v = obj.VectorIndices;
            
            if isa(input,'Msg')
                input = input.IMsg;
            end
            
            varids = reshape(v,numel(v),1);
            obj.VectorObject.setInput(varids,input);
        end
        
    end
    
end
