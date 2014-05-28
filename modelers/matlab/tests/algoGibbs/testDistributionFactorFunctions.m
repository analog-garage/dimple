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

function testDistributionFactorFunctions()

% Skip this test if the Statistics Toolbox is unavailable.
if (isempty(ver('stats')))
    dtrace(true, 'WARNING: testDistributionFactorFunctions was skipped because Statistics Toolbox not installed');
    return;
end

[hasLicense err] = license('checkout', 'statistics_toolbox');
if ~hasLicense
    dtrace(true, 'WARNING: testDistributionFactorFunctions was skipped because Statistics Toolbox license could not be obtained');
    return;
end

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testDistributionFactorFunctions');

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);

dtrace(debugPrint, '--testDistributionFactorFunctions');

end

% Test various distribution factor functions
function test1(debugPrint, repeatable)

% Bernoulli (variable parameter)
ps = [.001 .1 .5 .7 .99];
f = FactorFunction('Bernoulli');
for i=1:length(ps);
    p = ps(i);
    for j=1:10
        n = randi(100);
        b = rand(1,n) < p;
        k = sum(b);
        prob = p^k * (1-p)^(n-k);
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([p, num2cell(b)]), -log(prob));
    end
end
assert(f.IFactorFunction.evalEnergy({0 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({1 0}) == Inf);
assert(f.IFactorFunction.evalEnergy({-eps 0}) == Inf);
assert(f.IFactorFunction.evalEnergy({1+eps 0}) == Inf);

% Bernoulli (constant parameter)
ps = [.001 .1 .5 .7 .99];
for i=1:length(ps);
    p = ps(i);
    f = FactorFunction('Bernoulli', p);
    for j=1:10
        n = randi(100);
        b = rand(1,n) < p;
        k = sum(b);
        prob = p^k * (1-p)^(n-k);
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(b)), -log(prob));
    end
end
f = FactorFunction('Bernoulli', 0);
assert(f.IFactorFunction.evalEnergy(1) == Inf);
f = FactorFunction('Bernoulli', 1);
assert(f.IFactorFunction.evalEnergy(0) == Inf);

% Beta (constant parameters)
alphas = [1 2 .1];
betas = [4.5 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    f = FactorFunction('Beta', alpha, beta);
    for v=0:.1:1
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(betapdf(v,alpha,beta)));
    end
end

% Beta (variable parameters)
alphas = [1 2 .1];
betas = [4.5 .1 2];
f = FactorFunction('Beta');
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    for v=0:.1:1
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({alpha, beta, v}), -log(betapdf(v,alpha,beta)));
    end
end
assert(f.IFactorFunction.evalEnergy({-eps 1 0}) == Inf);
assert(f.IFactorFunction.evalEnergy({0 -eps 0}) == Inf);
assert(f.IFactorFunction.evalEnergy({0 -eps -eps}) == Inf);

% Beta (multiple outputs, constant parameters)
alphas = [1 2 .1];
betas = [4.5 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    f = FactorFunction('Beta', alpha, beta);
    for j=1:10
        v = betarnd(alpha, beta, 1, 1);
        prob = prod(betapdf(v,alpha,beta));
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(v)), -log(prob));
    end
end

% Binomial
ps = [.001 .1 .5 .7 .99];
f = FactorFunction('Binomial');
for i=1:length(ps);
    p = ps(i);
    for j=1:10
        n = randi(100);
        b = rand(1,n) < p;
        k = sum(b);
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({n, p, k}), -log(binopdf(k,n,p)));
    end
end
assert(f.IFactorFunction.evalEnergy({4 0 4}) == Inf);
assert(f.IFactorFunction.evalEnergy({4 1 0}) == Inf);
assert(f.IFactorFunction.evalEnergy({4 0.5 5}) == Inf);
assert(f.IFactorFunction.evalEnergy({4 -eps 0}) == Inf);
assert(f.IFactorFunction.evalEnergy({4 1+eps 0}) == Inf);

% Categorical (variable parameters)
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
f = FactorFunction('Categorical');
for j = 1:10
    x = randi(dim)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({alpha, x}), -log(alpha(x+1)));
end

% Categorical (multiple outputs, variable parameters)
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
f = FactorFunction('Categorical');
for j = 1:10
    x = randi(dim, 1, 4)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([alpha, num2cell(x)]), -log(prod(alpha(x+1))));
end

% Categorical (constant parameters)
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
f = FactorFunction('Categorical', alpha);
for j = 1:10
    x = randi(dim)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(x), -log(alpha(x+1)));
end

