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

function testFixedValues()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testFixedValues');

if (repeatable)
    seed = 14;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint);
test2(debugPrint);
test3(debugPrint);
test4(debugPrint);

dtrace(debugPrint, '--testFixedValues');

end

% Real variables
function test1(debugPrint)

fg = FactorGraph();
fg.Solver = 'Gaussian';

a = Real();
b = Real();

c = a + b;

% Run first with inputs and no fixed value
a.Input = [3, 1];
b.Input = [2, 2];
fg.solve();

assert(~a.hasFixedValue);
assertElementsAlmostEqual(c.Value, 5, 'absolute');
assertElementsAlmostEqual(c.Belief(1), 5, 'absolute');
assertElementsAlmostEqual(a.Input, [3; 1], 'absolute');
assertElementsAlmostEqual(b.Input, [2; 2], 'absolute');

% Now, set a fixed value and run again
a.FixedValue = 7;
fg.solve();

assert(a.hasFixedValue);
assert(isempty(a.Input));
assertElementsAlmostEqual(a.Belief, [7; 0], 'absolute');
assertElementsAlmostEqual(c.Belief, [9; 2], 'absolute');

% Now set the input again, and run again
a.Input = [4, 1];
fg.solve();

assert(~a.hasFixedValue);
assertElementsAlmostEqual(c.Value, 6, 'absolute');
assertElementsAlmostEqual(c.Belief(1), 6, 'absolute');
assertElementsAlmostEqual(a.Input, [4; 1], 'absolute');
assertElementsAlmostEqual(b.Input, [2; 2], 'absolute');

end


% Real-joint variables
function test2(debugPrint)

fg = FactorGraph();
fg.Solver = 'Gaussian';

a = Complex();
b = Complex();

c = a + b;

% Run first with inputs and no fixed value
a.Input = MultivariateMsg([3 2], eye(2));
b.Input = MultivariateMsg([2 7], eye(2));
fg.solve();

assert(~a.hasFixedValue);
assertElementsAlmostEqual(c.Value, 5 + 1i*9, 'absolute', 1e-5);
assertElementsAlmostEqual(c.Belief.Means, [5; 9], 'absolute', 1e-5);
assertElementsAlmostEqual(a.Input.getMeans(), [3; 2], 'absolute');
assertElementsAlmostEqual(b.Input.getMeans(), [2; 7], 'absolute');

% Now, set a fixed value and run again
a.FixedValue = 7 + 1i*1.4;
fg.solve();

assert(a.hasFixedValue);
assert(isempty(a.Input));
assertElementsAlmostEqual(a.Belief.Means, [7; 1.4], 'absolute', 1e-5);
assertElementsAlmostEqual(c.Belief.Means, [9; 8.4], 'absolute', 1e-5);
assert(all(a.Belief.Covariance(:)==0));
assertElementsAlmostEqual(c.Belief.Covariance, eye(2), 'absolute', 1e-5);

% Now set the input again, and run again
a.Input = MultivariateMsg([4 3], eye(2));
fg.solve();

assert(~a.hasFixedValue);
assertElementsAlmostEqual(c.Value, 6 + 1i*10, 'absolute', 1e-5);
assertElementsAlmostEqual(c.Belief.Means, [6; 10], 'absolute', 1e-5);
assertElementsAlmostEqual(a.Input.getMeans(), [4; 3], 'absolute');
assertElementsAlmostEqual(b.Input.getMeans(), [2; 7], 'absolute');

end



% Real variable arrays
function test3(debugPrint)

fg = FactorGraph();
fg.Solver = 'Gaussian';

a = Real(2,3,4);
b = Real(2,3,4);

c = a + b;

% Run first with inputs and no fixed value
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

hfv = a.hasFixedValue;
assert(all(~hfv(:)));
assertElementsAlmostEqual(c.Value, ain(:,:,:) + bin(:,:,:), 'absolute');
cBelief = c.Belief;
aInput = a.Input;
bInput = b.Input;
for i=1:2
    for j=1:3
        for k=1:4
            assertElementsAlmostEqual(cBelief{i,j,k}(1), ain(i,j,k) + bin(i,j,k), 'absolute');
            assertElementsAlmostEqual(aInput{i,j,k}(1), ain(i,j,k), 'absolute');
            assertElementsAlmostEqual(bInput{i,j,k}(1), bin(i,j,k), 'absolute');
        end
    end
