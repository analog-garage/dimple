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
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
%   implied. See the License for the specific language governing
%   permissions and limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function testSampledFactors()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testSampledFactors');

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

% Statistics toolbox is needed for some tests.
hasStats = true;
if (isempty(ver('stats')))
   dtrace(true, 'WARNING: testDemos runSingleCodeword was skipped because Communications Toolbox not installed');
   hasStats = false;
else
  [hasLicense err] = license('checkout', 'statistics_toolbox');
  if ~hasLicense
      dtrace(true, 'WARNING: testSampledFactors some tests skipped because Statistics Toolbox license could not be obtained');
      hasStats = false;
  end
end


test1(debugPrint, repeatable);
if hasStats
    test2(debugPrint, repeatable);
    test3(debugPrint, repeatable);
    test4(debugPrint, repeatable);
end
test5(debugPrint, repeatable);
test6(debugPrint, repeatable);
test7(debugPrint, repeatable);

dtrace(debugPrint, '--testSampledFactors');
end

% Real variables only
function test1(debugPrint, repeatable)

    fg = FactorGraph();
    fg.Solver = 'Gaussian';
    fg.Solver.setSampledFactorSamplesPerUpdate(10000);

    a = Real();
    b = Real([0 Inf]);
    fg.addFactor('Square',a,b);

    a.Input = [401 40.02];
    b.Input = [0 Inf];
    
    if repeatable
        fg.Solver.setSeed(1);
    end

    fg.solve();

    expectedBelief = [20; 1];
    diff = abs(b.Belief-expectedBelief);
    fracDiff = diff./expectedBelief;
    assertTrue(all(fracDiff < .02));
end

% Real and discrete variables
function test2(debugPrint, repeatable)

    fg = FactorGraph();
    fg.Solver = 'Gaussian';
    fg.Solver.setSampledFactorSamplesPerUpdate(10000);
    fg.Solver.setSampledFactorBurnInScansPerUpdate(100);
    fg.Solver.setSampledFactorScansPerSample(1);
    
    aM = 5;
    aS = 2;
    bM = -2;
    bS = 3;
    bDomain = -10:10;

    a = Real();
    b = Discrete(bDomain);
    c = Real();
    fg.addFactor('Sum',c,a,b);
    
    a.Input = [aM aS];
    b.Input = normpdf(bDomain, bM, bS);

    if repeatable
        fg.Solver.setSeed(1);
    end
    
    fg.solve();

    expectedBelief = [aM+bM; sqrt(aS^2+bS^2)];
    diff = abs(c.Belief-expectedBelief);
    fracDiff = diff./expectedBelief;
    assertTrue(all(fracDiff < .04));
    
    bMean = bDomain * b.Belief;
    diff = abs(bMean - (-2));
    fracDiff = diff./2;
    assertTrue(all(fracDiff < .01));
end

% RealJoint variables only, uncorrelated
function test3(debugPrint, repeatable)

    fg = FactorGraph();
    fg.Solver = 'Gaussian';
    fg.Solver.setSampledFactorSamplesPerUpdate(10000);

    a = Complex();
    b = Complex();
    c = a * b;  % Complex product
    assert(~isempty(strfind(fg.Factors{1}.VectorObject.getFactorFunction,'ComplexProduct')));
    
    aMean = [10 10];
    aCovariance = eye(2)*.01;
    bMean = [-20 20];
    bCovariance = eye(2)*.01;
    
    as = mvnrnd(aMean,aCovariance,100000);
    bs = mvnrnd(bMean,bCovariance,100000);
    ac = complex(as(:,1),as(:,2));
    bc = complex(bs(:,1),bs(:,2));
    cc = ac .* bc;
    cs = [real(cc) imag(cc)];
    expectedMean = mean(cs)';
    expectedCovariance = cov(cs);
    if debugPrint
        figure;
        hold off;
        plot(real(cc),imag(cc),'.');
        hold on;
        plot(real(ac),imag(ac),'.r');
        plot(real(bc),imag(bc),'.g');
    end
    
    % Use two different ways to set the input, just for variation
    a.Input = FactorFunction('MultivariateNormal', aMean, aCovariance);
    b.Input = MultivariateNormalParameters(bMean, bCovariance);

    if repeatable
        fg.Solver.setSeed(1);
    end
    
    fg.solve();
    
    diff = abs(c.Belief.Mean-expectedMean);
    fracDiff = diff./max(abs(expectedMean));
    assertTrue(all(fracDiff < .01));
    
    diff = abs(c.Belief.Covariance-expectedCovariance);
    fracDiff = diff./max(max(abs(expectedCovariance)));
    assertTrue(all(fracDiff(:) < .025));

