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

function testNestedLabels

    a = Bit();
    b = Bit();
    ng = FactorGraph(a,b);
    c = Bit();
    f = ng.addFactor(@xorDelta,a,b,c);
    a.Label = 'a';
    b.Label = 'b';
    c.Label = 'c';
    f.Label = 'f';
    fg = FactorGraph();
    g = Bit();
    h = Bit();
    g.Label = 'g';
    h.Label = 'h';
    fg.addFactor(ng,g,h);

    assertEqual(fg.Factors{1}.Label,'f');
    assertEqual(fg.Variables{1}.Label,'g');
    assertEqual(fg.Variables{2}.Label,'h');
    assertEqual(fg.Variables{3}.Label,'c');
end
