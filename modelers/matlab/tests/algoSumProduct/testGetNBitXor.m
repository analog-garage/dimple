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

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testGetNBitXor()

    %Reset cache so we can test caching later.
    global getNVBitXorDefGraphCache___;

    getNVBitXorDefGraphCache___ = [];
    
    maxNumEdges = 10;
    
    for j = 1:2
        for n = 2:maxNumEdges
            priors = rand(n,1);

            [ng] = getNBitXorDef(n);
            b = Bit(n,1);
            g = FactorGraph();
            g.addFactor(ng,b);
            b.Input = priors;
            g.Solver.setNumIterations(20);
            g.solve();
            actualBelief = b.Belief;

            b = Bit(n,1);
            g = FactorGraph();
            g.addFactor(@xorDelta,b);
            b.Input = priors;
            g.Solver.setNumIterations(20);
            g.solve();
            expectedBelief = b.Belief;

            assertElementsAlmostEqual(actualBelief,expectedBelief,'testGetNBitXor');
        end
    end

    %%%%%%
    %Now let's test caching

    %Test N caching
    %We expect there to be n different trees
    %global savedGraphs___;
    assertEqual(int32(getNVBitXorDefGraphCache___.NumGraphs),int32(maxNumEdges-1))
    
    %Test Solver caching
    %TODO: we need to have two solvers to test this
    
    %Test deltaFunc caching
    %TODO
    %myfunc = @(a,b,c) a == b && b == c;
    %[x,y] = symmetricRelationTree(myfunc,@Variable,[1 0], 10);
    
    %test var constructor caching
    %TODO
    
    %test domain caching
    %TODO
    


end
