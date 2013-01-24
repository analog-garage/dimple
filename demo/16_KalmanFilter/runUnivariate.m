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

%% Simple Kalman filter for the falling body
%% Equations: 
%% z_{k+1}=z_k+zdot_k+N(0,sigma_1)
%% zdot_{k+1}=zdot_k + Gt + N(0,sigma2)
%% where Gt is -9.8 m/s * sampling time
%% Observation:
%% y_k=z_k+N(0,sigma_3)

rand('seed',1);
randn('seed',1);

sigma1=0.1;
sigma2=0.1;
sigma3=100;
zstart=0;
zdotstart=0;

% running time: 5 seconds
% final speed ~50 m/s
% final altitude=122.5 m
% sampling interval=0.1s (50 samples)

sampling=50;
Gt=-9.8*0.1;

%%%
%generation of data
realZ=zeros(sampling,1);
realZdot=zeros(sampling,1);
realY=zeros(sampling,1);

realZ(1)=0;
realZdot(1)=0;

for k=2:sampling
   realZ(k)=realZ(k-1)+realZdot(k-1)+sigma1*randn();
   realZdot(k)=realZdot(k-1)+Gt+sigma2*randn(); 
end
realY=realZ+sigma3*randn(size(realZ));


% Creating Factor Graph

fg=FactorGraph();
fg.Solver='Gaussian';
Z=Real(sampling,1);
Zdot=Real(sampling,1);


% We could push the measurements Y into priors of Z
% but we choose to use a more natural approach
Y=Real(sampling,1);
for k=1:sampling
    Y(k).Input=[realY(k) 0];
end

% The gravity variable is replicated to make sure the graph is not loopy
Gvar=Real(1,1);
Gvar.Input=[Gt 0];


Noise1=Real(sampling,1);
Noise2=Real(sampling,1);
Noise3=Real(sampling,1);

for k=1:sampling
    Noise1(k).Input=[0 sigma1];
    Noise2(k).Input=[0 sigma2];
    Noise3(k).Input=[0 sigma3];
end
    


Z(1).Input=[0 0];
Zdot(1).Input=[0 0];

for k=2:sampling
   fg.addFactor(@add,Z(k),Z(k-1),Zdot(k-1),Noise1(k));
   fg.addFactor(@add,Zdot(k),Zdot(k-1),Gvar,Noise2(k));
   fg.addFactor(@add,Y(k),Z(k),Noise3(k));
end


fg.setScheduler(com.analog.lyric.dimple.schedulers.RandomWithoutReplacementScheduler());
fg.Solver.setNumIterations(200)
fg.solve;

results=zeros(sampling,1);
for k=1:sampling
    results(k)=Z(k).Belief(1);
end
plot(results);hold on; plot(realY,'r');hold on; plot(realZ,'go');
