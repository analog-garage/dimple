%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012-2014 Analog Devices, Inc.
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

function testCustomXor()

debugPrint = false;

dtrace(debugPrint, '++testCustomXor');

test1(debugPrint);
test2(debugPrint);
test3(debugPrint);

dtrace(debugPrint, '--testCustomXor');

end


% Test that custom function is created
function test1(debugPrint)

fg = FactorGraph;
fg.Solver = 'MinSum';

x = Bit(1,3);
f = fg.addFactor('Xor', x);

% Make sure CustomXor is used by solver and that Xor factor function is
% also available to evaluate
assert(~isempty(strfind(f.Solver.toString, 'CustomXor')));
assert(~isempty(strfind(f.VectorObject.getFactorFunction.toString, 'Xor')));
assertEqual(f.VectorObject.getFactorFunction.eval({0,0,0}), 1);

end

% Test including constants
function test2(debugPrint)

x = Bit(1,25);

fg = FactorGraph;
fg.Solver = 'MinSum';

% Add factors with various number and order of constants
f1 = fg.addFactor('Xor', x(1:3));
f2 = fg.addFactor('Xor', x(4), x(5), x(6), 1, 0, 1, 1, 0, 1);
f3 = fg.addFactor('Xor', x(7), x(8), x(9), 1, 0, 0, 1, 0, 1);
f4 = fg.addFactor('Xor', x(10), x(11), 1, 0, 1, x(12), 0, 1);
f5 = fg.addFactor('Xor', x(13), x(14), 1, 0, 1, x(15), 0, 1, 1);
f6 = fg.addFactor('Xor', 1, x(16), 0, x(17), 1, 0, 1, x(18), 0, 1, 1);
f7 = fg.addFactor('Xor', 1, x(19), 0, x(20), 1, 0, 1, x(21), 0, 1, 0);
f8 = fg.addFactor('Xor', x(22:25));

% Repeat the same inputs, except for the last value, force to 1
x.Input = [repmat([0.2 0.3 0.9], 1, 8) 1];

fg.solve();

b = x.Belief;

assertElementsAlmostEqual(b(4:6), b(1:3));      % Constants are parity 1
assertElementsAlmostEqual(b(7:9), b(22:24));    % Constants are parity 0
assertElementsAlmostEqual(b(10:12), b(22:24));  % Constants are parity 0
assertElementsAlmostEqual(b(13:15), b(4:6));    % Constants are parity 1
assertElementsAlmostEqual(b(16:18), b(22:24));  % Constants are parity 1
assertElementsAlmostEqual(b(19:21), b(4:6));    % Constants are parity 0

end


% Compare to non-custom factor function
function test3(debugPrint)

x = Bit(1,6);

fg = FactorGraph;
fg.Solver = 'MinSum';

% Add factors with various number and order of constants
f1 = fg.addFactor('Xor', x(1:3));
f2 = fg.addFactor(@xorDelta, x(4:6));   % Use MATLAB function

% Make sure one is custom and the other is not
assert(~isempty(strfind(f1.Solver.toString, 'CustomXor')));
assert(~isempty(strfind(f2.Solver.toString, 'STableFactor')));

% Repeat the same inputs, except for the last value, force to 1
x.Input = repmat([0.2 0.3 0.9], 1, 2);

fg.solve();

b = x.Belief;

assertElementsAlmostEqual(b(4:6), b(1:3));

end
