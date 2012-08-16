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

function testParameter

    v = Bit();
    ng = FactorGraph(v);
    ng.addFactor(@constFactor,v,[.6 .4]);

    fg = FactorGraph();
    v = Bit();
    v.Input = .4;
    fg.addRepeatedFactor(ng,v);

    fg.initialize();

    fg2 = FactorGraph();
    b = Bit();
    b.Input = .4;

    for i = 1:10
        fg2.addFactor(@constFactor,b,[.6 .4]);
        fg2.solve();
        fg.solve(false);

        assertElementsAlmostEqual(b.Belief,v.Belief);

        fg.advance();
    end
    
    assertEqual(1,length(fg.Variables));
    assertEqual(2,length(fg.Factors));
    
end
