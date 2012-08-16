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

function [ldpc,x] = createLdpc(A, bNested,numIterations)
    if nargin < 1
        A = load('matrixout.txt');
    end
    if nargin < 2
        bNested = true;
    end
    if nargin < 3
        numIterations=100;
    end
    
    blockLength = size(A,2);
    numCheckEquations = size(A,1);
    ldpc = FactorGraph();
    x = Bit(blockLength,1);
    for i = 1:numCheckEquations
        varIndices = find(A(i,:));
        if bNested
            gd = getNBitXorDef(length(varIndices));
            ldpc.addFactor(gd,x(varIndices));
        else
            ldpc.addFactor(@xorDelta,x(varIndices));
        end
    end
    ldpc.Solver.setNumIterations(numIterations);
end
