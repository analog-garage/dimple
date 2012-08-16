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

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Set parameters
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 
N = 200;
sigma = .3;
 
%create taps of filter
%taps = [1 .7 .2 .2];
taps = [1 .7 .2 .4];
%taps = rand(1,4);
 
%generate data
data = randi(2,N,1)*2-3;
 
%run through filter
y = conv(data,taps,'valid');
 
%add noise
noisy_y = y+randn(size(y,1),1)*sigma;
 
%build A
%TODO: should I use auto correlation and cross corelation instead?
A = zeros(length(data)-length(taps)+1,length(taps));
for i = length(taps):N
    A(i-length(taps)+1,:) = data(i:-1:i-length(taps)+1);
end
 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Solve using normal equation
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 
%min(Ax-b)^2
%Ax-b == 0
tic
x = (A'*A)^-1*A'*noisy_y;
toc
diff = norm(taps'-x)
taps1 = x;
 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Solve using simple factor graph
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
fg = FactorGraph();
fg.Solver = 'gaussian';
yv = RealJoint(length(noisy_y));
xv = RealJoint(length(taps));
fg.addFactor(@constmult,yv,A,xv);
 
means = noisy_y;
covar = eye(length(noisy_y))*sigma^2;
yv.Input = MultivariateMsg(means,covar);
 
%TODO: why is this so slow?
tic
fg.solve();
toc
 
diff2 = norm(xv.Belief.Means-taps')
taps2 = xv.Belief.Means;
 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Solve using RLS with Dimple
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
fg = FactorGraph();
fg.Solver = 'gaussian';
xv = cell(length(y),1);
yv = cell(length(y),1);
%noisy_yv = cell(length(y),1);
noise = cell(length(y),1);
for i = 1:length(xv)
    xv{i} = RealJoint(length(taps));
    yv{i} = RealJoint(1);
    %noisy_yv{i} = RealJoint(1);
    noise{i} = RealJoint(1);
    
    %TODO: what would happen with noise transpose?
    %fg.addFactor(@add,noisy_yv{i},noise{i},yv{i});
    fg.addFactor(@constmult,yv{i},A(i,:),xv{i});
    if i >= 2
       fg.addFactor(@constmult,xv{i},eye(length(taps)),xv{i-1}); 
    end
    
    %noise{i}.Input = MultivariateMsg(0,sigma^2);
    %moisy_yv{i}.Input = MultivariateMsg(
    yv{i}.Input = MultivariateMsg(noisy_y(i),sigma^2);
end
 
tic
fg.solve();
toc
 
%TODO: support data source for constants
diffs = zeros(length(xv),1);
for i = 1:length(xv)
    diffs(i) = norm(xv{i}.Belief.Means-taps');
end
taps3 = xv{length(xv)}.Belief.Means;
taps4 = xv{1}.Belief.Means

alltaps = [taps' taps1 taps2 taps3 taps4];

%Use optimization toolbox
if exist('lsqlin')
    taps5 = lsqlin(A,y);
    alltaps = [alltaps taps5];
end

alltaps
 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Support using RLS with Rolled up Graphs
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 
%TODO: need to support data source for constants
 



