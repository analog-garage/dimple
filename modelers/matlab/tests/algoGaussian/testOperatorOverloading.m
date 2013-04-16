%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
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

function testOperatorOverloading()

    a = Real();
    b = Real();

    fg = FactorGraph();
    fg.Solver = 'Gaussian';

    % Can overload + and *-constant operators with Gaussian custom factors
    z = (a + 2.7 * b) * 17;

    mus = [8 10];
    sigmas = [1 2];
    a.Input = [mus(1) sigmas(1)];
    b.Input = [mus(2) sigmas(2)];

    fg.solve();

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %Now compare against non-overloaded

    a2 = Real();
    b2 = Real();
    x2 = Real();
    y2 = Real();
    z2 = Real();

    fg2 = FactorGraph();
    fg2.Solver = 'Gaussian';
    
    fg2.addFactor('constmult', x2, b2, 2.7);
    fg2.addFactor('add', y2, x2, a2);
    fg2.addFactor('constmult', z2, y2, 17);

    a2.Input = [mus(1) sigmas(1)];
    b2.Input = [mus(2) sigmas(2)];

    fg2.solve();

    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % Check results
    assertEqual(a.Belief(1),a2.Belief(1));
    assertEqual(a.Belief(2),a2.Belief(2));
    assertEqual(b.Belief(1),b2.Belief(1));
    assertEqual(b.Belief(2),b2.Belief(2));
    assertEqual(z.Belief(1),z2.Belief(1));
    assertEqual(z.Belief(2),z2.Belief(2));

end

