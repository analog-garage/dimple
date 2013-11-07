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
    dtrace(true, 'WARNING: testDistributionFactorFunctions was skipped because statistics toolbox not installed');
    return;
end

[hasLicense err] = license('checkout', 'statistics_toolbox');
if ~hasLicense
    dtrace(true, 'WARNING: testDistributionFactorFunctions was skipped because statistics toolbox license could not be obtained');
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

% Bernoulli
ps = [.001 .1 .5 .7 .99];
for i=1:length(ps);
    p = ps(i);
    f = FactorFunction('Bernoulli');
    for j=1:10
        n = randi(100);
        b = rand(1,n) < p;
        k = sum(b);
        prob = p^k * (1-p)^(n-k);
        assertElementsAlmostEqual(f.IFactorFunction.eval([p, num2cell(b)]), prob);
    end
end
    
% Beta (constant parameters)
alphas = [1 2 .1];
betas = [4.5 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    f = FactorFunction('Beta', alpha, beta);
    for v=0:.1:1
        assertElementsAlmostEqual(f.IFactorFunction.eval(v), betapdf(v,alpha,beta));
    end
end

% Beta (variable parameters)
alphas = [1 2 .1];
betas = [4.5 .1 2];
for i = 1:length(alphas)
    alpha = alphas(i);
    beta = betas(i);
    f = FactorFunction('Beta');
    for v=0:.1:1
        assertElementsAlmostEqual(f.IFactorFunction.eval({alpha, beta, v}), betapdf(v,alpha,beta));
    end
end

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
        assertElementsAlmostEqual(f.IFactorFunction.eval(num2cell(v)), prob);
    end
end

% Binomial
ps = [.001 .1 .5 .7 .99];
for i=1:length(ps);
    p = ps(i);
    f = FactorFunction('Binomial');
    for j=1:10
        n = randi(100);
        b = rand(1,n) < p;
        k = sum(b);
        assertElementsAlmostEqual(f.IFactorFunction.eval({n, p, k}), binopdf(k,n,p));
    end
end

% Categorical
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
f = FactorFunction('Categorical');
for j = 1:10
    x = randi(dim)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.eval({alpha, x}), alpha(x+1));
end

% Categorical (multiple outputs)
dim = randi(100) + 10;
alpha = rand(1,dim);
alpha = alpha/sum(alpha);
f = FactorFunction('Categorical');
for j = 1:10
    x = randi(dim, 1, 4)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.eval([alpha, num2cell(x)]), prod(alpha(x+1)));
end

% CategoricalEnergyParameters
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
alphaE = -log(alphaS);
f = FactorFunction('CategoricalEnergyParameters', dim);
for j = 1:10
    x = randi(dim)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.eval([num2cell(alphaE), x]), alphaN(x+1));
end

% CategoricalEnergyParameters (multiple outputs)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
alphaE = -log(alphaS);
f = FactorFunction('CategoricalEnergyParameters', dim);
for j = 1:10
    x = randi(dim,1,4)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.eval([num2cell(alphaE), num2cell(x)]), prod(alphaN(x+1)));
end

% CategoricalUnnormalizedParameters
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
f = FactorFunction('CategoricalUnnormalizedParameters', dim);
for j = 1:10
    x = randi(dim)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.eval([num2cell(alphaS), x]), alphaN(x+1));
end

% CategoricalUnnormalizedParameters (multiple outputs)
dim = randi(100) + 10;
scale = rand*20;
alpha = rand(1,dim);
alphaN = alpha/sum(alpha);
alphaS = alphaN * scale;
f = FactorFunction('CategoricalUnnormalizedParameters', dim);
for j = 1:10
    x = randi(dim,1,4)-1;  % Integer 0:dim-1
    assertElementsAlmostEqual(f.IFactorFunction.eval([num2cell(alphaS), num2cell(x)]), prod(alphaN(x+1)));
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
    assertElementsAlmostEqual(f.IFactorFunction.eval(x), prob);
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
    assertElementsAlmostEqual(f.IFactorFunction.eval({alpha, x}), prob);
end

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
    assertElementsAlmostEqual(f.IFactorFunction.eval(x), prob);
end

% ExchangeableDirichlet (constant parameters)
dim = randi(100) + 10;
n = rand*20;
alpha = rand(1) * n;
f = FactorFunction('ExchangeableDirichlet', dim, alpha);
for j = 1:10
    x = rand(1,dim);
    x = x/sum(x);
    prob = prod(x.^(alpha-1)) / (prod(gamma(alpha)) / gamma(sum(alpha)));
    assertElementsAlmostEqual(f.IFactorFunction.eval(x), prob);
end

% ExchangeableDirichlet (variable parameters)
dim = randi(100) + 10;
n = rand*20;
alpha = rand(1) * n;
Z = 1/(prod(gamma(alpha)) / gamma(sum(alpha)));
f = FactorFunction('ExchangeableDirichlet', dim);
for j = 1:10
    x = rand(1,dim);
    x = x/sum(x);
    prob = prod(x.^(alpha-1)) * Z ;
    assertElementsAlmostEqual(f.IFactorFunction.eval({alpha, x}), prob);
