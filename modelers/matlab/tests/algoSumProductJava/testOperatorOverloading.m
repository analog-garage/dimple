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

function testOperatorOverloading()

    %There are two hacks associated with this feature
    %1) When creating a Factor with operator overloading, 
    %   the last created Factor Graph is used.  Ideally, Dimple would allow
    %   graphs to automatically merge when constraints are created between
    %   them.  If this were the case, this wouldn't be an issue.
    %2) The domain of the lhs is created automatically in whatever order
    %   the elements are found.  Often times the bit is reversed from 1/0
    %   to 0/1

    testCase1();
    testCase2();
    testCase3();
    
end

function testCase1()

    fg = FactorGraph();
    
    a = Bit();
    b = Bit();
    c = Bit();
    
    d = a | (b & c);
    
    a.Input = .8;
    b.Input = .8;
    c.Input = .8;
    
    fg.solve();
   
    fg2 = FactorGraph();
    
    a2 = Bit();
    b2 = Bit();
    c2 = Bit();
    d2 = Variable({0,1});
    tmp = Bit();
    
    myand = @(z,x,y) z == (x&y);
    fg2.addFactor(myand,tmp,b2,c2);
    myor = @(z,x,y) z == (x|y);
    fg2.addFactor(myor,d2,a2,tmp);
    
    a2.Input = .8;
    b2.Input = .8;
    c2.Input = .8;
    
    fg2.solve();
    
    %d2.Domain
    %d2.Belief
    
    %check domains match    
    assertTrue(isequal(d.Domain,d2.Domain));
    
    %check beliefs match
    assertElementsAlmostEqual(d.Belief,d2.Belief);
        
    
    %Uncomment if you want to see what the graph looks like:
    %{
    a.Name = 'a';
    b.Name = 'b';
    c.Name = 'c';    
    d.Name = 'd';
    
    d.Factors{1}.Name = 'or';
    intermediate = d.Factors{1}.Variables{3};
    intermediate.Name = 'intermediate';
    intermediate.Factors{1}.Name = 'and';
    
    fg.plot(1);
    %}

end

function testCase2()

    fg = FactorGraph();
    
    a = Bit();
    b = Bit();
    c = Bit();
    x = Bit();
    
    d = (a | ~(b & c)) ^ x;
    
    a.Input = .8;
    b.Input = .7;
    c.Input = .2;
    x.Input = .9;
    
    fg.solve();
   
    fg2 = FactorGraph();
    
    a2 = Bit();
    b2 = Bit();
    c2 = Bit();
    x2 = Bit();
    d2 = Variable({0,1});
    tmp1 = Bit();
    tmp2 = Bit();
    
    mynand = @(z,x,y) z == (~(x&y));
    fg2.addFactor(mynand,tmp1,b2,c2);
    myor = @(z,x,y) z == (x|y);
    fg2.addFactor(myor,tmp2,a2,tmp1);
    myxor = @(z,x,y) z == x ^ y;
    fg2.addFactor(myxor,d2,tmp2,x2);
    
    a2.Input = .8;
    b2.Input = .7;
    c2.Input = .2;
    x2.Input = .9;

    fg2.solve();
        
    %check domains match    
    assertTrue(isequal(d.Domain,d2.Domain));
    
    %check beliefs match
    assertElementsAlmostEqual(d.Belief,d2.Belief);

end

function testCase3()

    seed = 0;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);

    fg = FactorGraph();
    
    aDomain = 0:2:10;
    bDomain = -1:1;
    cDomain = [37 18 26];
    dDomain = 0:2;    
    
    aInput = rand(1,length(aDomain));
    bInput = rand(1,length(bDomain));
    cInput = rand(1,length(cDomain));
    dInput = rand(1,length(dDomain));
    
    a = Discrete(aDomain);
    b = Discrete(bDomain);
    c = Discrete(cDomain);
    d = Discrete(dDomain);
    
    x = (-(a + c))^d * b;
    y = x < c;
    z = x > c;
    
    a.Input = aInput;
    b.Input = bInput;
    c.Input = cInput;
    d.Input = dInput;
    
    fg.solve();
    
    
    [aX,cX]=ndgrid(aDomain,cDomain); acDomain = sort(unique(aX + cX));
    nacDomain = -acDomain;
    [nacX,dX]=ndgrid(nacDomain,dDomain); nacdDomain = sort(unique(nacX.^dX));
    [nacdX,bX]=ndgrid(nacdDomain,bDomain); xDomain = sort(unique(nacdX .* bX));
   
    fg2 = FactorGraph();
    
    a2 = Discrete(aDomain);
    b2 = Discrete(bDomain);
    c2 = Discrete(cDomain);
    d2 = Discrete(dDomain);
    ac2 = Discrete(acDomain);
    nac2 = Discrete(nacDomain);
    nacd2 = Discrete(nacdDomain);
    x2 = Discrete(xDomain);
    y2 = Bit();
    z2 = Bit();
    
    mysum = @(z,x,y) z == (x+y);
    myneg = @(z,x) z == (-x);
    mypow = @(z,x,y) z == (x^y);
    myprod = @(z,x,y) z == (x*y);
    mylt = @(z,x,y) z == (x<y);
    mygt = @(z,x,y) z == (x>y);
    fg2.addFactor(mysum,ac2,a2,c2);
    fg2.addFactor(myneg,nac2,ac2);
    fg2.addFactor(mypow,nacd2,nac2,d2);
    fg2.addFactor(myprod,x2,nacd2,b2);
    fg2.addFactor(mylt,y2,x2,c2);
    fg2.addFactor(mygt,z2,x2,c2);
    
    a2.Input = aInput;
    b2.Input = bInput;
    c2.Input = cInput;
    d2.Input = dInput;

    fg2.solve();
        
    %check domains match    
    assertTrue(isequal(x.Domain,x2.Domain));
    assertTrue(isequal(y.Domain,y2.Domain));
    assertTrue(isequal(z.Domain,z2.Domain));
    
    %check beliefs match
    assertElementsAlmostEqual(x.Belief,x2.Belief);
    assertElementsAlmostEqual(y.Belief,y2.Belief);
    assertElementsAlmostEqual(z.Belief,z2.Belief);
    
end
