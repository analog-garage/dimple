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

function var = If(condition, trueValue, falseValue, varargin)

fg = getFactorGraph();            % By default, use the current factor graph

% Parse optional arguments
for arg=varargin
    a = arg{1};
    if (isa(a, 'FactorGraph'))
        fg = a;                   % Optional argument to specify the factor graph
    end
end

if isa(trueValue, 'VariableBase')
    var = VariableBase.createFrom(trueValue);
elseif isa(falseValue, 'VaraibleBase')
    var = VariableBase.createFrom(falseValue);
else
    error('At least one of the input values must be a variable');
end

if ~isa(condition, 'Bit')
    if ~(isa(condition, 'Discrete') ...
            && condition.Domain.NumElements == 2 ...
            && condition.Domain.Elements{1} == 0 ...
            && condition.Domain.Elements{2} == 1)
        error('Condition must be a Bit variable');
    end
end
if ~(all(size(condition) == size(var)) || prod(size(condition)) == 1)
    error('Condition input must either be a scalar or the same dimensions as the other inputs');
end    

fg.addFactorVectorized('Multiplexer', var, condition, falseValue, trueValue);
    
end