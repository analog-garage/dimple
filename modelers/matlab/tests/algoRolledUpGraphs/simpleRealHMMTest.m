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
hmmLength = 100;                    % Length of the HMM
repeatable = false;                  % Make this run repeat all the same random values

% Gibbs solver parameters
numSamples = 100;                   % Total number of Gibbs samples to run
proposalStandardDeviation = 0.5;      % Proposal standard deviation for parameter variables
scansPerSample = 1;                 % Number of scans (one update of all variables) per sample
burnInScans = 100;                  % Number of burn-in scans before sampling

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
initialSigma = 20;
transitionMean = 0;
transitionSigma = 0.1;
transitionPrecision = 1/transitionSigma^2;
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

Xo = Real();
Xi = Real();
Ob = Real();
sg = FactorGraph(Xi,Xo,Ob);

% Could have done this using AdditiveNoise factor, but this exersizes some
% and deterministic factors
N = Real(com.analog.lyric.dimple.FactorFunctions.Normal(transitionMean,transitionPrecision));

% Would have liked to write "Xo = Xi + N" but that doesn't work in a
% sub-graph since Xo is already defined as a boundary variable
sg.addFactor(com.analog.lyric.dimple.FactorFunctions.Sum, Xo, Xi, N);
%sg.addFactor(com.analog.lyric.dimple.FactorFunctions.AdditiveNoise(transitionSigma),Xo,Xi);

sg.addFactor(com.analog.lyric.dimple.FactorFunctions.AdditiveNoise(obsSigma), Ob, Xi);

fg = FactorGraph();
X = Real(1,hmmLength);
O = Real(1,hmmLength-1);
fg.addFactorVectorized(sg, X(1:end-1),X(2:end),  O);

% Add observation data
% This is a hacky way to do it; better when we can set fixed input values
% for real variables too
%for i=1:hmmLength-1
%    O(i).Domain = [o(i) o(i)];
%    O(i).Solver.setInitialSampleValue(o(i));
%end
for i = 1:hmmLength-1
    O(i).Solver.setAndHoldSampleValue(o(i));
end

% Set proposal standard deviation for real variables
X.invokeSolverMethod('setProposalStandardDeviation', proposalStandardDeviation);

if (repeatable)
    fg.Solver.setSeed(1);		% Make the Gibbs solver repeatable
end

% Solve
disp('Starting Gibbs solver');
fg.Solver.setNumRestarts(1);
fg.Solver.setNumSamples(200);
fg.Solver.setBurnInUpdates(0);
%fg.Solver.setScansPerSample(scansPerSample);
fg.solve();

% Get the estimated transition matrix
output = cell2mat(X.invokeSolverMethodWithReturnValue('getBestSample'));
%disp('Gibbs estimate:'); disp(output);

hold off;
plot(x,'r');
hold on;
plot(output,'g');
plot(o,'b');


%end
