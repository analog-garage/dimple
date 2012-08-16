%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

function testJoinVariable()

    m = [1 2; ...
        3 4];

    aInput = [.2 .8];
    bInput = [.6 .4];
    myfactor = @(x,y,m) m(x+1,y+1);
    domain = {0,1};

    %First get expected values
    fg = FactorGraph();
    a = Variable(domain);
    b = Variable(domain);
    fg.addFactor(myfactor,a,b,m);
    a.Input = aInput;
    b.Input = bInput;

    fg.solve();
    aExpected = a.Belief;
    bExpected = b.Belief;

    a.Input = [1 1];
    b.Input = [1 1];
    fg.solve();
    aExpectedNoInput = a.Belief;
    bExpectedNoInput = b.Belief;


    %Now try using join
    fg = FactorGraph();

    a = Variable(domain);
    a.Name = 'a';
    b = Variable(domain);
    b.Name = 'b';

    a.Input = aInput;
    b.Input = bInput;


    f = fg.addFactor(myfactor,a,b,m);
    f.Name = 'f';

    anew = fg.split(a);
    anew.Name = 'anew';
    bnew = fg.split(b);
    bnew.Name = 'bnew';

    c = fg.join(a,b);
    c.Name = 'c';

    aproj = anew.Factors{1};
    aproj.Name = 'aproj';
    bproj = bnew.Factors{1};
    bproj.Name = 'bproj';


    fg.solve();
    assertElementsAlmostEqual(anew.Belief,aExpected);
    assertElementsAlmostEqual(bnew.Belief,bExpected);


    c.Input = [1 1 1 1];
    anew.Input = [.5 .5];
    bnew.Input = [.5 .5];
    fg.solve();
    assertElementsAlmostEqual(anew.Belief,aExpectedNoInput);
    assertElementsAlmostEqual(bnew.Belief,bExpectedNoInput);


    anew.Input = [.2 .8];
    bnew.Input = [.6 .4];
    fg.solve();
    assertElementsAlmostEqual(anew.Belief,aExpected);
    assertElementsAlmostEqual(bnew.Belief,bExpected);

    
    A = Bit();
    B = Bit();
    C = Bit();

    fg = FactorGraph();

    %Make sure this doesn't crash.
    fg.join(A,B,C);
    
end
