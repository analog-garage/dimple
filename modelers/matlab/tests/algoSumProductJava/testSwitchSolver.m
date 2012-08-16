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

function testSwitchSolver( )

    setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
    fg = FactorGraph();
    b = Bit(3,1);
    fg.addFactor(@xorDelta,b);
    b.Input = [.8 .8 .6];
    fg.solve();
    expected = [0.7586; 0.7586; 0.4138];
    assertTrue(all((b.Belief-expected)<.0001));
    
    fg.Solver = com.analog.lyric.dimple.solvers.minsum.Solver();
    fg.solve();
    
    expected = [0.7273; 0.7273; 0.2727];
    assertTrue(all((b.Belief-expected)<.0001));
    
    setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
    
    % float_to_fixed() from SolverSwitchTest.java
    
    %b2 = Bit(3,1);
    %setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
    %fg2 = FactorGraph();
    %fg.addFactor(@xorDelta,b);
    %setSolver(com.analog.lyric.dimple.solvers.sumproductfixedpoint.Solver());

    % fixed_to_float() from SolverSwitchTest.java
    
    %b2 = Bit(3,1);
    %setSolver(com.analog.lyric.dimple.solvers.sumproductfixedpoint.Solver());
    %fg2 = FactorGraph();
    %fg.addFactor(@xorDelta,b);
    %setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
end

