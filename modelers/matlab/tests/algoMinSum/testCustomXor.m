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

fg = FactorGraph;
fg.Solver = 'MinSum';

x = Bit(1,3);
f = fg.addFactor('Xor', x);

% Make sure CustomXor is used by solver and that Xor factor function is
% also available to evaluate
assert(~isempty(strfind(f.Solver.toString, 'CustomXor')));
assert(~isempty(strfind(f.VectorObject.getFactorFunction.toString, 'Xor')));
assertEqual(f.VectorObject.getFactorFunction.eval({0,0,0}), 1);

dtrace(debugPrint, '--testCustomXor');
end
