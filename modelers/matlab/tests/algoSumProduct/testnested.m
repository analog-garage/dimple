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

function testnested()
    %disp('++testnested')
    b = Bit(2,1);
    ng = FactorGraph(b);
    ng.addFactor(@xorDelta,b);

    b2 = Bit(2,1);
    g = FactorGraph();
    g.addFactor(ng,b2);
    b2.Input = [.9 .5]';
    g.Solver.setNumIterations(2);
    g.solve();
    assertElementsAlmostEqual(b2.Belief,[.9 .9]');
        
        
    %Do the same thing but with CSL
    b = Bit(2,1);
    ng = FactorGraph(b(1),b(2));
    ng.addFactor(@xorDelta,b);

    b2 = Bit(2,1);
    g = FactorGraph();
    g.addFactor(ng,b2);
    b2.Input = [.9 .5]';
    g.Solver.setNumIterations(2);
    g.solve();
    assertElementsAlmostEqual(b2.Belief,[.9 .9]');

    %disp('--testnested')
end

