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

function testGetValues()

debugPrint = false;

dtrace(debugPrint, '++testOperatorOverloadingGibbs');

test1(debugPrint);
test2(debugPrint);
test3(debugPrint);
test4(debugPrint);

dtrace(debugPrint, '--testOperatorOverloadingGibbs');

end

% Real variables
function test1(debugPrint)

fg = FactorGraph();
fg.Solver = 'Gaussian';

a = Real();
b = Real();

c = a + b;

a.Input = [3, 1];
b.Input = [2, 2];

fg.solve();

assertElementsAlmostEqual(c.Value, 5, 'absolute');
assertElementsAlmostEqual(c.Belief(1), 5, 'absolute');


end


% Real-joint variables
function test2(debugPrint)

fg = FactorGraph();
fg.Solver = 'Gaussian';

a = Complex();
b = Complex();

c = a + b;

a.Input = MultivariateMsg([3 2], eye(2));
b.Input = MultivariateMsg([2 7], eye(2));

fg.solve();

assertElementsAlmostEqual(c.Value, 5 + 1i*9, 'absolute', 1e-5);
assertElementsAlmostEqual(c.Belief.Means, [5; 9], 'absolute', 1e-5);

end



% Real variable arrays
function test3(debugPrint)

fg = FactorGraph();
fg.Solver = 'Gaussian';

a = Real(2,3,4);
b = Real(2,3,4);

c = a + b;

% Array Input for Real varaibles doesn't yet work, do use loops
ain = rand(2,3,4);
bin = rand(2,3,4);
for i=1:2
    for j=1:3
        for k=1:4
            a(i,j,k).Input = [ain(i,j,k) 1];
            b(i,j,k).Input = [bin(i,j,k) 1];
        end
    end
end

fg.solve();

assertElementsAlmostEqual(c.Value, ain(:,:,:) + bin(:,:,:), 'absolute');
cBelief = c.Belief;
for i=1:2
    for j=1:3
        for k=1:4
            assertElementsAlmostEqual(cBelief{i,j,k}(1), ain(i,j,k) + bin(i,j,k), 'absolute');
        end
    end
end


end


% Real-joint variable arrays
function test4(debugPrint)

fg = FactorGraph();
fg.Solver = 'Gaussian';

a = Complex(2,3,4);
b = Complex(2,3,4);

c = a + b;

% Array Input for Real varaibles doesn't yet work, do use loops
arin = rand(2,3,4);
aiin = rand(2,3,4);
brin = rand(2,3,4);
biin = rand(2,3,4);
for i=1:2
    for j=1:3
        for k=1:4
            a(i,j,k).Input = MultivariateMsg([arin(i,j,k) aiin(i,j,k)], eye(2));
            b(i,j,k).Input = MultivariateMsg([brin(i,j,k) biin(i,j,k)], eye(2));
        end
    end
end

fg.solve();

assertElementsAlmostEqual(c.Value, (arin + brin) + 1i*(aiin + biin), 'absolute', 1e-5);
cBelief = c.Belief;
for i=1:2
    for j=1:3
        for k=1:4
            assertElementsAlmostEqual(cBelief{i,j,k}.Means, [arin(i,j,k) + brin(i,j,k); aiin(i,j,k) + biin(i,j,k)] , 'absolute', 1e-5);
        end
    end
end

end
