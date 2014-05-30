%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2014 Analog Devices, Inc.
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

function var = Poisson(lambda, maxK, varargin)

% The argument maxK must be an integer value that specifies the
% maximum likely value of the output variable, K.  If lambda
% is fixed, or if its expected maximum is known, then maxK
% should be a multiple of this (if maxK is 5 times larger than
% the largest value of lambda, then less than 0.1% of the 
% probability mass would be greater than maxK).

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

varDomain = 0:round(maxK);
var = Discrete(varDomain, outSize{:});
if (isa(lambda,'VariableBase'))
    fg.addFactorVectorized('Poisson', lambda, var);     % Variable lambda
else
    fg.addFactorVectorized({'Poisson', lambda}, var);   % Constant lambda
end

end