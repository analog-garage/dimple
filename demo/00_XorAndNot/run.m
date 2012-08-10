%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2011, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Create graph
g = FactorGraph();
x = Bit();
w = Bit();
y = Bit();
z = Bit();

f1 = g.addFactor(@notdelta,x,w);
f2 = g.addFactor(@myxor,w,y,z);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Set priors and solve
x.Input = .75;
w.Input = .75;
y.Input = .75;
z.Input = .75;

g.solve();

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Get results and display
disp(w.Belief);
disp(x.Belief);
disp(y.Belief);
disp(z.Belief);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Set names and plot
x.Name = 'x';
w.Name = 'w';
y.Name = 'y';
z.Name = 'z';
f1.Name = '!';
f2.Name = 'xor';

g.plot('labels',1);

% -------------------------------------------------------------------------

% Do not modify. For automated testing purposes.
global Dimple_DEMO_RESULT;
if abs(w.Belief - 0.375) < 1e-10 && ...
   abs(x.Belief - 0.625) < 1e-10 && ...
   abs(y.Belief - 0.75)  < 1e-10 && ...
   abs(z.Belief - 0.75)  < 1e-10
    Dimple_DEMO_RESULT = 0;
else
    Dimple_DEMO_RESULT = 1;
end
