%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

function testtogether()
    %disp('++testtogether')
    b = Bit(5,1);
    g = FactorGraph();
    g.addFactor(@xorDelta,b(1:3));
    g.addFactor(@xorDelta,b(3:5));


    b.Input = [.9 .8 .2 .4 .9];
    g.Solver.setNumIterations(5);
    g.solve();
    
    x = b(1).Belief;
    y = b(2).Belief;
    z = b(3).Belief;
    v = b(4).Belief;
    w = b(5).Belief;
    
    
    %Todo should flip v
    expected_hard_v = true;
    v_guess = v(1) > .5;
    assertEqual(expected_hard_v,v_guess);
    
    
    %Now figure out soft values.
    into_z_from_xy = (.9*.2 + .8*.1) / (.9*.2 + .8*.1 + .9*.8 + .1 * .2);
    %into_z_from_xy
    into_z_from_vw = (.1*.4 + .9*.6) / (.1*.4 + .9*.6 + .9*.4 + .1*.6);
    %into_z_from_vw
    
    expected_z = into_z_from_xy*into_z_from_vw*.2/...
                (into_z_from_vw*into_z_from_xy*.2 + ...
                (1-into_z_from_xy)*(1-into_z_from_vw)*(1-.2));
    
    assertElementsAlmostEqual(expected_z,z(1),'testtogether z');
    
    into_vw_from_z = .26*.2/(.26*.2 + (1-.26)*(1-.2));
    into_v = into_vw_from_z*.1 + (1-into_vw_from_z)*.9;
    expected_v = .4*into_v/(.4*into_v + .6*(1-into_v));
    
    assertElementsAlmostEqual(expected_v,v(1),'testtogether v');
    %disp('--testtogether')
end
