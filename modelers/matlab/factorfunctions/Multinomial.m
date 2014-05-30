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

function var = Multinomial(N, alphas, varargin)

fg = getFactorGraph();            % By default, use the current factor graph
outSize = {1};                    % By default, result is a single variable

% Parse optional arguments
for arg=varargin
    a = arg{1};
    if (isa(a, 'FactorGraph'))
        fg = a;                   % Optional argument to specify the factor graph
    elseif (isnumeric(a) && all(round(a)==a))
        outSize = num2cell(a);    % Optional argument to specify output variable dimensions
    end
end

if (isa(alphas, 'RealJoint'))
    dim = alphas.Domain.NumElements;
    joint = true;
elseif (isnumeric(alphas))
    dim = numel(alphas);
    joint = true;
elseif (isa(alphas, 'Real'))
    dim = prod(size(alphas));     % numel not supported for variable arrays
    joint = false;
elseif (iscell(alphas))
    dim = numel(alphas);
    joint = false;
else
    error('Unknown type of alpha parameters');
end
    
if (isa(N,'VariableBase'))
    maxN = max(cell2mat(N.Domain.Elements));
    varDomain = 0:maxN; % N is variable, so output can range from 0 to maximum possible value of N
    var = Discrete(varDomain, outSize{:}, dim);
    if (numel(outSize)==1 && outSize{1}==1)
        varArgument = {var,[]};
    else
        varArgument = {var,1:numel(outSize)};   % Vectorize over all but last dimension
    end
    
    if (joint)
        fg.addFactorVectorized('Multinomial', N, alphas, varArgument);
    elseif iscell(alphas)
        fg.addFactorVectorized({'MultinomialUnnormalizedParameters', dim}, N, alphas{:}, varArgument);
    else
        fg.addFactorVectorized({'MultinomialUnnormalizedParameters', dim}, N, {alphas,[]}, varArgument);
    end
else
    varDomain = 0:N;    % N is constant, so output can range from 0 to N
    var = Discrete(varDomain, outSize{:}, dim);
    if (numel(outSize)==1 && outSize{1}==1)
        varArgument = {var,[]};
    else
        varArgument = {var,1:numel(outSize)};   % Vectorize over all but last dimension
    end

    if (joint)
        fg.addFactorVectorized({'Multinomial', N}, alphas, varArgument);
    elseif iscell(alphas)
        fg.addFactorVectorized({'MultinomialUnnormalizedParameters', dim, N}, alphas{:}, varArgument);
    else
        fg.addFactorVectorized({'MultinomialUnnormalizedParameters', dim, N}, {alphas,[]}, varArgument);
    end
end

end