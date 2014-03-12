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

function testLinear()

test1();
test2();
test3();
test4();
test5();

end


% Original linear custom factor (for backward compatibility)
function test1()

fg = FactorGraph();
fg.Solver = 'Gaussian';

a = Real();
b = Real();
c = Real();
consts = [1 2 3];
total = 6;
fl = fg.addFactor(@linear,a,b,c,consts,total);
a.Input = [4 1];
b.Input = [10 1];
c.Input = [12 100];
assert(~isempty(strfind(fl.Solver.toString, 'CustomGaussianLinear')));

fg.solve();


fg2 = FactorGraph();
fg2.Solver = 'Gaussian';
a2 = Real();
b2 = Real();
c2 = Real();

a2m = Real();
b2m = Real();
c2m = Real();
d = Real();
d.Input = [6 1e-9];
fm1 = fg2.addFactor(@constmult,a2m,a2,consts(1));
fm2 = fg2.addFactor(@constmult,b2m,b2,consts(2));
fm3 = fg2.addFactor(@constmult,c2m,c2,consts(3));
a2.Input = a.Input;
b2.Input = b.Input;
c2.Input = c.Input;
fa = fg2.addFactor(@add,d,a2m,b2m,c2m);
fg2.solve();
assert(~isempty(strfind(fm1.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fm2.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fm3.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fa.Solver.toString, 'CustomGaussianSum')));


assertElementsAlmostEqual(a.Belief,a2.Belief);
assertElementsAlmostEqual(b.Belief,b2.Belief);
assertElementsAlmostEqual(c.Belief,c2.Belief);
end



% New LinearEquation custom factor
function test2()

fg = FactorGraph();

a = Real();
b = Real();
c = Real();
consts = [1 2 3];
total = 6;
fl = fg.addFactor({'LinearEquation',consts},total,a,b,c);
a.Input = [4 1];
b.Input = [10 1];
c.Input = [12 100];
assert(~isempty(strfind(fl.Solver.toString, 'CustomGaussianLinearEquation')));
assert(~isempty(strfind(fl.VectorObject.getFactorFunction.getContainedFactorFunction,'LinearEquation')));

fg.solve();


fg2 = FactorGraph();
a2 = Real();
b2 = Real();
c2 = Real();

