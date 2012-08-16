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

%% Second example for an equalizer

%% Model: X_i, i=1..n are generated from [-1;1] with probability 1/2
%% (BPSK modulation)
%% Observations:
%% Y_i = a_1 X_{i-1} + a_2 X_i + a_3 X_{i+1} + N(0,\sigma^2)
%% Goal: from measurements Y_i, find most likely values for X_i

%% Equalizer is loopy, so does not give MAP performance
%% Observations Y_i are discretized and appear explicitely in the model

%% Slightly poorer performance than the loopy equalizer, but much improved speed
%% since the factor graph is generated only once per SNR point



N=256;  %number of variables. Boundary conditions: X_0=257=1
sigma=0.4;

argmax=@(v) find(v==max(v));
argmin=@(v) find(v==min(v));

%% Generating template problems first to find statistics of measurements Y
mask=[0.3 1 0.3];
BPSK=[-1;1];


realYstat=[];

for k=1:100
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
    
    realYstat=[realYstat realY];
end

%% Construction of Y's discretization

numpoints=40; %number of distinct values Y can take
pp=0:100/(numpoints-1):100;
Ydomain=percentile(realYstat,pp);



% Factor Graph

fg=FactorGraph();

X=Variable([-1;1],1,N);
Y=Variable(Ydomain,1,N);
Xboundary=Variable([-1;1],1,1);
Xboundary.Input=[0;1];
%boundary conditions

fg.addFactor(@softjoint,mask,sigma,[Xboundary X(1)  X(2)],realY(1));
fg.addFactor(@softjoint,mask,sigma,[X(N-1)  X(N)  Xboundary],realY(N));


for i=2:N-1
    fg.addFactor(@softjoint,mask,sigma,X(i-1:i+1),Y(i));
end


numtrials=30;
tic;
fprintf('Running %d trials\n',numtrials); 
for k=1:numtrials
    
    fprintf('Trial number:%d\n',k);

    %Problem generation
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
    
    
    
    
    for i=1:N
        idx=argmin(abs(Ydomain-realY(i)));
        iin=zeros(1,length(Ydomain));
        iin(idx)=1;
        Y(i).Input=iin;
    end
    
    fg.Solver = 'SumProduct';
    fg.Solver.iterate(100);
    
    succ_final=sum(X.Value==realX);
    successes(k)=succ_final;
end
toc;
fprintf('Final BER:%d\n',(N-mean(successes))/N);
fprintf('Throughput: %d bits per second\n',N*numtrials/toc);