end


% RealJoint variables only, correlated
function test4(debugPrint, repeatable)

    fg = FactorGraph();
    fg.Solver = 'Gaussian';
    fg.Solver.setSampledFactorSamplesPerUpdate(10000);

    a = Complex();
    b = Complex();
    c = a * b;  % Complex product
    assert(~isempty(strfind(fg.Factors{1}.VectorObject.getFactorFunction,'ComplexProduct')));
    
    aMean = [10 10];
    aCovariance = randCovariance(2);
    bMean = [-20 20];
    bCovariance = randCovariance(2);
    
    as = mvnrnd(aMean,aCovariance,100000);
    bs = mvnrnd(bMean,bCovariance,100000);
    ac = complex(as(:,1),as(:,2));
    bc = complex(bs(:,1),bs(:,2));
    cc = ac .* bc;
    cs = [real(cc) imag(cc)];
    expectedMean = mean(cs)';
    expectedCovariance = cov(cs);
    if debugPrint
        figure;
        hold off;
        plot(real(cc),imag(cc),'.');
        hold on;
        plot(real(ac),imag(ac),'.r');
        plot(real(bc),imag(bc),'.g');
    end

    % Use two different ways to set the input, just for variation
    a.Input = FactorFunction('MultivariateNormal', aMean, aCovariance);
    b.Input = MultivariateNormalParameters(bMean, bCovariance);

    if repeatable
        fg.Solver.setSeed(1);
    end
    
    fg.solve();

    diff = abs(c.Belief.Mean-expectedMean);
    fracDiff = diff./max(abs(expectedMean));
    assertTrue(all(fracDiff < .02));
    
    diff = abs(c.Belief.Covariance-expectedCovariance);
    fracDiff = diff./max(max(abs(expectedCovariance)));
    assertTrue(all(fracDiff(:) < .02));

end


