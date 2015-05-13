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

function testProxyWrapping()
%TESTPROXYWRAPPING Tests for wrapping of Dimple objects with proxy objects

assert(isempty(wrapProxyObject([])));

assert(42 == wrapProxyObject(42));

domain = DiscreteDomain(1:2);
domain2 = wrapProxyObject(domain.IDomain);
assert(isa(domain2,'DiscreteDomain'));
assert(domain.IDomain == domain2.IDomain);

domain = RealDomain(0,10);
domain2 = wrapProxyObject(domain.IDomain);
assert(isa(domain2,'RealDomain'));
assert(domain.IDomain == domain2.IDomain);

% TODO: more test cases...

% Make sure that variables from factors have correctly wrapped domains.
fg = FactorGraph();
fg.addFactor(@xorDelta, Discrete(1:2,1,2));
assert(all(cell2mat(fg.Variables{1}.Domain.Elements(:)) == [1;2]));
assert(all(cell2mat(fg.Factors{1}.Variables{1}.Domain.Elements(:)) == [1;2]));

end

