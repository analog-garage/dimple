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

%function simpleRealHMMTest()

% Graph parameters
hmmLength = 50;                    % Length of the HMM
repeatable = false;                  % Make this run repeat all the same random values

% Gibbs solver parameters
numSamples = 10000;                   % Total number of Gibbs samples to run
proposalStandardDeviation = 0.5;      % Proposal standard deviation for parameter variables
scansPerSample = 1;                 % Number of scans (one update of all variables) per sample
burnInScans = 100;                  % Number of burn-in scans before sampling
bufferSize = 10;

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end


%**************************************************************************
% Model perameters
%**************************************************************************
initialMean = 0;
initialSigma = 0;
transitionMean = 0;
transitionSigma = 0.1;
obsMean = 0;
obsSigma = 1;


%**************************************************************************
% Sample from system to be estimated
%**************************************************************************

x = zeros(1,hmmLength);
x(1) = randn()*initialSigma + initialMean;
for i=2:hmmLength
    x(i) = x(i-1) + randn()*transitionSigma + transitionMean;
end
obsNoise = randn(1,hmmLength)*obsSigma + obsMean;
o = x + obsNoise;


%**************************************************************************
% Solve using Gibbs sampling solver
%**************************************************************************
setSolver('Gibbs');
fg = FactorGraph();
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

Xo = Real();
Xi = Real();
Ob = Real();
sg = FactorGraph(Xi,Xo,Ob);

% Could have done this using AdditiveNoise factor, but this exersizes some
% and deterministic factors
%N = Real(com.analog.lyric.dimple.FactorFunctions.Normal(transitionMean,transitionSigma));

% Would have liked to write "Xo = Xi + N" but that doesn't work in a
% sub-graph since Xo is already defined as a boundary variable
%sg.addFactor(com.analog.lyric.dimple.FactorFunctions.Sum, Xo, Xi, N);
sg.addFactor(com.analog.lyric.dimple.FactorFunctions.AdditiveNoise(transitionSigma),Xo,Xi);
sg.addFactor(com.analog.lyric.dimple.FactorFunctions.AdditiveNoise(obsSigma), Ob, Xi);

fg = FactorGraph();
X = RealStream();
O = RealStream();


fg.addFactor(sg,bufferSize, X,X.getSlice(2),  O);

% Add observation data
% This is a hacky way to do it; better when we can set fixed input values
% for real variables too
%for i=1:hmmLength-1
%    O(i).Domain = [o(i) o(i)];
%    O(i).Solver.setInitialSampleValue(o(i));
%end
ffds = FactorFunctionDataSource();

for i = 1:hmmLength-1
    ffds.add(com.analog.lyric.dimple.FactorFunctions.Normal(o(i),1e-5));
end
O.DataSource = ffds;

% Set proposal standard deviation for real variables
for i = 1:X.Size
    X.get(i).Solver.setProposalStandardDeviation(proposalStandardDeviation);
end


if (repeatable)
    fg.Solver.setSeed(1);		% Make the Gibbs solver repeatable
end

% Solve
disp('Starting Gibbs solver');
fg.NumSteps = 0;
fg.initialize();

i = 1;
output = [];
while 1
    fg.solve(false);
    sample = X.get(1).Solver.getBestSample();
    output(i) = sample;
    i = i + 1;
    
    if fg.hasNext()
        fg.advance();
    else
        break;
    end
end

% Get the estimated transition matrix
%output = cell2mat(X.invokeSolverMethodWithReturnValue('getBestSample'));
disp('Gibbs estimate:'); disp(output);

hold off;
plot(x,'r');
hold on;
plot(output,'g');
plot(o,'b');


%end