% Categorical (multiple outputs, constant parameters)
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
f = FactorFunction('Categorical', alpha);
for j = 1:10
    x = randi(dim, 1, 4)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(x)), -log(prod(alpha(x+1))));
end

% CategoricalEnergyParameters (variable parameters)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
alphaE = -log(alphaS);
f = FactorFunction('CategoricalEnergyParameters', dim);
for j = 1:10
    x = randi(dim)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([num2cell(alphaE), x]), -log(alphaN(x+1)));
end

% CategoricalEnergyParameters (multiple outputs, variable parameters)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
alphaE = -log(alphaS);
f = FactorFunction('CategoricalEnergyParameters', dim);
for j = 1:10
    x = randi(dim,1,4)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([num2cell(alphaE), num2cell(x)]), -log(prod(alphaN(x+1))));
end

% CategoricalEnergyParameters (constant parameters)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
alphaE = -log(alphaS);
f = FactorFunction('CategoricalEnergyParameters', dim, alphaE);
for j = 1:10
    x = randi(dim)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(x), -log(alphaN(x+1)));
end

% CategoricalEnergyParameters (multiple outputs, constant parameters)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
alphaE = -log(alphaS);
f = FactorFunction('CategoricalEnergyParameters', dim, alphaE);
for j = 1:10
    x = randi(dim,1,4)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(x)), -log(prod(alphaN(x+1))));
end

% CategoricalUnnormalizedParameters (variable parameters)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
f = FactorFunction('CategoricalUnnormalizedParameters', dim);
for j = 1:10
    x = randi(dim)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([num2cell(alphaS), x]), -log(alphaN(x+1)));
end
alphaS(1) = -eps;
assert(f.IFactorFunction.evalEnergy([num2cell(alphaS), x]) == Inf);

% CategoricalUnnormalizedParameters (multiple outputs, variable parameters)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
f = FactorFunction('CategoricalUnnormalizedParameters', dim);
for j = 1:10
    x = randi(dim,1,4)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([num2cell(alphaS), num2cell(x)]), -log(prod(alphaN(x+1))));
end

% CategoricalUnnormalizedParameters (constant parameters)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
f = FactorFunction('CategoricalUnnormalizedParameters', dim, alphaS);
for j = 1:10
    x = randi(dim)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(x), -log(alphaN(x+1)));
end

% CategoricalUnnormalizedParameters (multiple outputs, constant parameters)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
f = FactorFunction('CategoricalUnnormalizedParameters', dim ,alphaS);
for j = 1:10
    x = randi(dim,1,4)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(x)), -log(prod(alphaN(x+1))));
end

% Dirichlet (constant parameters)
dim = randi(100) + 10;
n = rand*20;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
alpha = alpha * n;
f = FactorFunction('Dirichlet',alpha);
for j = 1:10
    x = rand(1,dim);
    x = x/sum(x);
    prob = prod(x.^(alpha-1)) / (prod(gamma(alpha)) / gamma(sum(alpha)));
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(x), -log(prob));
end

% Dirichlet (variable parameters)
dim = randi(100) + 10;
n = rand*20;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
alpha = alpha * n;
Z = 1/(prod(gamma(alpha)) / gamma(sum(alpha)));
f = FactorFunction('Dirichlet');
for j = 1:10
    x = rand(1,dim);
    x = x/sum(x);
    prob = prod(x.^(alpha-1)) * Z ;
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({alpha, x}), -log(prob));
end
alphaTest = alpha;
alphaTest(1) = 0;
assert(f.IFactorFunction.evalEnergy({alphaTest, x}) == Inf);
alphaTest(1) = -eps;
assert(f.IFactorFunction.evalEnergy({alphaTest, x}) == Inf);
x(1) = x(1) + 0.001;
assert(f.IFactorFunction.evalEnergy({alpha, x}) == Inf);

% Dirichlet (multiple outputs, constant parameters)
dim = randi(10) + 10;
n = rand*20;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
alpha = alpha * n;
Z = 1/(prod(gamma(alpha)) / gamma(sum(alpha)));
f = FactorFunction('Dirichlet',alpha);
for j = 1:10
    x = cell(1,4);
    prob = 1;
    for k=1:4
        x{k} = rand(1,dim);
        x{k} = x{k}/sum(x{k});
        prob = prob * prod(x{k}.^(alpha-1)) * Z;
    end
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(x), -log(prob));
end

% ExchangeableDirichlet (constant parameters)
dim = randi(100) + 10;
n = rand*20;
alpha = rand(1) * n;
f = FactorFunction('ExchangeableDirichlet', dim, alpha);
for j = 1:10
    x = rand(1,dim);
    x = x/sum(x);
    logp = sum(log(x)*(alpha-1)) - dim*gammaln(alpha) + gammaln(alpha*dim);
    prob = exp(logp);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(x), -log(prob));
