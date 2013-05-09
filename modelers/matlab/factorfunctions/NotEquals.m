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

function indicatorVar = NotEquals(varargin)

fg = getFactorGraph();            % Use the current factor graph

% Get size from arguments
vectorize = false;
outSize = 1;                      % By default, result is a single variable
for arg=varargin
    a = arg{1};
    if (~isscalar(a))
        sizeA = size(a);
        if (vectorize && any(outSize ~= sizeA))
            error('Array inputs much have identical dimensions');
        end
        vectorize = true;
        outSize = sizeA;
    end
end
outSize = num2cell(outSize);

indicatorVar = Bit(outSize{:});

if (vectorize)
    fg.addFactorVectorized('NotEquals', indicatorVar, varargin{:});
else
    fg.addFactor('NotEquals', indicatorVar, varargin{:});
end

end