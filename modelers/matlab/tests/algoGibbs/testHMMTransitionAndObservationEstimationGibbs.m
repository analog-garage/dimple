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

function testHMMTransitionAndObservationEstimationGibbs()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testHMMParameterEstimationGibbs');

numStates = 2;
numObsValues = 3;
hmmLength = 1000;
numSamples = 100;
proposalStandardDeviation = 0.1;
updatesPerSample = (hmmLength*2 + numStates^2 + numStates*numObsValues);
burnInUpdates = 1000;

if (repeatable)
    seed = 2;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end



% Sample from system to be estimated
transMatrix = randStochasticStrongDiag(numStates, numStates, 2);
obsMatrix = randStochasticStrongDiag(numObsValues, numStates, 10);

% Run HMM to produce an output sequence.  The factor graph produced after
% will try to infer the state realizations.
stateRealization = zeros(hmmLength,1);
obsRealization = zeros(hmmLength,1);
initDist = randSimplex(numStates);
stateRealization(1) = multinomialSample(initDist);
obsRealization(1) = multinomialSample(obsMatrix(:,stateRealization(1)));
for i = 2:hmmLength
	stateRealization(i) = multinomialSample(transMatrix(:,stateRealization(i-1)));
	obsRealization(i) = multinomialSample(obsMatrix(:,stateRealization(i)));
end

dtrace(debugPrint,'Observation matrix:'); if(debugPrint); disp(obsMatrix); end;
dtrace(debugPrint,'Transition matrix:'); if(debugPrint); disp(transMatrix); end;

t = tic;
fg = FactorGraph();
fg.Solver = 'gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setUpdatesPerSample(updatesPerSample);
fg.Solver.setBurnInUpdates(burnInUpdates);

% Variables
A = Real(numStates, numStates);
O = Real(numObsValues, numStates);
state = Discrete(0:numStates-1,1,hmmLength);
obs = Discrete(0:numObsValues-1,1,hmmLength);

% Priors on A and O
A.Input = com.analog.lyric.dimple.FactorFunctions.NegativeExpGamma(1,1);
O.Input = com.analog.lyric.dimple.FactorFunctions.NegativeExpGamma(1,1);


% Add transition factors
transitionFunction = com.analog.lyric.dimple.FactorFunctions.ParameterizedDiscreteTransition(numStates);
fg.addFactorVectorized(transitionFunction, state(2:end), state(1:end-1), {A,[]});

% Add observation factors
observationFunction = com.analog.lyric.dimple.FactorFunctions.ParameterizedDiscreteTransition(numObsValues, numStates);
fg.addFactorVectorized(observationFunction, obs, state, {O,[]});

% Add observations
obsInputs = zeros(hmmLength, numObsValues);
for obsVal = 1:numObsValues
    obsInputs(obsVal==obsRealization,obsVal)=1;
end;
obs.Input = obsInputs;

ft = toc(t);
if (debugPrint); fprintf('Graph creation time: %.2f seconds\n', ft); end;

% Set proposal standard deviation for real variables
% arrayfun(@(a)(a.Solver.setProposalStandardDeviation(proposalStandardDeviation)), A);
for row=1:size(A,1)
    for col = 1:size(A,2)
        A(row,col).Solver.setProposalStandardDeviation(proposalStandardDeviation);
    end
end
for row=1:size(A,1)
    for col = 1:size(A,2)
        O(row,col).Solver.setProposalStandardDeviation(proposalStandardDeviation);
    end
end


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

% graph1.Solver.saveAllSamples();
dtrace(debugPrint,'Starting Gibbs solve');
t = tic;
fg.solve();
st = toc(t);
if (debugPrint); fprintf('Solve time: %.2f seconds\n', st); end;


% Get the output for O variables
Ooutput = arrayfun(@(a)(a.Solver.getBestSample()), O);
Ooutput = Ooutput - repmat(min(Ooutput,[],1),numObsValues,1);
Ooutput = exp(-Ooutput);
Ooutput = Ooutput./repmat(sum(Ooutput,1),numObsValues,1);
dtrace(debugPrint,'Gibbs estimate for O:'); if(debugPrint); disp(Ooutput); end;


% Get the output for A variables
Aoutput = arrayfun(@(a)(a.Solver.getBestSample()), A);
Aoutput = Aoutput - repmat(min(Aoutput,[],1),numStates,1);
Aoutput = exp(-Aoutput);
Aoutput = Aoutput./repmat(sum(Aoutput,1),numStates,1);
dtrace(debugPrint,'Gibbs estimate for A:'); if(debugPrint); disp(Aoutput); end;

% Compute the KL-divergence rate of the estmate of A
% KLDivergenceRateA = kLDivergenceRate(transMatrix, Aoutput);
% dtrace(debugPrint,'Gibbs KL divergence rate for A:'); dtrace(debugPrint,num2str(KLDivergenceRateA));

