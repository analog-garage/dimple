%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

disp(sprintf('\n++Dimple Tutorial Nested\n'));


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
% Define 4 bit xor from two 3 bit xors 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
b = Bit(4,1);
XorGraph = FactorGraph(b); 
c = Bit(); 
XorGraph.addFactor(@xorDeltaTutorial,b(1),b(2),c); 
XorGraph.addFactor(@xorDeltaTutorial,b(3),b(4),c);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
% Create graph for 6 bit code 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
d = Bit(6,1);
MyGraph = FactorGraph(d); 
MyGraph.addFactor(XorGraph,d([1:3 5])); 
MyGraph.addFactor(XorGraph,d([1 2 4 6]));
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
% Set input and Solve 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
d.Input = [.75 .6 .9 .1 .2 .9]; 
MyGraph.Solver.setNumIterations(20);
MyGraph.solve();
disp(d.Value');

disp(sprintf('\n--Dimple Tutorial Nested\n'));