a2m = Real();
b2m = Real();
c2m = Real();
d = Real();
d.Input = [6 1e-9];
fm1 = fg2.addFactor('Product',a2m,a2,consts(1));
fm2 = fg2.addFactor('Product',b2m,b2,consts(2));
fm3 = fg2.addFactor('Product',c2m,c2,consts(3));
a2.Input = a.Input;
b2.Input = b.Input;
c2.Input = c.Input;
fa = fg2.addFactor('Sum',d,a2m,b2m,c2m);
assert(~isempty(strfind(fm1.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fm2.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fm3.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fa.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(fm1.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fm2.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fm3.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fa.VectorObject.getFactorFunction.getContainedFactorFunction,'Sum')));

fg2.solve();

assertElementsAlmostEqual(a.Belief,a2.Belief);
assertElementsAlmostEqual(b.Belief,b2.Belief);
assertElementsAlmostEqual(c.Belief,c2.Belief);
end




% New LinearEquation custom factor with non-constant total
function test3()

fg = FactorGraph();

a = Real();
b = Real();
c = Real();
d = Real();
consts = [1 2 3];
fl = fg.addFactor({'LinearEquation',consts},d,a,b,c);
a.Input = {'Normal', 4, 1};
b.Input = {'Normal', 10, 1};
c.Input = {'Normal', 12, 1e-4};
d.Input = {'Normal', 6, 10};
assert(~isempty(strfind(fl.Solver.toString, 'CustomGaussianLinearEquation')));
assert(~isempty(strfind(fl.VectorObject.getFactorFunction.getContainedFactorFunction,'LinearEquation')));

fg.solve();


fg2 = FactorGraph();
a2 = Real();
b2 = Real();
c2 = Real();

a2m = Real();
b2m = Real();
c2m = Real();
d2 = Real();
fm1 = fg2.addFactor('Product',a2m,a2,consts(1));
fm2 = fg2.addFactor('Product',b2m,b2,consts(2));
fm3 = fg2.addFactor('Product',c2m,c2,consts(3));
a2.Input = a.Input;
b2.Input = b.Input;
c2.Input = c.Input;
d2.Input = d.Input;
fa = fg2.addFactor('Sum',d2,a2m,b2m,c2m);
assert(~isempty(strfind(fm1.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fm2.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fm3.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fa.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(fm1.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fm2.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fm3.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fa.VectorObject.getFactorFunction.getContainedFactorFunction,'Sum')));

fg2.solve();

assertElementsAlmostEqual(a.Belief,a2.Belief);
assertElementsAlmostEqual(b.Belief,b2.Belief);
assertElementsAlmostEqual(c.Belief,c2.Belief);
assertElementsAlmostEqual(d.Belief,d2.Belief);
end



% New LinearEquation custom factor with constant inputs
function test4()

fg = FactorGraph();

a = Real();
b = 4;
c = Real();
d = Real();
consts = [1 2 3];
fl = fg.addFactor({'LinearEquation',consts},d,a,b,c);
a.Input = {'Normal', 4, 1};
c.Input = {'Normal', 12, 1e-4};
d.Input = {'Normal', 6, 10};
assert(~isempty(strfind(fl.Solver.toString, 'CustomGaussianLinearEquation')));
assert(~isempty(strfind(fl.VectorObject.getFactorFunction.getContainedFactorFunction,'LinearEquation')));

fg.solve();


fg2 = FactorGraph();
a2 = Real();
b2 = 4;
c2 = Real();

a2m = Real();
b2m = b2*consts(2);
c2m = Real();
d2 = Real();
fm1 = fg2.addFactor('Product',a2m,a2,consts(1));
fm3 = fg2.addFactor('Product',c2m,c2,consts(3));
a2.Input = a.Input;
c2.Input = c.Input;
d2.Input = d.Input;
fa = fg2.addFactor('Sum',d2,a2m,b2m,c2m);
assert(~isempty(strfind(fm1.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fm3.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fa.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(fm1.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fm3.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fa.VectorObject.getFactorFunction.getContainedFactorFunction,'Sum')));

fg2.solve();

assertElementsAlmostEqual(a.Belief,a2.Belief);
assertElementsAlmostEqual(c.Belief,c2.Belief);
assertElementsAlmostEqual(d.Belief,d2.Belief);
end


% New LinearEquation custom factor with constant inputs and total
function test5()

fg = FactorGraph();

a = Real();
b = 4;
c = Real();
d = 6;
consts = [1 2 3];
fl = fg.addFactor({'LinearEquation',consts},d,a,b,c);
a.Input = {'Normal', 4, 1};
c.Input = {'Normal', 12, 1e-4};
assert(~isempty(strfind(fl.Solver.toString, 'CustomGaussianLinearEquation')));
assert(~isempty(strfind(fl.VectorObject.getFactorFunction.getContainedFactorFunction,'LinearEquation')));

fg.solve();


fg2 = FactorGraph();
a2 = Real();
b2 = 4;
c2 = Real();

a2m = Real();
b2m = b2*consts(2);
c2m = Real();
d2 = 6;
fm1 = fg2.addFactor('Product',a2m,a2,consts(1));
fm3 = fg2.addFactor('Product',c2m,c2,consts(3));
a2.Input = a.Input;
c2.Input = c.Input;
fa = fg2.addFactor('Sum',d2,a2m,b2m,c2m);
assert(~isempty(strfind(fm1.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fm3.Solver.toString, 'CustomGaussianProduct')));
assert(~isempty(strfind(fa.Solver.toString, 'CustomGaussianSum')));
assert(~isempty(strfind(fm1.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fm3.VectorObject.getFactorFunction.getContainedFactorFunction,'Product')));
assert(~isempty(strfind(fa.VectorObject.getFactorFunction.getContainedFactorFunction,'Sum')));

fg2.solve();

assertElementsAlmostEqual(a.Belief,a2.Belief);
assertElementsAlmostEqual(c.Belief,c2.Belief);
end
