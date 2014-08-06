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

classdef RealStream < VariableStreamBase 
    methods
        function obj = RealStream(varargin)
            
            % Default arguments (unbounded domain, dimension 1x1)
            if (isempty(varargin))
                varargin = {1};
            end
            
            % First argument may be a domain
            varargIndex = 1;
            arg = varargin{varargIndex};
            if (isnumeric(arg) && (length(arg) == 2))
                domain = RealDomain(arg(1),arg(2));
                varargIndex = varargIndex + 1;
            elseif isa(arg, 'RealDomain')
                domain = arg;
                varargIndex = varargIndex + 1;
            else
                domain = RealDomain(-Inf,Inf);
            end
            if (varargIndex > length(varargin))
                varargin = [varargin {1}];
            end
            
            % Remaining arguments are array dimension
            dimArgs = varargin(varargIndex:end);
            dims = [dimArgs{:}];
            if size(dims) == 1
                dimArgs = {dimArgs{1}, dimArgs{1}};
                dims = [dimArgs{:}];
            end
                                    
            model = getModeler();            
            IDiscreteStream = model.createRealStream(domain.IDomain,prod(dims));
            
            obj@VariableStreamBase(IDiscreteStream,dims);
            obj.Domain = domain;

        end
    end
end
