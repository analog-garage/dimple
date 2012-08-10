function testNumIterations()
    fg = FactorGraph();
    fg.NumIterations = 20;
    assertEqual(20,fg.NumIterations);

end