% Compare the expected distribution of the observations to the distribution
% based on the estimated transition and observation matrices.  Because
% there is ambiguity in the actual matrices, only this value should be
% consistent with the values from the source model.
expectedObsDistribution = obsMatrix * ((transMatrix^10000)*[1;0]);
estimatedObsDistribution = Ooutput * ((Aoutput^10000)*[1;0]);
dtrace(debugPrint,'Expected observation distribution:'); if(debugPrint); disp(expectedObsDistribution); end;
dtrace(debugPrint,'Estimated observation distribution:'); if(debugPrint); disp(estimatedObsDistribution); end;
KLDivergenceObsDistribution = kLDivergence(expectedObsDistribution, estimatedObsDistribution);
dtrace(debugPrint,'Gibbs KL divergence of observation distribution:'); dtrace(debugPrint,num2str(KLDivergenceObsDistribution));

assert(KLDivergenceObsDistribution < 0.01);


% % COMMENTED OUT FOR NOW; BAUM-WELCH DOESN'T SEEM TO BE WORKING RELIABLY
% % FIXME: WHEN ADDING BACK, ADD IN OBSERVATION ESTIMATION TO THIS VERSION
% % Compare with Baum-Welch ============================================
% fg2 = FactorGraph();
% fg2.Solver = 'sumproduct';
% fg2.Solver.setNumIterations(1);
% 
% % Variables
% state2 = Discrete(0:numStates-1,1,hmmLength);
% 
% % Add random transition factors
% transitionFactor2 = FactorTable(randStochasticMatrix(numStates, numStates),state2.Domain,state2.Domain);
% fg2.addFactorVectorized(transitionFactor2, state2(2:end), state2(1:end-1)).DirectedTo = state2(2:end);
% 
% % Add observation factors
% state2.Input = obsMatrix(obsRealization,:);
% 
% numReEstimations = 20;
% numRestarts = 20;
% dtrace(debugPrint,'Starting Baum-Welch solve');
% t2 = tic;
% fg2.baumWelch({transitionFactor2},numRestarts,numReEstimations);
% if (debugPrint); toc(t2); end;
% output2 = zeros(numStates,numStates);
% for i = 1:length(transitionFactor2.Weights)
%     output2(transitionFactor2.Indices(i,1)+1, transitionFactor2.Indices(i,2)+1) = transitionFactor2.Weights(i);
% end
% output2 = output2./repmat(sum(output2,1),numStates,1);
% dtrace(debugPrint,'Baum-Welch estimate:'); if(debugPrint); disp(output2); end;
% 
% KLDivergenceRate2 = kLDivergenceRate(transMatrix, output2);
% dtrace(debugPrint,'Baum-Welch KL divergence rate:'); dtrace(debugPrint,num2str(KLDivergenceRate2));


dtrace(debugPrint, '--testHMMParameterEstimationGibbs');

end


function KLDivergenceRate = kLDivergenceRate(baseMatrix, estimatedMatrix)
numStates = size(baseMatrix,1);
piStationary = (baseMatrix^1000) * ones(numStates,1)/numStates; % Approximate stationary distribution
P = baseMatrix;
Q = estimatedMatrix;
KLDivergenceRate = 0;
for inState = 1:numStates
    Si = 0;
    for outState = 1:numStates
        Si = Si + P(outState,inState) * log(P(outState,inState) / Q(outState,inState));
    end
    KLDivergenceRate = KLDivergenceRate + piStationary(inState) * Si;
end

end


function KLDivergence = kLDivergence(baseDistribution, estimatedDistribution)
P = baseDistribution;
Q = estimatedDistribution;
KLDivergence = sum(P .* log(P./Q));
end


function m = randStochasticStrongDiag(dOut, dIn, diagStrength)
m = randStochasticMatrix(dOut,dIn);
m = m + diagStrength * eye(dOut,dIn);
m = m ./ repmat(sum(m,1), dOut, 1);
end


function m = randStochasticMatrix(dOut, dIn)
m = zeros(dOut,dIn);
for i = 1:dIn
    m(:,i) = randSimplex(dOut);
end
end


function m = randSparseStochasticMatrix(dOut, dIn, sparsity)
m = zeros(dOut,dIn);
for i = 1:dIn
    m(:,i) = randSkeletonSimplex(dOut, sparsity);
end
end


% Given a column vector x in R^k contained in the standard (k-1)-simplex,
% choose n in {1,...,k} according to the multinomial distribution x
function n = multinomialSample(x)
	n = 1+sum(cumsum(x)<rand);
end

% Choose a column vector in R^k uniformly from the standard (k-1)-simplex
function x = randSimplex(k)
	x = normalize(randExp([k,1]));
end

% Choose a column vector in R^k with expected sparsity p uniformly over the
% union of the appropriate (k-1)-simplices
function x = randSimplexSkeleton(k,p)
	mask = rand([k,1]) < p;
	% Make sure the vector we try to normalize is nonzero somewhere
	if max(mask) == 0
		mask(ceil(k*rand())) = 1;
	end
	x = normalize(mask.*randExp([k,1]));
end

% Standard exponential random variables
function x = randExp(varargin)
	x = -log(rand(varargin{:}));
end

% Normalize a vector
function out = normalize(in)
	out = in/sum(in);
end


