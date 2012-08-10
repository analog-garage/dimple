%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

disp(sprintf('\n++Dimple Tutorial BadNBitXor\n'));

FourBitXor=FactorGraph();
Domain=[0;1];
B1=Variable(Domain);
B2=Variable(Domain);
B3=Variable(Domain);
B4=Variable(Domain);
FourBitXor.addFactor(@BadNBitXorDelta_Tutorial,[B1,B2,B3,B4]);
B1.Input=[0.2 0.8];
B2.Input=[0.2 0.8];
B3.Input=[0.2 0.8];
B4.Input=[0.5 0.5];
FourBitXor.Solver.setNumIterations(30);
FourBitXor.solve;
disp(B1.Value);

disp(sprintf('\n--Dimple Tutorial BadNBitXor\n'));
