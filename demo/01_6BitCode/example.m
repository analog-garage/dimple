%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
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