% Test that only bounded variables result in custom factor; otherwise
% should be sampled factor
function test5(debugPrint, repeatable)

    fg = FactorGraph();
    
    va = Real(1,10);
    vb = Real([0 Inf], 1, 10);
    boundedJointDomain = RealJointDomain(RealDomain(0,Inf), RealDomain);
    ja = RealJoint(2, 1, 10);
    jb = RealJoint(boundedJointDomain, 2, 1, 10);

    fvga = fg.addFactor({'Normal', 0, 1}, va);
    fvgb = fg.addFactor({'Normal', 0, 1}, vb);
    fvla = fg.addFactor({'LinearEquation', ones(1,9)}, va);
    fvlb = fg.addFactor({'LinearEquation', ones(1,9)}, vb);
    fvsa = fg.addFactor('Sum', va);
    fvsb = fg.addFactor('Sum', vb);
    fvda = fg.addFactor('Subtract', va);
    fvdb = fg.addFactor('Subtract', vb);
    fvna = fg.addFactor('Negate', va(1), va(2));
    fvnb = fg.addFactor('Negate', vb(1), va(2));
    fvpa = fg.addFactor('Product', va(1), va(2), 2);
    fvpb = fg.addFactor('Product', vb(1), va(2), 2);
    fjga = fg.addFactor({'MultivariateNormal', zeros(1,2), eye(2)}, ja);
    fjgb = fg.addFactor({'MultivariateNormal', zeros(1,2), eye(2)}, jb);
    fjsa = fg.addFactor('RealJointSum', ja);
    fjsb = fg.addFactor('RealJointSum', jb);
    fjda = fg.addFactor('RealJointSubtract', ja);
    fjdb = fg.addFactor('RealJointSubtract', jb);
    fjna = fg.addFactor('RealJointNegate', ja(1), ja(2));
    fjnb = fg.addFactor('RealJointNegate', jb(1), ja(2));
    fjpa = fg.addFactor({'MatrixRealJointVectorProduct',2,2}, ja(1), 2*eye(2), ja(2));
    fjpb = fg.addFactor({'MatrixRealJointVectorProduct',2,2}, jb(1), 2*eye(2), ja(2));

    assert(~isempty(strfind(class(fvga.Solver), 'CustomNormalConstantParameters')));
    assert(~isempty(strfind(class(fvgb.Solver), 'SampledFactor')));
    assert(~isempty(strfind(class(fvla.Solver), 'CustomGaussianLinearEquation')));
    assert(~isempty(strfind(class(fvlb.Solver), 'SampledFactor')));
    assert(~isempty(strfind(class(fvsa.Solver), 'CustomGaussianSum')));
    assert(~isempty(strfind(class(fvsb.Solver), 'SampledFactor')));
    assert(~isempty(strfind(class(fvda.Solver), 'CustomGaussianSubtract')));
    assert(~isempty(strfind(class(fvdb.Solver), 'SampledFactor')));
    assert(~isempty(strfind(class(fvna.Solver), 'CustomGaussianNegate')));
    assert(~isempty(strfind(class(fvnb.Solver), 'SampledFactor')));
    assert(~isempty(strfind(class(fvpa.Solver), 'CustomGaussianProduct')));
    assert(~isempty(strfind(class(fvpb.Solver), 'SampledFactor')));

    assert(~isempty(strfind(class(fjga.Solver), 'CustomMultivariateNormalConstantParameters')));
    assert(~isempty(strfind(class(fjgb.Solver), 'SampledFactor')));
    assert(~isempty(strfind(class(fjsa.Solver), 'CustomMultivariateGaussianSum')));
    assert(~isempty(strfind(class(fjsb.Solver), 'SampledFactor')));
    assert(~isempty(strfind(class(fjda.Solver), 'CustomMultivariateGaussianSubtract')));
    assert(~isempty(strfind(class(fjdb.Solver), 'SampledFactor')));
    assert(~isempty(strfind(class(fjna.Solver), 'CustomMultivariateGaussianNegate')));
    assert(~isempty(strfind(class(fjnb.Solver), 'SampledFactor')));
    assert(~isempty(strfind(class(fjpa.Solver), 'CustomMultivariateGaussianProduct')));
    assert(~isempty(strfind(class(fjpb.Solver), 'SampledFactor')));
end


% Test that bounded variables work with sampled factors
function test6(debugPrint, repeatable)

    fg = FactorGraph();
    fg.Solver.setSampledFactorSamplesPerUpdate(10000);
    
	a = Real;
    b = Real;
    c = Real([1.99 2.01]);
    d = Real([-3.01 -2.99]);
    
    x = a + b;
    y = c + d;
    
    a.Input = {'Normal', 7, 0.01};
    b.Input = {'Normal', 3, 0.01};
    c.Input = {'Normal', 7, 0.01};
    d.Input = {'Normal', 3, 0.01};

    if repeatable
        fg.Solver.setSeed(1);
    end

    fg.solve();
    
    assertElementsAlmostEqual(x.Belief.Mean, 10);
    assertElementsAlmostEqual(y.Belief.Mean, -1, 'absolute', 1e-4);
    assertElementsAlmostEqual(x.Belief.Precision, .005);
    assert(y.Belief.Precision > 1000);

end

% Test setting fixed-value with sampled factors
% (this is from a use-case that failed when fixed values were being
% improperly cleared)
function test7(debugPrint, repeatable)

dimension=5;
length = 2;
TransitionMatrix = Real(dimension,dimension);
T2 = FactorFunction('DiscreteTransitionUnnormalizedParameters',dimension);
x=Discrete(0:(dimension-1),length,1);
fg = FactorGraph;
for i=1:dimension^2
    TransitionMatrix(i).FixedValue=1;
end
fg.addFactorVectorized(T2,x(2:end),x(1:end-1),{TransitionMatrix,[]});
fg.solve();

end


function C = randCovariance(n)

A = rand(n,n);
B = A + A';
minEig = min(eig(B));
r = randn^2 + eps;
C = B + eye(n)*(r - minEig);

end
