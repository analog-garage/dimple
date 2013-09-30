function testConjugateSamplingRolledUpGraph()

debugPrint = false;
repeatable = true;

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

dtrace(debugPrint, '++testConjugateSamplingRolledUpGraph');

test1(debugPrint, repeatable);

dtrace(debugPrint, '--testConjugateSamplingRolledUpGraph');

end


% Rolled-up graph with Real variables using conjugate sampler
function test1(debugPrint, repeatable)

% Generate test data
debugPlot = true;
numSamples = 1000;
transitionPrecision = 1;
observationPrecsion = 20;
length = 25;
bufferSize = 5;
x = zeros(1,length);
x(1) = 37;      % Don't center around 0
transitionSigma = 1/sqrt(transitionPrecision);
observationSigma = 1/sqrt(observationPrecsion);
for i = 2:length
    x(i) = x(i-1) + randn * transitionSigma;
end
data = x + randn(1,length) * observationSigma;



% Sub-graph
sg = FactorGraph();
sg.Solver = 'Gibbs';

in = Real();
out = Normal(in, transitionPrecision);

sg.addBoundaryVariables(in, out);

% Outer-graph
fg = FactorGraph();
fg.Solver = 'Gibbs';

states = RealStream();
f = fg.addFactor(sg, states, states.getSlice(2));
f.BufferSize = bufferSize;

% FIXME: This is an ugly way to get the data in
% Preferably, the sub-graph would include an observation factor,
% and the corresponding output variables would be set to a fixed-value
% This would require data-sources that set FixedValue instead of Input
dataSource = FactorFunctionDataSource();
for i=1:length
    dataSource.add(FactorFunction('Normal', data(i), observationPrecsion));
end
states.DataSource = dataSource;
% FIXME: Blast-from-the-past-factor should be recognized as conjugate
% assert(strcmp(states.Variables(1).Solver.getSamplerName,'NormalSampler'));
assert(strcmp(states.Variables(2).Solver.getSamplerName,'NormalSampler'));

fg.Solver.setNumSamples(numSamples);
fg.Solver.setBurnInScans(10);
fg.Solver.saveAllSamples();
fg.Solver.saveAllScores();
if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

% fg.solve();
% Manually loop through the rolled-up graph instead of calling solve
outputB = zeros(1,length);
outputM = zeros(1,length);
fg.initialize();
for i = 1:length-bufferSize
    fg.solveOneStep();
    % Get both best-sample and mean of all samples
    outputB(i) = states.get(1).Solver.getBestSample();
    samples = states.get(1).Solver.getAllSamples();
%     outputM(i) = mean(samples(end-numSamples+1:end));
    outputM(i) = mean(samples);
    
    if fg.hasNext()
        tmp = states.get(1).Factors{1}.Solver.getPotential();
        fg.advance();
        assertEqual(tmp,states.get(1).Factors{2}.Solver.getPotential());
        
    else
        % Wrapping up; get the rest of the buffer
        for j=1:bufferSize
            outputB(i+j) = states.get(1+j).Solver.getBestSample();
            samples = states.get(1+j).Solver.getAllSamples();
            outputM(i+j) = mean(samples(end-numSamples+1:end));
        end
        break
    end
end

if (debugPrint && debugPlot)
    figure(1);
    hold off;
    plot(x,'r');
    hold on;
    plot(data,'b');
    plot(outputB,'g');
    plot(outputM,'m');
    drawnow();
end


% Unrolled version
fgU = FactorGraph();
fgU.Solver = 'Gibbs';

statesU = Real(1,length);
obsU = Real(1,length);
fgU.addFactorVectorized('Normal', statesU(1:end-1), transitionPrecision, statesU(2:end));
fgU.addFactorVectorized('Normal', statesU, observationPrecsion, obsU);
obsU.FixedValue = data;

fgU.Solver.setNumSamples(numSamples);
fgU.Solver.setBurnInScans(10);
fgU.Solver.saveAllSamples();
fgU.Solver.saveAllScores();
if (repeatable)
    fgU.Solver.setSeed(1);					% Make this repeatable
end

fgU.solve();
outputBU = zeros(1,length);
outputMU = zeros(1,length);
for i=1:length
    % Get both best-sample and mean of all samples
    outputBU(i) = statesU(i).Solver.getBestSample();
    samples = statesU(i).Solver.getAllSamples();
    outputMU(i) = mean(samples);
end

if (debugPrint && debugPlot)
    figure(2);
    hold off;
    plot(x,'r');
    hold on;
    plot(data,'b');
    plot(outputBU,'g');
    plot(outputMU,'m');
    drawnow();
end

% Compare rolled-up and unrolled versions
% Shouldn't be identical, but should be fairly close
assertElementsAlmostEqual(outputB, outputBU, 'absolute', 0.5);
assertElementsAlmostEqual(outputM, outputMU, 'absolute', 0.5);
    
end
