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

function testfunc()
    %disp('++testfunc')
    b = Bit(2,1);
    g = FactorGraph();
    g.addFactor(@myInvFunc,b(1),b(2));
    b.Input = [.8 .3];
    g.Solver.setNumIterations(2);
    g.solve();
    
    x = b(1).Belief;
    y = b(2).Belief;
    
    expected_x = .7*.8/(.7*.8 + .3*.2);
    assertElementsAlmostEqual(expected_x,x(1));    
    assertElementsAlmostEqual(1-expected_x,y(1));    
    %disp('--testfunc')
    
end
