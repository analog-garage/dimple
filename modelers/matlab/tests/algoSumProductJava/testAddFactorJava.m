

myxor = com.analog.lyric.dimple.FactorFunctions.lib.XorDelta();

fg = FactorGraph();

b = Bit(3,1);

fg.addFactor(myxor,b(1),b(2),b(3));

b(1).Input = .8;
b(2).Input = .8;

fg.solve();


fg2 = FactorGraph();
b2 = Bit(3,1);

fg2.addFactor(@xorDelta,b2);

b2.Input = [.8 .8 .5];

fg2.solve();


assertElementsAlmostEqual(b(3).Belief,b2(3).Belief);
