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

function outVector = MatrixVectorProduct(a, b, varargin)

fg = getFactorGraph();            % By default, use the current factor graph

% Parse optional arguments
for arg=varargin
    ar = arg{1};
    if (isa(ar, 'FactorGraph'))
        fg = ar;                   % Optional argument to specify the factor graph
    end
end

if ((nnz(size(a)>1)==2) && (nnz(size(b)>1)==1))         % A*b
    matrix = a;
    inVector = b;
    inLength = size(matrix,2);
    outLength = size(matrix,1);
    if (inLength~=length(inVector)); error('Incompatible dimensions'); end;
    outSize = {outLength 1};
elseif ((nnz(size(a)>1)==1) && (nnz(size(b)>1)==2))     % a*B
    matrix = b';
    inVector = a';
    inLength = size(matrix,2);
    outLength = size(matrix,1);
    if (inLength~=length(inVector)); error('Incompatible dimensions'); end;
    outSize = {1 outLength};
else
    error('Inavlid dimensions: only matrix*vector or vector*matrix supported');
end

outVector = Real(outSize{:});

ff = FactorFunction('MatrixVectorProduct', inLength, outLength);
fg.addFactor(ff, outVector, matrix, inVector);

end