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

function testDamping()


    %Test damping from variable

    for i = 1:2

        fg = FactorGraph();

        if i == 1
            %dampingVal = 0;
            dampingVal = .4;
            fg.Solver.setDamping(dampingVal);
        end

        a = Bit();
        b = Bit();
        f = fg.addFactor(@xorDelta,a,b);

        a.Input = .8;

        if i == 2
            dampingVal = .7;
            fg.Solver.setDamping(dampingVal);
        end

        fg.solve();

        msg2xor = dampingVal*.5 + (1-dampingVal)*.8;
        msgFromXor = dampingVal*.5 + (1-dampingVal)*msg2xor;

        assertEqual(b.Belief,msgFromXor);

        fg.solve();
    end


end

