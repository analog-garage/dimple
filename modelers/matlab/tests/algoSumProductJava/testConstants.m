
b = Bit(2,1);
fg = FactorGraph();
fg.addFactor(com.analog.lyric.dimple.FactorFunctions.XorDelta(),b,1);
b(1).Input = .8;
fg.solve();
b(2).Belief
assertElementsAlmostEqual(b(2).Belief,.2);



b = Bit(2,1);
fg = FactorGraph();
fg.addFactor(@xorDelta,b,[1 0]);
b(1).Input = .8;
fg.solve();
assertElementsAlmostEqual(b(2).Belief,.2);
