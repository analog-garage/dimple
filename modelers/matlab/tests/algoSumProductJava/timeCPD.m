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

M = 4;
N = 5;
T = 1000;

ZDomains = cell(N,1);
for i = 1:N
    ZDomains{i} = num2cell(1:M);
end


[fg,y,a,zs] = makeCustomFactorCPD(ZDomains);
vars = struct('fg',fg,'y',y,'a',a,'zs',{zs},'type','custom factor','T',T);
[fg,y,a,zs] = makeMultiplexerCPD(ZDomains);
vars(2) = struct('fg',fg,'y',y,'a',a,'zs',{zs},'type','multiplexer CPD','T',T);
[fg,y,a,zs] = makeNestedMultiplexerCPD(ZDomains);
vars(3) = struct('fg',fg,'y',y,'a',a,'zs',{zs},'type','nested multiplexer CPD','T',T);
[fg,y,a,zs] = makeFullCPD(ZDomains);
vars(4) = struct('fg',fg,'y',y,'a',a,'zs',{zs},'type','full CPD','T',T);
[fg,y,a,zs] = makeCustomFactorCPD(ZDomains);
vars(5) = struct('fg',fg,'y',y,'a',a,'zs',{zs},'type','custom factor single update','T',T);
fg.Schedule = [{y,a} zs' {fg.Factors{1}}];

for i = 1:length(vars)
    vars(i).fg.NumIterations = T;
    tic
    vars(i).fg.solve();
    time = toc / T;
    fprintf('type: %s time:      %.2E\n',vars(i).type,time);
end