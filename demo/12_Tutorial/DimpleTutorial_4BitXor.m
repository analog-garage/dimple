%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

disp(sprintf('\n++Dimple Tutorial 4 bit Xor\n'));

XorGraph = FactorGraph(); 
b = Bit(4,1); 
c = Bit(); 
XorGraph.addFactor(@xorDeltaTutorial,b(1),b(2),c); 
XorGraph.addFactor(@xorDeltaTutorial,b(3),b(4),c); 
b.Input = [ .8 .8 .8 .5];
XorGraph.Solver.setNumIterations(2);
XorGraph.solve();
disp(b.Belief); 
disp(b.Value);

disp(sprintf('--Dimple Tutorial 4 bit Xor\n'));
