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
function testGeneralFactor()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testGeneralFactor');

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);
test3(debugPrint, repeatable);
test4(debugPrint, repeatable);

dtrace(debugPrint, '--testGeneralFactor');
end

% Real variables only
function test1(debugPrint, repeatable)

    fg = FactorGraph();
    fg.Solver = 'Gaussian';
    fg.Solver.setSampledFactorSamplesPerUpdate(10000);
    fg.Solver.setSeed(1);

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
    fg.Solver.setSeed(1);

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
    fg.Solver.setSeed(1);

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
    b.Input = MultivariateMsg(bMean, bCovariance);

    if repeatable
        fg.Solver.setSeed(1);
    end
    
    fg.solve();
    
    diff = abs(c.Belief.Means-expectedMean);
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
    fg.Solver.setSeed(1);

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
    b.Input = MultivariateMsg(bMean, bCovariance);

    if repeatable
        fg.Solver.setSeed(1);
    end
    
    fg.solve();

    diff = abs(c.Belief.Means-expectedMean);
    fracDiff = diff./max(abs(expectedMean));
    assertTrue(all(fracDiff < .02));
    
    diff = abs(c.Belief.Covariance-expectedCovariance);
    fracDiff = diff./max(max(abs(expectedCovariance)));
    assertTrue(all(fracDiff(:) < .02));

end


function C = randCovariance(n)

A = rand(n,n);
B = A + A';
minEig = min(eig(B));
r = randn^2 + eps;
C = B + eye(n)*(r - minEig);

end
