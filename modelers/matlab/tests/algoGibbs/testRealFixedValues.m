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

function testRealFixedValues()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testRealFixedValues');

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);
test3(debugPrint, repeatable);
test4(debugPrint, repeatable);
test5(debugPrint, repeatable);

dtrace(debugPrint, '--testRealFixedValues');

end

% Real variables
function test1(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(10);

a = Real();
b = Real();

c = a + b;

% Run first with inputs and no fixed value
a.Input = FactorFunction('Normal',3,5);
b.Input = FactorFunction('Normal',4,1);

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.solve();

as = a.Solver.getCurrentSample;
assert(~a.hasFixedValue);

% Now, set a fixed value and run again
a.FixedValue = 2.7;
fg.solve();

af = a.Solver.getCurrentSample;

assert(as ~= af);
assertElementsAlmostEqual(af, 2.7, 'absolute');
assertElementsAlmostEqual(a.FixedValue, 2.7, 'absolute');
assert(a.hasFixedValue);

% Now set the input again, and run again
a.Input = FactorFunction('Normal',3,5);
assert(~a.hasFixedValue);

fg.solve();
as2 = a.Solver.getCurrentSample;
assert(as2 ~= af);

end


% Real-joint variables
function test2(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

a = Complex();
b = Complex();

c = a + b;

% Run first with inputs and no fixed value
a.Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-1,1)};
b.Input = {FactorFunction('Normal',4,1), FactorFunction('Normal',6,2)};

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.Solver.setNumSamples(10);
fg.solve();

as = a.Solver.getCurrentSample;
assert(~a.hasFixedValue);

% Now, set a fixed value and run again
a.FixedValue = 2.7 + 1i*1.2;
fg.solve();

af = a.Solver.getCurrentSample;

assert(all(as ~= af));
assertElementsAlmostEqual(af, [2.7; 1.2], 'absolute');
assertElementsAlmostEqual(a.FixedValue, 2.7 + 1i*1.2, 'absolute');
assert(a.hasFixedValue);

% Now set the input again, and run again
a.Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-1,1)};
assert(~a.hasFixedValue);

fg.solve();
as2 = a.Solver.getCurrentSample;
assert(all(as2 ~= af));


end



% Real variable arrays
function test3(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

a = Real(2,3,4);
b = Real(2,3,4);

c = a + b;

% Run first with inputs and no fixed value
% Array Input for Real varaibles doesn't yet work, do use loops
for i=1:2
    for j=1:3
        for k=1:4
            a(i,j,k).Input = FactorFunction('Normal',3,5);
            b(i,j,k).Input = FactorFunction('Normal',4,1);
        end
    end
end

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.Solver.setNumSamples(10);
fg.solve();

as = a.invokeSolverMethodWithReturnValue('getCurrentSample');
hfv = a.hasFixedValue;
assert(all(~hfv(:)));

% Now, set a fixed value and run again
fv = rand(2,3,4);
a.FixedValue = fv;
fg.solve();

af = a.invokeSolverMethodWithReturnValue('getCurrentSample');

cellfun(@(x,y)assert(x ~= y), as, af);
assertElementsAlmostEqual(cell2mat(af), fv, 'absolute');
assertElementsAlmostEqual(a.FixedValue, fv, 'absolute');
hfv2 = a.hasFixedValue;
assert(all(hfv2(:)));

% Now set the input again, and run again
for i=1:2
    for j=1:3
        for k=1:4
            a(i,j,k).Input = FactorFunction('Normal',3,5);
        end
    end
end
hfv3 = a.hasFixedValue;
assert(all(~hfv3(:)));

fg.solve();
as2 = a.invokeSolverMethodWithReturnValue('getCurrentSample');
cellfun(@(x,y)assert(x ~= y), as2, af);


end


% Real-joint variable arrays
function test4(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

a = Complex(2,3,4);
b = Complex(2,3,4);

c = a + b;

% Run first with inputs and no fixed value
% Array Input for Real varaibles doesn't yet work, do use loops
for i=1:2
    for j=1:3
        for k=1:4
            a(i,j,k).Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-1,1)};
            b(i,j,k).Input = {FactorFunction('Normal',2,5), FactorFunction('Normal',-2,1)};
        end
    end
end

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.Solver.setNumSamples(10);
fg.solve();

as = a.invokeSolverMethodWithReturnValue('getCurrentSample');
hfv = a.hasFixedValue;
assert(all(~hfv(:)));

% Now, set a fixed value and run again
fv = rand(2,3,4) + 1i*rand(2,3,4);
a.FixedValue = fv;
fg.solve();

af = a.invokeSolverMethodWithReturnValue('getCurrentSample');

cellfun(@(x,y)assert(all(x ~= y)), as, af);
afr = cellfun(@(x)x(1), af, 'UniformOutput', false);
afi = cellfun(@(x)x(2), af, 'UniformOutput', false);
assertElementsAlmostEqual(cell2mat(afr) + 1i*cell2mat(afi), fv, 'absolute');
assertElementsAlmostEqual(a.FixedValue, fv, 'absolute');
hfv2 = a.hasFixedValue;
assert(all(hfv2(:)));

% Now set the input again, and run again
for i=1:2
    for j=1:3
        for k=1:4
            a(i,j,k).Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-1,1)};
        end
    end
end
hfv3 = a.hasFixedValue;
assert(all(~hfv3(:)));

fg.solve();
as2 = a.invokeSolverMethodWithReturnValue('getCurrentSample');
cellfun(@(x,y)assert(all(x ~= y)), as2, af);


end



% Real-joint variable arrays (same as test4, but 2D case)
function test5(debugPrint, repeatable)

fg = FactorGraph();
fg.Solver = 'Gibbs';

a = Complex(2,3);
b = Complex(2,3);

c = a + b;

% Run first with inputs and no fixed value
% Array Input for Real varaibles doesn't yet work, do use loops
for i=1:2
    for j=1:3
        a(i,j).Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-1,1)};
        b(i,j).Input = {FactorFunction('Normal',2,5), FactorFunction('Normal',-2,1)};
    end
end

if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end

fg.Solver.setNumSamples(10);
fg.solve();

as = a.invokeSolverMethodWithReturnValue('getCurrentSample');
hfv = a.hasFixedValue;
assert(all(~hfv(:)));

% Now, set a fixed value and run again
fv = rand(2,3) + 1i*rand(2,3);
a.FixedValue = fv;
fg.solve();

af = a.invokeSolverMethodWithReturnValue('getCurrentSample');

cellfun(@(x,y)assert(all(x ~= y)), as, af);
afr = cellfun(@(x)x(1), af, 'UniformOutput', false);
afi = cellfun(@(x)x(2), af, 'UniformOutput', false);
assertElementsAlmostEqual(cell2mat(afr) + 1i*cell2mat(afi), fv, 'absolute');
assertElementsAlmostEqual(a.FixedValue, fv, 'absolute');
hfv2 = a.hasFixedValue;
assert(all(hfv2(:)));

% Now set the input again, and run again
for i=1:2
    for j=1:3
        a(i,j).Input = {FactorFunction('Normal',3,5), FactorFunction('Normal',-1,1)};
    end
end
hfv3 = a.hasFixedValue;
assert(all(~hfv3(:)));

fg.solve();
as2 = a.invokeSolverMethodWithReturnValue('getCurrentSample');
cellfun(@(x,y)assert(all(x ~= y)), as2, af);


end
