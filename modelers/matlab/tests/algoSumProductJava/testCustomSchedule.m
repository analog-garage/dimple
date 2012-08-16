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

function testCustomSchedule()

    %First think of a graph that requires a custom schedule using both
    %nodes and edges.  Or at least a way to see that the schedule worked.
    % a eq1 b eq2 c
    % update b
    % update eq1->a
    % update eq2->c
    % update a->eq1
    % update c->eq2
    % update eq1->b
    % update eq2->b
    % get belief of a (should be affected by b but not c)
    % get belief of c (should be affected by b but not a)
    % get belief of b (should be affected by all)
    
           
    %Build graph
    eq = @(x,y) x == y;
    fg = FactorGraph();
    a = Bit();
    b = Bit();
    c = Bit();
    eq1 = fg.addFactor(eq,a,b);
    eq2 = fg.addFactor(eq,b,c);
        
    a.Name = 'a';
    b.Name = 'b';
    c.Name = 'c';
    eq1.Name = 'eq1';
    eq2.Name = 'eq2';
   
    %Make sure we throw an error if an invalid port is updated
    valid = 1;
    fg_tmp = FactorGraph();
    b_tmp = Bit(2,1);
    f = fg_tmp.addFactor(eq,b_tmp(1),b_tmp(2));
    try
        fg_tmp.Schedule = {b_tmp(1),b_tmp(2),f,b};
    catch Err
        valid = 0;
    end
        
    assertTrue(valid==0);


    %Make sure we throw an error if not all ports updated.
    valid = 1;
    try
        fg.Schedule = {b};
    catch Err
        valid = 0;
    end
        
    assertTrue(valid==0);
    
    
    
    %define schedule
    schedule = {
        b,
        {eq1,a},
        {eq2,c},
        {a,eq1},
        {c,eq2},
        eq1,
        {eq2,b}
        };    
    
    fg.Schedule = schedule;
    
    %Set priors
    a.Input = .6;
    b.Input = .7;
    c.Input = .8;
    
    %Read beliefs
    fg.solve();
    
    %compare beliefs    
    afg = FactorGraph();
    x = Bit();
    y = Bit();
    afg.addFactor(eq,x,y);
    x.Input = a.Input(2);
    y.Input = b.Input(2);
    afg.solve();
    assertElementsAlmostEqual(a.Belief,x.Belief);
    
    afg = FactorGraph();
    x = Bit();
    y = Bit();
    afg.addFactor(eq,x,y);
    x.Input = c.Input(2);
    y.Input = b.Input(2);
    afg.solve();
    assertElementsAlmostEqual(c.Belief,x.Belief);
    
    
    z = Bit();
    afg.addFactor(eq,y,z);
    x.Input = a.Input(2);
    y.Input = b.Input(2);
    z.Input = c.Input(2);
    afg.Solver.setNumIterations(2);
    afg.solve();
    assertElementsAlmostEqual(b.Belief,y.Belief);
    
    
end
