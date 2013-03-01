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

function testAddFactorJava()


    myxor = com.analog.lyric.dimple.FactorFunctions.Xor();

    fg = FactorGraph();

    b = Bit(3,1);

    fg.addFactor(myxor,b(1),b(2),b(3));

    b(1).Input = .8;
    b(2).Input = .8;

    fg.solve();


    fg2 = FactorGraph();
    b2 = Bit(3,1);

    fg2.addFactor(@xorDelta,b2);

    b2.Input = [.8 .8 .5];

    fg2.solve();


    assertElementsAlmostEqual(b(3).Belief,b2(3).Belief);
end
