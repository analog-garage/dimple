%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
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

classdef RealJointStream < VariableStreamBase
    methods
        function obj = RealJointStream(varargin)
            
            % First argument can be either a RealJointDomain or
            % the number of dimensions in the joint varaible
            if (isa(varargin{1}, 'RealJointDomain'));
                domain = varargin{1};
            else
                domain = RealJointDomain(varargin{1});
            end
            nextArg = 2;

            % Determine the size of the array of RealJoint variables
            if nargin < nextArg
                dims = [1 1];
            else
                dimArgs = varargin(nextArg:end);
                dims = [dimArgs{:}];
                if size(dims) == 1
                    dimArgs = {dimArgs{1}, dimArgs{1}};
                    dims = [dimArgs{:}];
                end
            end              
            
            model = getModeler();
            stream = model.createRealJointStream(domain.IDomain,prod(dims));
            
            obj@VariableStreamBase(stream,dims);
            obj.Domain = domain;
        end
    end
    
    methods(Access=protected)
        function sink = getDataSink(obj)
            sink = MultivariateDataSink(obj.IVariableStream.getDataSink());
        end
        
    end
    
end
