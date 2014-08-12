%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2014 Analog Devices, Inc.
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

function testMiscFactorFunctions()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testMiscFactorFunctions');

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);

dtrace(debugPrint, '--testMiscFactorFunctions');

end

% Test RealJoint and Complex to Real vector conversaion factor functions
function test1(debugPrint, repeatable)

for trial = 1:2
    
    fg = FactorGraph;
    fg.Solver = 'Gibbs';
    fg.Solver.setNumSamples(1000);
    fg.Solver.setBurnInScans(10);
    fg.Solver.saveAllSamples();
    fg.Solver.saveAllScores();
    if (repeatable)
        fg.Solver.setSeed(1);					% Make this repeatable
    end
    
    j31 = RealJoint(3);
    j32 = RealJoint(3);
    r31 = Real(1,3);
    r32 = Real(1,3);
    c1 = Complex;
    c2 = Complex;
    rc1 = Real(1,2);
    rc2 = Real(1,2);
    
    if trial == 1
        fg.addFactor('RealJointToRealVector',r31,j31);
        fg.addFactor('RealVectorToRealJoint',j32,r32);
        fg.addFactor('ComplexToRealVector',rc1,c1);
        fg.addFactor('RealVectorToComplex',c2,rc2);
        tolerance = 0.1;
    else
        smoothing = 1;
        fg.addFactor({'RealJointToRealVector',smoothing},r31,j31);
        fg.addFactor({'RealVectorToRealJoint',smoothing},j32,r32);
        fg.addFactor({'ComplexToRealVector',smoothing},rc1,c1);
        fg.addFactor({'RealVectorToComplex',smoothing},c2,rc2);
        tolerance = 0.25;
    end
    
    ff = cell(1,10);
    means = zeros(size(ff));
    precisions = zeros(size(ff));
    for i=1:length(ff)
        means(i) = randg*10;
        precisions(i) = rand + 0.2;
        ff{i} = FactorFunction('Normal',means(i), precisions(i));
    end
    j31.Input = {ff{1},ff{2},ff{3}};
    r32(1).Input = ff{4};
    r32(2).Input = ff{5};
    r32(3).Input = ff{6};
    c1.Input = {ff{7},ff{8}};
    rc2(1).Input = ff{9};
    rc2(2).Input = ff{10};
    
    fg.solve();
    
    j31s = j31.Solver.getAllSamples;
    j32s = j32.Solver.getAllSamples;
    r31s1 = r31(1).Solver.getAllSamples;
    r31s2 = r31(2).Solver.getAllSamples;
    r31s3 = r31(3).Solver.getAllSamples;
    r32s1 = r32(1).Solver.getAllSamples;
    r32s2 = r32(2).Solver.getAllSamples;
    r32s3 = r32(3).Solver.getAllSamples;
    c1s = c1.Solver.getAllSamples;
    c2s = c2.Solver.getAllSamples;
    rc1s1 = rc1(1).Solver.getAllSamples;
    rc1s2 = rc1(2).Solver.getAllSamples;
    rc2s1 = rc2(1).Solver.getAllSamples;
    rc2s2 = rc2(2).Solver.getAllSamples;
    
    if (trial == 1) % Without smoothing, samples should be exact
        assertElementsAlmostEqual(j31s(:,1), r31s1);
        assertElementsAlmostEqual(j31s(:,2), r31s2);
        assertElementsAlmostEqual(j31s(:,3), r31s3);
        assertElementsAlmostEqual(j32s(:,1), r32s1);
        assertElementsAlmostEqual(j32s(:,2), r32s2);
        assertElementsAlmostEqual(j32s(:,3), r32s3);
        
        assertElementsAlmostEqual(c1s(:,1), rc1s1);
        assertElementsAlmostEqual(c1s(:,2), rc1s2);
        assertElementsAlmostEqual(c2s(:,1), rc2s1);
        assertElementsAlmostEqual(c2s(:,2), rc2s2);
    end
    
    assertElementsAlmostEqual(means(1), mean(r31s1), 'absolute', tolerance);
    assertElementsAlmostEqual(means(2), mean(r31s2), 'absolute', tolerance);
    assertElementsAlmostEqual(means(3), mean(r31s3), 'absolute', tolerance);
    assertElementsAlmostEqual(means(4), mean(j32s(:,1)), 'absolute', tolerance);
    assertElementsAlmostEqual(means(5), mean(j32s(:,2)), 'absolute', tolerance);
    assertElementsAlmostEqual(means(6), mean(j32s(:,3)), 'absolute', tolerance);
    assertElementsAlmostEqual(means(7), mean(rc1s1), 'absolute', tolerance);
    assertElementsAlmostEqual(means(8), mean(rc1s2), 'absolute', tolerance);
    assertElementsAlmostEqual(means(9), mean(c2s(:,1)), 'absolute', tolerance);
    assertElementsAlmostEqual(means(10), mean(c2s(:,2)), 'absolute', tolerance);
    
end

end



