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

function benchmarks = benchmarkStereoVision()
    
    warmupIterations = 0;
    iterations = 1;
    doGC = false;
    
    benchmarks = functionbenchmarks({ ...
        @stereoVisionArtScaledSumProduct, ...
        @stereoVisionArtScaledMinSum, ...
        @stereoVisionArtScaledGibbs ...
        }, warmupIterations ...
        , iterations ...
        , doGC);
    
end

function stereoVisionArtScaledSumProduct()
    
    fg = FactorGraph();
    fg.Solver = 'SumProduct';
    fg.Solver.setNumIterations(10);
    svg = StereoVisionGraph(fg, 'art_scaled');
    
    fg.solve();
    % output = svg.variables.Value;
    score = fg.Score;
    
end

function stereoVisionArtScaledMinSum()
    
    fg = FactorGraph();
    fg.Solver = 'MinSum';
    fg.Solver.setNumIterations(10);
    svg = StereoVisionGraph(fg, 'art_scaled');
    
    fg.solve();
    % output = svg.variables.Value;
    score = fg.Score;
    
end

function stereoVisionArtScaledGibbs()
    
    fg = FactorGraph();
    fg.Solver = 'Gibbs';
    fg.Solver.setNumSamples(400);
    svg = StereoVisionGraph(fg, 'art_scaled');
    
    fg.solve();
    % output = svg.variables.Value;
    score = fg.Score;
    
end