end

% Now, set a fixed value and run again
afv = rand(2,3,4);
a.FixedValue = afv;
fg.solve();

hfv = a.hasFixedValue;
assert(all(hfv(:)));
aie = cellfun(@(x)isempty(x),a.Input);
assert(all(aie(:)));
aBelief = a.Belief;
cBelief = c.Belief;
for i=1:2
    for j=1:3
        for k=1:4
            assertElementsAlmostEqual(aBelief{i,j,k}, [afv(i,j,k); 0], 'absolute');
            assertElementsAlmostEqual(cBelief{i,j,k}, [afv(i,j,k) + bin(i,j,k); 1], 'absolute');
        end
    end
end

% Now set the input again, and run again
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

hfv = a.hasFixedValue;
assert(all(~hfv(:)));
assertElementsAlmostEqual(c.Value, ain(:,:,:) + bin(:,:,:), 'absolute');
cBelief = c.Belief;
aInput = a.Input;
bInput = b.Input;
for i=1:2
    for j=1:3
        for k=1:4
            assertElementsAlmostEqual(cBelief{i,j,k}(1), ain(i,j,k) + bin(i,j,k), 'absolute');
            assertElementsAlmostEqual(aInput{i,j,k}(1), ain(i,j,k), 'absolute');
            assertElementsAlmostEqual(bInput{i,j,k}(1), bin(i,j,k), 'absolute');
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

% Run first with inputs and no fixed value
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

hfv = a.hasFixedValue;
assert(all(~hfv(:)));
assertElementsAlmostEqual(c.Value, (arin + brin) + 1i*(aiin + biin), 'absolute', 1e-5);
cBelief = c.Belief;
aInput = a.Input;
bInput = b.Input;
for i=1:2
    for j=1:3
        for k=1:4
            assertElementsAlmostEqual(cBelief{i,j,k}.Means, [arin(i,j,k) + brin(i,j,k); aiin(i,j,k) + biin(i,j,k)] , 'absolute', 1e-5);
            assertElementsAlmostEqual(aInput{i,j,k}.getMeans(), [arin(i,j,k); aiin(i,j,k)], 'absolute');
            assertElementsAlmostEqual(bInput{i,j,k}.getMeans(), [brin(i,j,k); biin(i,j,k)], 'absolute');
        end
    end
end

% Now, set a fixed value and run again
afv = rand(2,3,4) + 1i*rand(2,3,4);
a.FixedValue = afv;
fg.solve();

hfv = a.hasFixedValue;
assert(all(hfv(:)));
aie = cellfun(@(x)isempty(x),a.Input);
assert(all(aie(:)));
aBelief = a.Belief;
cBelief = c.Belief;
for i=1:2
    for j=1:3
        for k=1:4
            assertElementsAlmostEqual(aBelief{i,j,k}.Means, [real(afv(i,j,k)); imag(afv(i,j,k))] , 'absolute', 1e-5);
            assertElementsAlmostEqual(cBelief{i,j,k}.Means, [real(afv(i,j,k)) + brin(i,j,k); imag(afv(i,j,k)) + biin(i,j,k)] , 'absolute', 1e-5);
            assertElementsAlmostEqual(aBelief{i,j,k}.Covariance, zeros(2) , 'absolute');
            assertElementsAlmostEqual(cBelief{i,j,k}.Covariance, eye(2) , 'absolute', 1e-5);
        end
    end
end

% Now set the input again, and run again
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

hfv = a.hasFixedValue;
assert(all(~hfv(:)));
assertElementsAlmostEqual(c.Value, (arin + brin) + 1i*(aiin + biin), 'absolute', 1e-5);
cBelief = c.Belief;
aInput = a.Input;
bInput = b.Input;
for i=1:2
    for j=1:3
        for k=1:4
            assertElementsAlmostEqual(cBelief{i,j,k}.Means, [arin(i,j,k) + brin(i,j,k); aiin(i,j,k) + biin(i,j,k)] , 'absolute', 1e-5);
            assertElementsAlmostEqual(aInput{i,j,k}.getMeans(), [arin(i,j,k); aiin(i,j,k)], 'absolute');
            assertElementsAlmostEqual(bInput{i,j,k}.getMeans(), [brin(i,j,k); biin(i,j,k)], 'absolute');
        end
    end
end

end


