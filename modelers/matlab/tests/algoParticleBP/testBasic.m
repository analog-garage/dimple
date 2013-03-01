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

function testBasic()

    %We use a particle BP solver to find the distribution over c given
    %c = a*b
    %when a is a normal variable with mean = 3 and std = 1
    %     b is a normal variable with mean = 5 and std = 1

    %First we set the Solver
    setSolver('particlebp');

    %Create three real variables.
    a = Real();
    b = Real();
    c = Real();

    %Create the factor graph.
    fg = FactorGraph();
    fg.Solver.setSeed(0);
    
    %specify the number of particles per message
    fg.Solver.setNumParticles(30);

    %specify the 
    fg.Solver.setResamplingUpdatesPerParticle(30);

    %Create the factor.
    realProduct = com.analog.lyric.dimple.FactorFunctions.Product(1.0);
    fg.addFactor(realProduct,c,a,b);

    %Set the inputs for a and b.
    a.Input = com.analog.lyric.dimple.FactorFunctions.SimpleNormal(3,1);
    b.Input = com.analog.lyric.dimple.FactorFunctions.SimpleNormal(5,1);

    %Optionally use tempering
    %fg.Solver.setInitialTemperature(1);
    %fg.Solver.setTemperingHalfLifeInIterations(1);

    %Solve
    fg.initialize();
    for i = 1:3
%         fprintf('iteration: %d\n',i);
        fg.Solver.iterate();
    end

    %Retrieve the beliefs
    x = 0:.1:30;
    y = c.Solver.getBelief(x);
    
    %Plot the result and display the mean of c
    %plot(x,y,'*');
    mn = x*y;
    assertTrue(mn>14 && mn < 16);
    %fprintf('We expect the mean of c to be around 15: %f\n',mn);

end
