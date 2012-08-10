function testAddTable()

    %First let's just add the factor as indices and values
    fg = FactorGraph();
    b = Bit(2,1);
    ind = [0 0; 1 1];
    val = [1 1];
    fg.addFactor(ind,val,b);
    b(1).Input = .8;
    fg.Solver.setNumIterations(2);
    fg.solve();
    assertEqual(b(2).Belief,.8);

    %Now let's add the Factor using createTable to test we can share tables
    fg = FactorGraph();
    b = Bit(2,1);   
    t = fg.createTable(ind,val,b.Domain,b.Domain);
    fg.addFactor(t,b);
    b(1).Input = .8;
    fg.Solver.setNumIterations(2);
    fg.solve();
    assertEqual(b(2).Belief,.8);
    
end
