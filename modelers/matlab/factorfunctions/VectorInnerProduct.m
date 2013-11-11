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

function out = VectorInnerProduct(a, b, varargin)

fg = getFactorGraph();            % By default, use the current factor graph

% Parse optional arguments
for arg=varargin
    ar = arg{1};
    if (isa(ar, 'FactorGraph'))
        fg = ar;                   % Optional argument to specify the factor graph
    end
end

if (isa(a,'RealJoint'))
    aSize = a.Domain.NumElements;
else
    aSize = length(a);
end
if (isa(b,'RealJoint'))
    bSize = b.Domain.NumElements;
else
    bSize = length(b);
end
if (aSize ~= bSize); error('Incompatible dimensions'); end;

out = Real();

fg.addFactor('VectorInnerProduct', out, a, b);

end