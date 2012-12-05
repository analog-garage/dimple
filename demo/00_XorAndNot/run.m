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
global DIMPLE_TEST_VERBOSE;
silent = false;
if (exist('DIMPLE_TEST_VERBOSE','var')); if (~DIMPLE_TEST_VERBOSE); silent = true; end; end;


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
if (~silent)
    disp(w.Belief);
    disp(x.Belief);
    disp(y.Belief);
    disp(z.Belief);
end


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Set names and plot
x.Name = 'x';
w.Name = 'w';
y.Name = 'y';
z.Name = 'z';
f1.Name = '!';
f2.Name = 'xor';

g.plot('labels',1);

