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
