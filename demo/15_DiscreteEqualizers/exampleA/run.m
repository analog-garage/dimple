%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

%% Basic example for an equalizer

%% Model: X_i, i=1..n are generated from [-1;1] with probability 1/2
%% (BPSK modulation)
%% Observations:
%% Y_i = a_1 X_{i-1} + a_2 X_i + a_3 X_{i+1} + N(0,\sigma^2)
%% Goal: from measurements Y_i, find most likely values for X_i

%% Equalizer is loopy, so does not give MAP performance
%% Observations Y_i do not explicitely appear in the model:
%% they are 'demapped' (i.e. incorporated into a factor function)

%% This is the simplest approach, but it requires creating a new factor
%% graph for each set of observations, so it's too slow.
global mask sigma

N=256;  %number of variables. Boundary conditions: X_0=X_257=1
sigma=0.4;


% generating the problem first

mask=[0.3 1 0.3];
BPSK=[-1;1];
realX=BPSK(randi([1 2],1,N))';

realY=zeros(1,N);

%boundary condition first:
localX=[1 realX(1) realX(2)];
realY(1)=sum(localX.*mask);
localX=[realX(N-1) realX(N)  1];
realY(N)=sum(localX.*mask);

%other points
for i=2:N-1
       localX=realX(i-1:i+1);
       val=(sum(localX.*mask));
       realY(i)=val;
end
realG=sigma*randn(1,N);
realY=realY+realG;

error_init=sum((-1+2*(realY>0))~=realX);
fprintf('Initial number of errors:%d\n',error_init);


% Factor Graph

fg=FactorGraph();
tic;
X=Variable([-1;1],1,N);

%creating a boundary variable which always takes value 1
Xboundary=Variable([-1;1],1,1);
Xboundary.Input=[0 1];

%boundary conditions
fg.addFactor(@softjoint,[Xboundary  X(1)  X(2)],realY(1));
fg.addFactor(@softjoint,[X(N-1)  X(N)  Xboundary],realY(N));


for i=2:N-1
    fg.addFactor(@softjoint,X(i-1:i+1),realY(i));
end

fg.Solver = 'SumProduct';
fg.Solver.iterate(100); 
tput=N/toc;
error_final=sum(X.Value~=realX);
fprintf('Final number of errors:%d\n',error_final);
fprintf('Throughput: %d bits per second\n',tput);

