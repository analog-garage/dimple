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

function testGetSlice()

    %TODO: how do we test?
    s = BitStream();

    %test getSlice with only one arg
    s1 = s.getSlice(1);
    s2 = s.getSlice(2);
    s3 = s.getSlice(3);

    b = Bit();
    c = Bit();
    ng = FactorGraph(b,c);
    ng.addFactor(@xorDelta,b,c);
    fg = FactorGraph();
    stream = fg.addFactor(ng,2,s,s2);

    assertTrue(s1.get(1) == s.get(1));
    assertTrue(s2.get(1) == s1.get(2));
    assertTrue(s3.get(1) == s1.get(3));

end
