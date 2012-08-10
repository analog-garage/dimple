function testParameter

    v = Bit();
    ng = FactorGraph(v);
    ng.addFactor(@constFactor,v,[.6 .4]);

    fg = FactorGraph();
    v = Bit();
    v.Input = .4;
    fg.addRepeatedFactor(ng,v);

    fg.initialize();

    fg2 = FactorGraph();
    b = Bit();
    b.Input = .4;

    for i = 1:10
        fg2.addFactor(@constFactor,b,[.6 .4]);
        fg2.solve();
        fg.solve(false);

        assertElementsAlmostEqual(b.Belief,v.Belief);

        fg.advance();
    end
    
    assertEqual(1,length(fg.Variables));
    assertEqual(2,length(fg.Factors));
    
end
