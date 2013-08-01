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

function [fg, y, a, zs] = makeFullCPD(ZDomains)

    N = length(ZDomains);
    y = Discrete(MultiplexerCPD(ZDomains{:}).Y.Domain);
    a = Discrete(1:N);
    zs = cell(N,1);
    for i = 1:N
        zs{i} = Discrete(ZDomains{i});
    end

    fg = FactorGraph();

    fg.addFactor(@myMultiplexorTestFunc,y,a,zs{:});

end