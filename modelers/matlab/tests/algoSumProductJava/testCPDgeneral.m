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

function testCPDgeneral


%Try different domains for Z

%N = 3; %Number of possible sources
ZDomains = {{1,2},{2,3},{3,4,5}};
N = length(ZDomains);

%Create Nested Graph version
[ng,Y,A,Zs,ZA] = buildMultiplexorCPD(ZDomains);

%Create Custom Factor Version
CF_FG = FactorGraph();
CF_Y = Discrete(Y.Domain);
CF_A = Discrete(1:N);
CF_Zs = cell(N,1);
for i = 1:N
    CF_Zs{i} = Discrete(ZDomains{i});
end

CF_FG.addFactor(@multiplexorCPD,CF_Y,CF_A,CF_Zs{:});

%Create full version
y = Discrete(Y.Domain);
a = Discrete(1:N);
zs = cell(N,1);
for i = 1:N
    zs{i} = Discrete(ZDomains{i});
end

fg = FactorGraph();

fg.addFactor(@myMultiplexorTestFunc,y,a,zs{:});

%Test downard message
aInput = rand(N,1);
zInputs = cell(N,1);
for i = 1:N
   zInputs{i} = rand(1,length(ZDomains{i})); 
   zInputs{i} = zInputs{i} / sum(zInputs{i});
end
    
a.Input = aInput;

for i = 1:N
   zs{i}.Input = zInputs{i}; 
end

CF_A.Input = aInput;

for i = 1:N
    CF_Zs{i}.Input = zInputs{i};
end

for i = 1:N
   Zs{i}.Input = zInputs{i};
end

A.Input = aInput;


ng.solve();
fg.solve();
CF_FG.solve();

belief = y.Belief;


assertTrue(norm(Y.Belief-belief) < 1e-15);
assertTrue(norm(CF_Y.Belief-belief) < 1e-15);


%Test message to a
yInput = rand(length(y.Domain.Elements),1);
a.Input = ones(N,1);
y.Input = yInput;
%z.Input = zInput;

Y.Input = yInput;
A.Input = a.Input;
CF_A.Input = a.Input;
CF_Y.Input = yInput;

ng.solve();
fg.solve();
CF_FG.solve();


assertTrue(norm(A.Belief-a.Belief) < 1e-15);
assertTrue(norm(a.Belief-CF_A.Belief) < 1e-15);

%Test message to zi
aInput = rand(N,1);
a.Input = aInput;
A.Input = aInput;
CF_A.Input = aInput;

%disp('Calculating messages to Z');

for x = 1:N
    for i = 1:length(zInputs)
        zInputs{i} = rand(1,length(ZDomains{i}));
        zInputs{i} = zInputs{i} / sum(zInputs{i});
        
        zs{i}.Input = zInputs{i};
        CF_Zs{i}.Input = zInputs{i};
        Zs{i}.Input = zInputs{i};
    end
    
    fg.solve();
    ng.solve();
    CF_FG.solve();
    
    
    assertTrue(norm(zs{x}.Belief - Zs{x}.Belief) < 1e-15);
    assertTrue(norm(Zs{x}.Belief - CF_Zs{x}.Belief) < 1e-15);
end

