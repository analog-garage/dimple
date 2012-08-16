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

function testequals()
    %disp('++testequals')
    b = Bit(3,1);
    MyGraph = FactorGraph();
    MyGraph.addFactor(@myEqFunc,b);

    b.Input = [.9 .8 .5];
    MyGraph.solve();

    x = b(1).Belief;
    y = b(2).Belief;
    z = b(3).Belief;

    expected_x = .9*.8*.5/(.9*.8*.5 + .1*.2*.5);
    assertElementsAlmostEqual(expected_x,x(1));
    assertElementsAlmostEqual(expected_x,y(1));
    assertElementsAlmostEqual(expected_x,z(1));

    v = Variable([2 1 0],3,1);
    MyGraph2 = FactorGraph();
    MyGraph2.addFactor(@myEqFunc,v);
    
    %BUG: this throws exception, of 'priors must sum to 1'
    % we think Jeff discovered a tolerance in the CSolver
    v.Input =  [.7 .2 .1; .3 .6 .1; .6 .2 .2];

    MyGraph2.solve();

    x = v(1).Belief;

    expected_x = .7*.3*.6/(.7*.3*.6+.2*.6*.2+.1*.1*.2);
    assertElementsAlmostEqual(expected_x,x(1));
    %disp('--testequals')
    

end
