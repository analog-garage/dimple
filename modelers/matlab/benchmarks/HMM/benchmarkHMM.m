%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2014 Analog Devices, Inc.
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

function benchmarks = benchmarkHMM()
    
    warmupIterations = 0;
    iterations = 1;
    doGC = false;
    
    benchmarks = functionbenchmarks({ ...
        @hmmGibbs100x4x4, ...
        @hmmGibbs100000x4x4, ...
        @hmmGibbs1000x1000x1000, ...
        @hmmSumProduct100x4x4, ...
        @hmmSumProduct100000x4x4, ...
        @hmmSumProduct1000x1000x1000, ...
        @hmmMinSum100x4x4, ...
        @hmmMinSum100000x4x4, ...
        @hmmMinSum1000x1000x1000 }, ...
        warmupIterations ...
        , iterations ...
        , doGC);
    
end

function hmmGibbs100x4x4()
    fg = FactorGraph();
    fg.Solver = 'Gibbs';
    fg.Solver.setNumSamples(600000);
    stages = 100;
    stateDomainOrder = 4;
    observationDomainOrder = 4;
    hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
end

function hmmGibbs100000x4x4()
    fg = FactorGraph();
    fg.Solver = 'Gibbs';
    fg.Solver.setNumSamples(300);
    stages = 100000;
    stateDomainOrder = 4;
    observationDomainOrder = 4;
    hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
end

function hmmGibbs1000x1000x1000()
    fg = FactorGraph();
    fg.Solver = 'Gibbs';
    fg.Solver.setNumSamples(1500);
    stages = 1000;
    stateDomainOrder = 1000;
    observationDomainOrder = 1000;
    hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
end

function hmmSumProduct100x4x4()
    fg = FactorGraph();
    fg.Solver = 'SumProduct';
    fg.Solver.setNumIterations(1200);
    stages = 100;
    stateDomainOrder = 4;
    observationDomainOrder = 4;
    hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
end

function hmmSumProduct100000x4x4()
    fg = FactorGraph();
    fg.Solver = 'SumProduct';
    fg.Solver.setNumIterations(240);
    stages = 100000;
    stateDomainOrder = 4;
    observationDomainOrder = 4;
    hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
end

function hmmSumProduct1000x1000x1000()
    fg = FactorGraph();
    fg.Solver = 'SumProduct';
    fg.Solver.setNumIterations(3);
    stages = 1000;
    stateDomainOrder = 1000;
    observationDomainOrder = 1000;
    hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
end

function hmmMinSum100x4x4()
    fg = FactorGraph();
    fg.Solver = 'MinSum';
    fg.Solver.setNumIterations(6000);
    stages = 100;
    stateDomainOrder = 4;
    observationDomainOrder = 4;
    hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
end

function hmmMinSum100000x4x4()
    fg = FactorGraph();
    fg.Solver = 'MinSum';
    fg.Solver.setNumIterations(360);
    stages = 100000;
    stateDomainOrder = 4;
    observationDomainOrder = 4;
    hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
end

function hmmMinSum1000x1000x1000()
    fg = FactorGraph();
    fg.Solver = 'MinSum';
    fg.Solver.setNumIterations(3);
    stages = 1000;
    stateDomainOrder = 1000;
    observationDomainOrder = 1000;
    hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
end

function hmmInference(fg, stages, stateDomainOrder, observationDomainOrder)
    hmm = HMMGraph(fg, stages, stateDomainOrder, observationDomainOrder);
    fg.solve();
    ignore (hmm.states(1).Belief);
    ignore (fg.Score);
end

function ignore(~)
end
