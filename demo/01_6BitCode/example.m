%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Define 4 bit xor from two 3 bit xors
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
b = Bit(4,1);
%v = newVariable(4,1);
%setDomain(v,[1 0]);

XorGraph = FactorGraph(b);
c = Bit;
XorGraph.addFactor(@xorDelta_01_6BitCode,b(1),b(2),c);
XorGraph.addFactor(@xorDelta_01_6BitCode,b(3),b(4),c);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Create graph for 6 bit code
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
d = Bit(6,1);
MyGraph = FactorGraph(d);
MyGraph.addFactor(XorGraph,d([1:3 5]));
MyGraph.addFactor(XorGraph,d([1 2 4 6]));
    


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Set priors and Solve
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
priors =  [.75 .6 .9 .1 .2 .9]';
d.Input = priors;
MyGraph.Solver.setNumIterations(20);
MyGraph.solve();
disp((d.Belief>.5)');
d.Belief
