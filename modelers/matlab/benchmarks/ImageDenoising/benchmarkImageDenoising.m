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

function benchmarks = benchmarkImageDenoising()
    
    warmupIterations = 0;
    iterations = 1;
    doGC = false;
    
    benchmarks = functionbenchmarks({ ...
        @imageDenoisingSumProduct, ...
        @imageDenoisingMinSum, ...
        @imageDenoisingGibbs ...
        }, warmupIterations ...
        , iterations ...
        , doGC);
    
end

function imageDenoisingSumProduct()
    
    fg = FactorGraph();
    fg.Solver = 'SumProduct';
    fg.Solver.setNumIterations(1);
    doImageDenoising(fg);
    
end

function imageDenoisingMinSum()
    
    fg = FactorGraph();
    fg.Solver = 'MinSum';
    fg.Solver.setNumIterations(2);
    doImageDenoising(fg);
    
end

function imageDenoisingGibbs()
    
    fg = FactorGraph();
    fg.Solver = 'Gibbs';
    fg.Solver.setNumSamples(1600);
    doImageDenoising(fg);
    
end

function doImageDenoising(fg)
    
    imageFileName = 'images/1202.4002.3.png';
    imageDimension = 100;
    xImageOffset = 800;
    yImageOffset = 1925;
    xImageSize = imageDimension;
    yImageSize = imageDimension;
    noiseSigma = 1.0;
    xBlockSize = 4;
    yBlockSize = 4;
    g = ImageDenoisingGraph(fg, 'FactorTableValues300dpi.mat', ...
        xImageSize,  yImageSize,  xBlockSize, yBlockSize);
    likelihoods = noisyImageInput(imageFileName, noiseSigma, ...
        xImageOffset, yImageOffset, xImageSize, yImageSize);
    g.variables.Input = likelihoods;
    fg.solve();
    % output = svg.variables.Value;
    score = fg.Score;
    
end

