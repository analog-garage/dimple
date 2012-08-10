function testDefaultTreeSchdule()
    fg = FactorGraph();
    b = Bit(3,1);
    eq = @(x,y) x == y;
    fg.addFactor(eq,b(1),b(2));
    fg.addFactor(eq,b(2),b(3));
    b(1).Input = .8;
    
    fg.solve();
    
    assertElementsAlmostEqual(b(3).Belief,.8);
end
