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

function testOperatorOverloading()

    %There are two hacks associated with this feature
    %1) When creating a Factor with operator overloading, 
    %   the last created Factor Graph is used.  Ideally, Dimple would allow
    %   graphs to automatically merge when constraints are created between
    %   them.  If this were the case, this wouldn't be an issue.
    %2) The comain of the lhs is created automatically in whatever order
    %   the elements are found.  Often times the bit is reversed from 1/0
    %   to 0/1

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