end

% ExchangeableDirichlet (variable parameters)
dim = randi(100) + 10;
n = rand*20;
alpha = rand(1) * n;
logZ = dim*gammaln(alpha) - gammaln(alpha*dim);
f = FactorFunction('ExchangeableDirichlet', dim);
for j = 1:10
    x = rand(1,dim);
    x = x/sum(x);
    logp = sum(log(x)*(alpha-1)) - logZ;
    prob = exp(logp);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({alpha, x}), -log(prob));
end
assert(f.IFactorFunction.evalEnergy({0, x}) == Inf);
assert(f.IFactorFunction.evalEnergy({-eps, x}) == Inf);
x(1) = x(1) + 0.001;
assert(f.IFactorFunction.evalEnergy({alpha, x}) == Inf);

% ExchangeableDirichlet (multiple outputs, constant parameters)
dim = randi(10) + 10;
n = rand*20;
alpha = rand(1) * n;
logZ = dim*gammaln(alpha) - gammaln(alpha*dim);
f = FactorFunction('ExchangeableDirichlet', dim, alpha);
for j = 1:10
    x = cell(1,4);
    logp = 0;
    for k=1:4
        x{k} = rand(1,dim);
        x{k} = x{k}/sum(x{k});
        logp = logp + sum(log(x{k})*(alpha-1)) - logZ;
    end
    prob = exp(logp);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(x), -log(prob));
end

% Gamma (constant parameters)
alphas = [1 2 .1];
betas = [1 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    scale = 1/beta;
    f = FactorFunction('Gamma', alpha, beta);
    for v=0:.1:1
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(gampdf(v,alpha,scale)));
    end
end

% Gamma (variable parameters)
alphas = [1 2 .1];
betas = [1 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    scale = 1/beta;
    f = FactorFunction('Gamma');
    for v=0:.1:1
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({alpha, beta, v}), -log(gampdf(v,alpha,scale)));
    end
