

ng = FactorGraph(a,b);
ng.addFactor(@xorDelta,a,b);
p = Bit();
s = BitStream();
fg = FactorGraph();
fgs = fg.addFactor(ng,p,s);
fgs.BufferSize = 5;
fg.plot();
