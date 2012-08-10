function testCustomScheduleNested()

    eq = @(x,y) x == y;
    b = Bit(2,1);
    nfg = FactorGraph(b);
    nfg.addFactor(eq,b(1),b(2));

    b = Bit(3,1);
    fg = FactorGraph();
    nf1 = fg.addFactor(nfg,b(1),b(2));
    nf2 = fg.addFactor(nfg,b(2),b(3));
    
    
    fg.Schedule = {b(1),nf1,b(2),nf2,b(3)};
    b(1).Input = .7;
    fg.solve();
    
    assertElementsAlmostEqual(b(3).Belief,b(1).Input(2));
       
    
    fg.Schedule = {b(1),nf1,nf2,b(2),b(3)};
    fg.solve();
    assertElementsAlmostEqual(b(3).Belief,.5);
    
    
    
    waserror = 0;
    try
        fg.Schedule = {b(1),b(2),b(3)};
    catch Err
        waserror = 1;
    end
    
    assertTrue(waserror==1);
    
    
    %Now let's try nesting with a custom schedule on the nested graph.
    
    %Create a graph to nest and give it a funny schedule    
    % nfg: eb(1) - f1 - ib - f2 - eb(2)
    eb = Bit(2,1);
    ib = Bit();

    nfg = FactorGraph(eb);
    f1 = nfg.addFactor(eq,eb(1),ib);
    f2 = nfg.addFactor(eq,ib,eb(2));

    %Set an input and solve
    eb(1).Input = .8;
    
    nfg.solve();
    
    %We expect the output to be equal to the input since the tree
    %scheduler passes the info along.
    assertElementsAlmostEqual(eb(2).Belief,eb(1).Input(2));
    
    %Now we create a schedule that will not propogate the info.
    nfg.Schedule = {ib,{f1,eb(1)},{f2,eb(2)},eb(1),eb(2),f1,f2};
    nfg.solve();
    
    assertElementsAlmostEqual(eb(2).Belief,.5);

    
    %Nest it and see if the schedule is preserved
    b = Bit(2,1);
    fg = FactorGraph();
    g = fg.addFactor(nfg,b);
    
    fg.Schedule = {b(1),b(2),g};
    
    b(1).Input = .8;
    fg.solve();
    assertElementsAlmostEqual(b(2).Belief,.5);
    
end