end
assert(f.IFactorFunction.evalEnergy({0, 1, 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({1, 0, 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({-eps, 1, 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({1, -eps, 1}) == Inf);

% Gamma (multiple outputs, constant parameters)
alphas = [1 2 .1];
betas = [1 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    scale = 1/beta;
    f = FactorFunction('Gamma', alpha, beta);
    for j=1:10
        v = gamrnd(alpha,scale,1,4);
        prob = prod(gampdf(v,alpha,scale));
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(v)), -log(prob));
    end
end

% InverseGamma (constant parameters)
alphas = [1 2 .1];
betas = [1 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    Z = beta^alpha / gamma(alpha);
    f = FactorFunction('InverseGamma', alpha, beta);
    for v=.1:.1:1
        prob = v^(-alpha-1) * exp(-beta/v) * Z;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(prob));
    end
end

% InverseGamma (variable parameters)
alphas = [1 2 .1];
betas = [1 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    Z = beta^alpha / gamma(alpha);
    f = FactorFunction('InverseGamma');
    for v=0:.1:1
        prob = v^(-alpha-1) * exp(-beta/v) * Z;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({alpha, beta, v}), -log(prob));
    end
end
assert(f.IFactorFunction.evalEnergy({0, 1, 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({1, 0, 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({-eps, 1, 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({1, -eps, 1}) == Inf);

% InverseGamma (multiple outputs, constant parameters)
alphas = [1 2 .1];
betas = [1 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    Z = beta^alpha / gamma(alpha);
    f = FactorFunction('InverseGamma', alpha, beta);
    for j=1:10
        v = rand(1,4);
        prob = prod(v.^(-alpha-1) .* exp(-beta./v) * Z);
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(v)), -log(prob));
    end
end

% LogNormal (constant parameters)
means = [0 -2 4 pi .01];
stdevs = [1 .2 27 10 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('LogNormal', mean, precision);
    for v=0:.5:10;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(lognpdf(v,mean,std)));
    end
end

% LogNormal (variable parameters)
means = [0 -2 4 pi .01];
stdevs = [1, .2, 27, 10, 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('LogNormal');
    for v=0:.25:10;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({mean, precision, v}), -log(lognpdf(v,mean,std)));
    end
end
assert(f.IFactorFunction.evalEnergy({0, -eps, 0}) == Inf);

% LogNormal (multiple outputs, constant parameters)
means = [0 -10 100 pi .01];
stdevs = [1 .2 27 10 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('LogNormal', mean, precision);
    for j=1:10
        v = lognrnd(mean,std,1,4);
        prob = prod(lognpdf(v,mean,std));
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(v)), -log(prob));
    end
end

% Multinomial (variable N)
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
f = FactorFunction('Multinomial');
for j = 1:10
    N = randi(1000);
    x = mnrnd(N,alpha);
    xc = num2cell(x);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([N, alpha, xc]), -log(mnpdf(x,alpha)));
end

% Multinomial (constant N)
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
for j = 1:10
    N = randi(1000);
    f = FactorFunction('Multinomial', N);
    x = mnrnd(N,alpha);
    xc = num2cell(x);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([alpha, xc]), -log(mnpdf(x,alpha)));
end

% MultinomialEnergyParameters (variable N)
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
alphaE = -log(alpha);
f = FactorFunction('MultinomialEnergyParameters', dim);
for j = 1:10
    N = randi(1000);
    x = mnrnd(N,alpha);
    xc = num2cell(x);
    alphaEc = num2cell(alphaE);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([N, alphaEc, xc]), -log(mnpdf(x,alpha)));
end

% MultinomialEnergyParameters (constant N)
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
alphaE = -log(alpha);
for j = 1:10
    N = randi(1000);
    f = FactorFunction('MultinomialEnergyParameters', dim, N);
    x = mnrnd(N,alpha);
    xc = num2cell(x);
    alphaEc = num2cell(alphaE);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([alphaEc, xc]), -log(mnpdf(x,alpha)));
end

% MultinomialUnnormalizedParameters (variable N)
dim = randi(100) + 10;
alphaU = rand(1,dim);
alpha = alphaU/sum(alphaU);
f = FactorFunction('MultinomialUnnormalizedParameters', dim);
for j = 1:10
    N = randi(1000);
    x = mnrnd(N,alpha);
    xc = num2cell(x);
    alphaUc = num2cell(alphaU);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([N, alphaUc, xc]), -log(mnpdf(x,alpha)));
end

% MultinomialUnnormalizedParameters (constant N)
dim = randi(100) + 10;
alphaU = rand(1,dim);
alpha = alphaU/sum(alphaU);
for j = 1:10
    N = randi(1000);
    f = FactorFunction('MultinomialUnnormalizedParameters', dim, N);
    x = mnrnd(N,alpha);
    xc = num2cell(x);
    alphaUc = num2cell(alphaU);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy([alphaUc, xc]), -log(mnpdf(x,alpha)));
end

% MultivariateNormal (constant parameters)
dims = num2cell(randi(10,1,10) + 10);
means = cellfun(@(d)randn(d,1)*10, dims,'UniformOutput',false);
covariances = cellfun(@(d)randCovariance(d), dims,'UniformOutput',false);
for i=1:length(dims);
    dim = dims{i};
    mean = means{i};
    covariance = covariances{i};
    f = FactorFunction('MultivariateNormal', mean, covariance);
    e = max(eig(covariance));
    for t=1:100
        v = randn(dim,1)*sqrt(e)/50 + mean;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(mvnpdf(v,mean,covariance)));
    end
end

% MultivariateNormal (multiple outputs, constant parameters)
dims = num2cell(randi(10,1,10) + 10);
means = cellfun(@(d)randn(d,1)*10, dims,'UniformOutput',false);
covariances = cellfun(@(d)randCovariance(d), dims,'UniformOutput',false);
for i=1:length(dims);
    dim = dims{i};
    mean = means{i};
    covariance = covariances{i};
    f = FactorFunction('MultivariateNormal', mean, covariance);
    e = min(eig(covariance));
    for t=1:100
        v = randn(dim,4)*sqrt(e)/50 + repmat(mean,1,4);
        v = num2cell(v,1);
        pv = cellfun(@(v)mvnpdf(v,mean,covariance), v,'UniformOutput',false);
        prob = prod(cell2mat(pv));
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(prob));
    end
end

% NegativeExpGamma (constant parameters)
alphas = [1 2 .1];
betas = [1 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    scale = 1/beta;
    f = FactorFunction('NegativeExpGamma', alpha, beta);
    for v=-2:1:2
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(gampdf(exp(-v),alpha,scale)));
    end
end

% NegativeExpGamma (variable parameters)
alphas = [1 2 .1];
betas = [1 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    scale = 1/beta;
    f = FactorFunction('NegativeExpGamma');
    for v=-2:1:2
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({alpha, beta, v}), -log(gampdf(exp(-v),alpha,scale)));
    end
end
assert(f.IFactorFunction.evalEnergy({0, 1, 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({1, 0, 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({-eps, 1, 1}) == Inf);
assert(f.IFactorFunction.evalEnergy({1, -eps, 1}) == Inf);

% NegativeExpGamma (multiple outputs, constant parameters)
alphas = [1 2 .1];
betas = [1 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    scale = 1/beta;
    f = FactorFunction('NegativeExpGamma', alpha, beta);
    for h=1:10
        v = -log(gamrnd(alpha,scale,1,4));
        prob = prod(gampdf(exp(-v),alpha,scale));
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(v)), -log(prob));
    end
end

% Normal (constant parameters)
means = [0 -2 4 pi .01];
stdevs = [1 .2 27 10 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('Normal', mean, precision);
    for v=-5:.5:5;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(normpdf(v,mean,std)));
    end
end

% Normal (variable parameters)
means = [0 -2 4 pi .01];
stdevs = [1, .2, 27, 10, 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('Normal');
    for v=-5:.5:5;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({mean, precision, v}), -log(normpdf(v,mean,std)));
    end
end
assert(f.IFactorFunction.evalEnergy({0, -eps, 0}) == Inf);

% Normal (multiple outputs, constant parameters)
means = [0 -2 4 pi .01];
stdevs = [1 .2 27 10 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('Normal', mean, precision);
    for j=1:10;
        v = randn(1,4)*std + mean;
        prob = prod(normpdf(v,mean,std));
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(v)), -log(prob));
    end
end

% Poisson
f = FactorFunction('Poisson');
for i=1:10;
    n=randi(15);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({i,n}),-log(poisspdf(i,n)));
end
for i=1:10
    n=randi(15);
    assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({n,i}),-log(poisspdf(n,i)));
end
assert(f.IFactorFunction.evalEnergy({randi(100),0}) == Inf);
assert(f.IFactorFunction.evalEnergy({randi(100),0}) == Inf);
assert(f.IFactorFunction.evalEnergy({0,0}) == 0);

% Rayleigh (constant parameters)
sigmas = [1 .2 27 10 2];
for i=1:length(means);
    sigma = sigmas(i);
    f = FactorFunction('Rayleigh',sigma);
    for v=0:.5:5;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(raylpdf(v,sigma)));
    end
