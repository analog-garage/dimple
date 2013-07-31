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

N = 5; %Number of possible sources
M = 5; %Domain of Zs and Y
T = 1000;

%Create Nested Graph version
ZDomains = cell(N,1);
for i = 1:N
   ZDomains{i} = cell(M,1);
   for j = 1:M
       ZDomains{i}{j} = j;
   end
end

[ng,Y,A,Zs,ZA] = buildMultiplexorCPD(ZDomains);

%Create Custom Factor Version
CF_FG = FactorGraph();
CF_Y = Discrete(1:M);
CF_A = Discrete(1:N);
CF_Z = Discrete(1:M,N,1);

CF_FG.addFactor(@multiplexorCPD,CF_Y,CF_A,CF_Z);

%Create full version
y = Discrete(1:M);
a = Discrete(1:N);
z = Discrete(1:M,N,1);

fg = FactorGraph();

func = @(y,a,z) y == z(a);
fg.addFactor(func,y,a,z);

%Test downard message
aInput = rand(N,1);
zInput = rand(N,M);
zInput = zInput ./ repmat(sum(zInput,2),1,M);
a.Input = aInput;
z.Input = zInput;
CF_A.Input = aInput;
CF_Z.Input = zInput;

for i = 1:N
   Zs{i}.Input = zInput(i,:); 
end
A.Input = aInput;

ng.NumIterations = T;
fg.NumIterations = T;
CF_FG.NumIterations = T;
ng.initialize();
fg.initialize();
CF_FG.initialize();

disp('nested graph');
tic
ng.solve();
disp(toc / T);

disp('brute force');
tic
fg.solve();
disp(toc / T);

disp('custom factor');
tic
CF_FG.solve();
disp(toc / T);

aInput = a.Input;
zInput = z.Input;
belief = y.Belief;


py = zeros(M,1);

disp('MATLAB');
tic
for i = 1:M
    sm = 0;
    for j = 1:N
        sm = sm + aInput(j)*zInput(j,i);
    end
    py(i) = sm;
end
disp(toc);

%{

py = py / sum(py);
%disp('belief of Y for custom math');
assertTrue(norm(belief-py') < 1e-15);
%disp('belief of Y for nested graph');
assertTrue(norm(Y.Belief-py') < 1e-15);
%disp('belief of Y for custom factor');
assertTrue(norm(CF_Y.Belief-py') < 1e-15);



%Test message to a
yInput = rand(M,1);
a.Input = ones(N,1);
y.Input = yInput;
z.Input = zInput;

Y.Input = yInput;
A.Input = a.Input;
CF_A.Input = a.Input;
CF_Y.Input = yInput;

ng.solve();
fg.solve();
CF_FG.solve();

abelief = zeros(N,1);

for i = 1:N
    sm = 0;
    for j = 1:M
        sm = sm + yInput(j) * zInput(i,j);
    end
    abelief(i) = sm;
end


abelief = abelief / sum(abelief);
%disp('ABelief compared to math');
assertTrue(norm(a.Belief-abelief') < 1e-15);
%disp('ABelief compared to nested graph');
assertTrue(norm(A.Belief-abelief') < 1e-15);
%disp('ABelief compared to custom factor');
assertTrue(norm(CF_A.Belief-abelief') < 1e-15);

%Test message to zi
aInput = rand(N,1);
a.Input = aInput;
A.Input = aInput;
CF_A.Input = aInput;

%disp('Calculating messages to Z');

for x = 1:N
    zInput = rand(N,M);
    zInput = zInput ./ repmat(sum(zInput,2),1,M);
    zInput(x,:) = ones(1,M);
    z.Input = zInput;
    CF_Z.Input = zInput;
    
    for i = 1:N
       Zs{i}.Input = zInput(i,:); 
    end
       
    
    
    fg.solve();
    ng.solve();
    CF_FG.solve();
    
    
    %calculate in MATLAB
    zbelief = zeros(M,1);
    
    offset = 0;
    for j = 1:N
        if j ~= x
            for k = 1:M
                offset = offset + aInput(j) * yInput(k) * zInput(j,k);
            end
        end
    end
    
    
    for i = 1:M
        sm = aInput(x) * yInput(i)+ offset;
        zbelief(i) = sm;
    end
    
    zbelief = zbelief / sum(zbelief);
    %disp('math against brute force');
    assertTrue(norm(z(x).Belief - zbelief') < 1e-15);
    %disp('Nested Graph');
    assertTrue(norm(Zs{x}.Belief - zbelief') < 1e-15);
    %disp('Custom Factor');
    assertTrue(norm(CF_Z(x).Belief - zbelief') < 1e-15);
end

%}