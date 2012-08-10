%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

clc

N = 4;
aGraph = createAdder(N,N+1);

g = newGraph();

x = addBit(g,N);
y = addBit(g,N);
z = addBit(g,N+1);
c = addBit(g,N+2);

addGraph(g,aGraph,x,y,z,c);

xval = 10;
yval = 8;
xvec = num2vec(xval,N);
yvec = num2vec(yval,N);
setBitInput(x,xvec);
setBitInput(y,yvec);
setBitInput(c(1),0);
%setBitInput(c(end),0);

setNumIterations(g,10);
solve(g);

zvec = getBelief(z);
zvec = zvec(:,1);
z = vec2num(zvec);
z
