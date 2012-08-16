%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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
