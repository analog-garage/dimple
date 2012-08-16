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

function testGetIndices

    a = Bit();
    b = Bit();
    ng = FactorGraph(a,b);
    ng.addFactor(@xorDelta,a,b);

    fg = FactorGraph();

    s = BitStream();

    fg.addFactor(ng,4,s,s.getSlice(2));


    assertEqual(s.FirstVarIndex,1);
    assertEqual(s.LastVarIndex,5);
    assertTrue(s.FirstVar==s.get(1));
    assertTrue(s.LastVar==s.get(5));

    fg.advance();

    assertEqual(s.FirstVarIndex,2);
    assertEqual(s.LastVarIndex,6);
    assertTrue(s.FirstVar==s.get(2));
    assertTrue(s.LastVar==s.get(6));
end