end

% ExchangeableDirichlet (multiple outputs, constant parameters)
dim = randi(10) + 10;
n = rand*20;
alpha = rand(1) * n;
Z = 1/(prod(gamma(alpha)) / gamma(sum(alpha)));
f = FactorFunction('ExchangeableDirichlet', dim, alpha);
for j = 1:10
    x = cell(1,4);
    prob = 1;
    for k=1:4
        x{k} = rand(1,dim);
        x{k} = x{k}/sum(x{k});
        prob = prob * prod(x{k}.^(alpha-1)) * Z;
    end
    assertElementsAlmostEqual(f.IFactorFunction.eval(x), prob);
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
        assertElementsAlmostEqual(f.IFactorFunction.eval(v), gampdf(v,alpha,scale));
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
        assertElementsAlmostEqual(f.IFactorFunction.eval({alpha, beta, v}), gampdf(v,alpha,scale));
    end
end

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
        assertElementsAlmostEqual(f.IFactorFunction.eval(num2cell(v)), prob);
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
        assertElementsAlmostEqual(f.IFactorFunction.eval(v), prob);
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
        assertElementsAlmostEqual(f.IFactorFunction.eval({alpha, beta, v}), prob);
    end
end

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
        assertElementsAlmostEqual(f.IFactorFunction.eval(num2cell(v)), prob);
    end
end

% LogNormal (constant parameters)
means = [0 -10 100 pi .01];
stdevs = [1 .2 27 10 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('LogNormal', mean, precision);
    for v=0:.5:10;
        assertElementsAlmostEqual(f.IFactorFunction.eval(v), lognpdf(v,mean,std));
    end
end

% LogNormal (variable parameters)
means = [0, -10, 100, pi, .01];
stdevs = [1, .2, 27, 10, 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('LogNormal');
    for v=0:.25:10;
        assertElementsAlmostEqual(f.IFactorFunction.eval({mean, precision, v}), lognpdf(v,mean,std));
    end
end

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
        assertElementsAlmostEqual(f.IFactorFunction.eval(num2cell(v)), prob);
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
    for v=-10:1:10
        assertElementsAlmostEqual(f.IFactorFunction.eval(v), gampdf(exp(-v),alpha,scale));
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
    for v=-10:1:10
        assertElementsAlmostEqual(f.IFactorFunction.eval({alpha, beta, v}), gampdf(exp(-v),alpha,scale));
    end
end

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
        assertElementsAlmostEqual(f.IFactorFunction.eval(num2cell(v)), prob);
    end
end

% Normal (constant parameters)
means = [0 -10 100 pi .01];
stdevs = [1 .2 27 10 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('Normal', mean, precision);
    for v=-10:.5:10;
        assertElementsAlmostEqual(f.IFactorFunction.eval(v), normpdf(v,mean,std));
    end
end

% Normal (variable parameters)
means = [0, -10, 100, pi, .01];
stdevs = [1, .2, 27, 10, 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('Normal');
    for v=-10:.5:10;
        assertElementsAlmostEqual(f.IFactorFunction.eval({mean, precision, v}), normpdf(v,mean,std));
    end
end

% Normal (multiple outputs, constant parameters)
means = [0 -10 100 pi .01];
stdevs = [1 .2 27 10 2];
for i=1:length(means);
    mean = means(i);
    std = stdevs(i);
    precision = 1/std^2;
    f = FactorFunction('Normal', mean, precision);
    for j=1:10;
        v = randn(1,4)*std + mean;
        prob = prod(normpdf(v,mean,std));
        assertElementsAlmostEqual(f.IFactorFunction.eval(num2cell(v)), prob);
    end
end

% Rayleigh (constant parameters)
sigmas = [1 .2 27 10 2];
for i=1:length(means);
    sigma = sigmas(i);
    f = FactorFunction('Rayleigh',sigma);
    for v=0:.5:20;
        assertElementsAlmostEqual(f.IFactorFunction.eval(v), raylpdf(v,sigma));
    end
end

% Rayleigh (variable parameters)
sigmas = [1 .2 27 10 2];
for i=1:length(means);
    sigma = sigmas(i);
    f = FactorFunction('Rayleigh');
    for v=0:.5:20;
        assertElementsAlmostEqual(f.IFactorFunction.eval({sigma, v}), raylpdf(v,sigma));
    end
end

% Rayleigh (multiple outputs, constant parameters)
sigmas = [1 .2 27 10 2];
for i=1:length(means);
    sigma = sigmas(i);
    f = FactorFunction('Rayleigh',sigma);
    for j=1:10;
        v = raylrnd(sigma,1,4);
        prob = prod(raylpdf(v,sigma));
        assertElementsAlmostEqual(f.IFactorFunction.eval(num2cell(v)), prob);
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
        assertElementsAlmostEqual(f.IFactorFunction.eval(v), prob);
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
        assertElementsAlmostEqual(f.IFactorFunction.eval({mean, precision, v}), prob);
    end
end

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
        assertElementsAlmostEqual(f.IFactorFunction.eval(num2cell(v)), prob);
    end
end


end


