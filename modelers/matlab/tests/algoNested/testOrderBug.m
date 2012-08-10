function testOrderBug()

    b = Bit(4,1);
    fourXor = FactorGraph(b);
    fourXor.addFactor(@xorDelta,b(1:3));
    fourXor.addFactor(@xorDelta,b(2:4));

    b = Bit(6,1);
    fg = FactorGraph(b);
    g = fg.addFactor(fourXor,b(1:4));
    fg.addFactor(fourXor,b(3:6));

    fg2 = FactorGraph();
    b = Bit(8,1);
    g = fg2.addFactor(fg,b(1:6));
    fg2.addFactor(fg,b(3:8));

    b.Input = ones(8,1)*.8;
    fg2.Solver.setNumIterations(100);
    fg2.solve();

    
    expected = [1 1 0 1 1 0 1 1]';
    assertEqual(expected,b.Value);
end