end

% Rayleigh (variable parameters)
sigmas = [1 .2 27 10 2];
for i=1:length(means);
    sigma = sigmas(i);
    f = FactorFunction('Rayleigh');
    for v=0:.5:5;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({sigma, v}), -log(raylpdf(v,sigma)));
    end
end
assert(f.IFactorFunction.evalEnergy({-eps, 0}) == Inf);

% Rayleigh (multiple outputs, constant parameters)
sigmas = [1 .2 27 10 2];
for i=1:length(means);
    sigma = sigmas(i);
    f = FactorFunction('Rayleigh',sigma);
    for j=1:10;
        v = raylrnd(sigma,1,4);
        prob = prod(raylpdf(v,sigma));
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(v)), -log(prob));
    end
end

% VonMises (constant parameters)
means = [0 pi 2 pi/3 ];
stdevs = [1 .2 3 .1];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    Z = 1/(2*pi*besseli(0,precision));
    f = FactorFunction('VonMises', mean, precision);
    for v=-pi:pi/8:pi;
        prob = exp(precision*cos(v-mean)) * Z;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(v), -log(prob));
    end
end

% VonMises (variable parameters)
means = [0 pi 2 pi/3 ];
stdevs = [1 .2 3 .1];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    Z = 1/(2*pi*besseli(0,precision));
    f = FactorFunction('VonMises');
    for v=-pi:pi/8:pi;
        prob = exp(precision*cos(v-mean)) * Z;
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy({mean, precision, v}), -log(prob));
    end
end
assert(f.IFactorFunction.evalEnergy({0, -eps, 0}) == Inf);

% VonMises (multiple outputs, constant parameters)
means = [0 pi 2 pi/3 ];
stdevs = [1 .2 3 .1];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    Z = 1/(2*pi*besseli(0,precision));
    f = FactorFunction('VonMises', mean, precision);
    for j=1:10
        v = rand(1,4)*2*pi - pi;
        prob = prod(exp(precision*cos(v-mean)) * Z);
        assertElementsAlmostEqual(f.IFactorFunction.evalEnergy(num2cell(v)), -log(prob));
    end
end


end


% Generate a random covariance matrix for testing MultivariateNormal
function C = randCovariance(n)
A = rand(n,n);
B = A + A';
minEig = min(eig(B));
r = randn^2 + eps;
C = B + eye(n)*(r - minEig);
end



