function testRemoveFactor()

    fg = FactorGraph();
    b = Bit(3,1);
    f1 = fg.addFactor(@xorDelta,b(1:2)); %#ok<NASGU>
    f2 = fg.addFactor(@xorDelta,b(2:3));

    b.Input = [.8 .8 .6];

    fg.Solver.setNumIterations(2);
    fg.solve();

    assertElementsAlmostEqual([.96 .96 .96]',b.Belief);

    fg.removeFactor(f2);

    fg.solve();

    p1 = .8*.8;
    p0 = .2*.2;
    total = p1+p0;
    p1 = p1/total;
    p0 = p0/total; %#ok<NASGU>
    assertElementsAlmostEqual([p1 p1 .6]',b.Belief);
    
end
