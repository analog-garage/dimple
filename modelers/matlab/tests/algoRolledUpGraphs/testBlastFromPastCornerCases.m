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

function testBlastFromPastCornerCases()

  
    %What happens if twoFSs share a parameter.  Do we update blast from pasts correctly?
    const1 = [.6 .4];
    const2 = [.7 .3];
    input = .2;

    b = Bit();
    ng1 = FactorGraph(b);
    ng1.addFactor(@constFactor,b,const1);

    b = Bit();
    ng2 = FactorGraph(b);
    ng2.addFactor(@constFactor,b,const2);

    fg = FactorGraph();
    b = Bit();
    b.Input = input;
    fg.addRepeatedFactor(ng1,b);
    fg.addRepeatedFactor(ng2,b);

    fg.initialize();

    fg2 = FactorGraph();
    c = Bit();
    c.Input = input;

    for i = 1:10
      fg.solve(false);

      fg2.addFactor(@constFactor,c,const1);
      fg2.addFactor(@constFactor,c,const2);
      fg2.solve();

      assertElementsAlmostEqual(b.Belief,c.Belief);

      fg.advance();
    end

    assertEqual(4,length(fg.Factors));
    assertEqual(1,length(fg.Variables));

end
