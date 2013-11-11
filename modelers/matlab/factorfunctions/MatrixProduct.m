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

function out = MatrixProduct(a, b, varargin)

fg = getFactorGraph();            % By default, use the current factor graph

% Parse optional arguments
for arg=varargin
    ar = arg{1};
    if (isa(ar, 'FactorGraph'))
        fg = ar;                   % Optional argument to specify the factor graph
    end
end

if (size(a,2) ~= size(b,1)); error('Incompatible dimensions'); end;
Nr = size(a,1);
Nx = size(a,2);
Nc = size(b,2);
outSize = {Nr Nc};

out = Real(outSize{:});

ff = FactorFunction('MatrixProduct', Nr, Nx, Nc);
fg.addFactor(ff, out, a, b);

end