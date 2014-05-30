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

function testFactorCreationUtilities()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testFactorCreationUtilities');

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
test5(debugPrint, repeatable);
test6(debugPrint, repeatable);

dtrace(debugPrint, '--testFactorCreationUtilities');

end

% Test creation of various kinds of factors
function test1(debugPrint, repeatable)

fgAlt = FactorGraph();
fgAlt.Solver = 'Gibbs';

fg = FactorGraph();
fg.Solver = 'Gibbs';

testHelper(@Bernoulli, 1, 0, 0, true, 'Bit', fg, fgAlt);
testHelper(@Beta, 2, 0, 0, true, 'Real', fg, fgAlt);
testHelper(@Categorical, 10, 11, 0, false, 'Discrete', fg, fgAlt);
testHelper(@Categorical, 0, 0, 12, true, 'Discrete', fg, fgAlt);
testHelper(@CategoricalEnergyParameters, 10, 11, 0, false, 'Discrete', fg, fgAlt);
testHelper(@Dirichlet, 0, 0, 11, true, 'RealJoint', fg, fgAlt);
testHelper(@Gamma, 2, 0, 0, true, 'Real', fg, fgAlt);
testHelper(@InverseGamma, 2, 0, 0, true, 'Real', fg, fgAlt);
testHelper(@LogNormal, 2, 0, 0, true, 'Real', fg, fgAlt);
testHelper(@NegativeExpGamma, 2, 0, 0, true, 'Real', fg, fgAlt);
testHelper(@Normal, 2, 0, 0, true, 'Real', fg, fgAlt);
testHelper(@Rayleigh, 1, 0, 0, true, 'Real', fg, fgAlt);
testHelper(@VonMises, 2, 0, 0, true, 'Real', fg, fgAlt);

fg.initialize();    % Make sure this doesn't crash

end

% Test various forms of factor creation
function testHelper(utilityFunction, numRealParameters, numVectorParameters, numJointParameters, includeConstants, outputType, graph, altGraph)

numParameters = [numRealParameters, numVectorParameters, numJointParameters];

for testCase = 1:3
    n = numParameters(testCase);
    if (n == 0)
        continue;
    end
    
    switch (testCase)
        case 1  % Real parameters
            p = cellfun(@(x)(Real()),cell(1,n),'UniformOutput',false);
            pAlt = cellfun(@(x)(Real()),cell(1,n),'UniformOutput',false);
            pConst = num2cell(rand(1,n));
            if (n > 1)
                numMixedConstants = floor(n/2);
                numMixedVariables = n - numMixedConstants;
                vMixed = cellfun(@(x)(Real()),cell(1,numMixedVariables),'UniformOutput',false);
                cMixed = num2cell(rand(1,numMixedConstants));
                pMixed=cell(1,n);
                pMixed(1:2:2*numMixedVariables)=vMixed;
                pMixed(2:2:2*numMixedConstants)=cMixed;
            else
                pMixed = [];
            end
            
        case 2  % Real vector of parameters
            p = {Real(1,n)};
            pAlt = {Real(1,n)};
            pConst = {rand(1,n)};
            pMixed = [];
            
        case 3  % RealJoint parameters
            p = {RealJoint(n)};
            pAlt = {RealJoint(n)};
            pConst = {rand(1,n)};
            pMixed = [];
            
    end
    
    if (~includeConstants)
        pConst = [];
    end
    
    % Test with variable parameters
    x1 = utilityFunction(p{:});
    assert(all(size(x1)==[1,1]));
    assert(isa(x1,outputType));
    assert(x1.Solver.getParentGraph == graph.Solver);

    % Test creating an array of output variables with variable parameters
    x2 = utilityFunction(p{:}, [2,4]);
    assert(all(size(x2)==[2,4]));
    assert(isa(x2,outputType));

    if (~isempty(pConst))
        % Test with constant parameters
        x3 = utilityFunction(pConst{:});
        assert(all(size(x3)==[1,1]));
        assert(isa(x3,outputType));

        % Test creating an array of output variables with constant parameters
        x4 = utilityFunction(pConst{:}, [2,4]);
        assert(all(size(x4)==[2,4]));
        assert(isa(x4,outputType));
    end
    
    if (~isempty(pMixed))
        % Test mixed variable and constant parameters
        x5 = utilityFunction(pMixed{:});
        assert(all(size(x5)==[1,1]));
        assert(isa(x5,outputType));
    end
    
    % Test adding to an alternative graph (not the current graph)
    x6 = utilityFunction(pAlt{:}, altGraph);
    assert(x6.Solver.getParentGraph == altGraph.Solver);
    assert(isa(x6,outputType));

end

end


% Test constant parameter Bernoulli, Categorical, and CategoricalEnergyParameters
function test2(debugPrint, repeatable)

fg = FactorGraph();

% Single Bernoulli
b = Bernoulli(0.8);
assertElementsAlmostEqual(0.8, b.Factors{1}.VectorObject.getFactorFunction.eval(1));
assertElementsAlmostEqual(0.2, b.Factors{1}.VectorObject.getFactorFunction.eval(0));
b.Guess = 1;        % Also test using Guess, as another approach
s1 = exp(-fg.Score);
b.Guess = 0;
s0 = exp(-fg.Score);
assertElementsAlmostEqual(s1/(s1+s0), 0.8);

% Array of Bernoullis
bv = Bernoulli(0.8, [2,3,4]);
assertElementsAlmostEqual(0.8, bv(1,2,3).Factors{1}.VectorObject.getFactorFunction.eval(1));
assertElementsAlmostEqual(0.2, bv(1,2,3).Factors{1}.VectorObject.getFactorFunction.eval(0));
assertElementsAlmostEqual(0.8, bv(2,3,4).Factors{1}.VectorObject.getFactorFunction.eval(1));
assertElementsAlmostEqual(0.2, bv(2,3,4).Factors{1}.VectorObject.getFactorFunction.eval(0));

% Single Categorical
N = 7;
dist = rand(1,N);
c = Categorical(dist);
ff = c.Factors{1}.VectorObject.getFactorFunction;
fDist = arrayfun(@(i)ff.eval(i), 0:(N-1));
assertElementsAlmostEqual(dist/sum(dist), fDist/sum(fDist));

% Array of Categoricals
cv = Categorical(dist, [2,3,4]);
ff1 = cv(1,2,3).Factors{1}.VectorObject.getFactorFunction;
ff2 = cv(2,3,4).Factors{1}.VectorObject.getFactorFunction;
fDist1 = arrayfun(@(i)ff1.eval(i), 0:(N-1));
fDist2 = arrayfun(@(i)ff2.eval(i), 0:(N-1));
assertElementsAlmostEqual(dist/sum(dist), fDist1/sum(fDist1));
assertElementsAlmostEqual(dist/sum(dist), fDist2/sum(fDist2));

% Single CategoricalEnergyParameters
Ne = 11;
energyDist = randn(1,Ne);
e = CategoricalEnergyParameters(energyDist);
ffe = e.Factors{1}.VectorObject.getFactorFunction;
fDiste = arrayfun(@(i)ffe.eval(i), 0:(Ne-1));
diff = -log(fDiste) - energyDist;
assertElementsAlmostEqual(diff(1:end-1), diff(2:end));  % Assert difference is constant
assertElementsAlmostEqual(sum(e.Input),1);              % Utility makes inputs sum to 1

% Array of CategoricalEnergyParameters
ev = CategoricalEnergyParameters(energyDist, [2,3,4]);
ffe1 = ev(1,2,3).Factors{1}.VectorObject.getFactorFunction;
ffe2 = ev(2,3,4).Factors{1}.VectorObject.getFactorFunction;
fDiste1 = arrayfun(@(i)ffe1.eval(i), 0:(Ne-1));
fDiste2 = arrayfun(@(i)ffe2.eval(i), 0:(Ne-1));
diff = -log(fDiste1) - energyDist;
assertElementsAlmostEqual(diff(1:end-1), diff(2:end));  % Assert difference is constant
assertElementsAlmostEqual(sum(ev(1,2,3).Input),1);      % Utility makes inputs sum to 1
diff = -log(fDiste2) - energyDist;
assertElementsAlmostEqual(diff(1:end-1), diff(2:end));  % Assert difference is constant
assertElementsAlmostEqual(sum(ev(2,3,4).Input),1);      % Utility makes inputs sum to 1

end


% Test Binomial
function test3(debugPrint, repeatable)

fgAlt = FactorGraph();
fgAlt.Solver = 'Gibbs';

fg = FactorGraph();
fg.Solver = 'Gibbs';

maxN = 100;
constN = 37;
constP = 0.27;
p = Real;
N = Discrete(0:maxN);
pAlt = Real;
NAlt = Discrete(0:maxN);

x1 = Binomial(N, p);
x2 = Binomial(constN, p);
x3 = Binomial(N, constP);
x4 = Binomial(constN, constP);
assert(isa(x1,'Discrete'));
assert(isa(x2,'Discrete'));
assert(isa(x3,'Discrete'));
assert(isa(x4,'Discrete'));
assert(all(cell2mat(x1.Domain.Elements) == 0:maxN));
assert(all(cell2mat(x2.Domain.Elements) == 0:constN));
assert(all(cell2mat(x3.Domain.Elements) == 0:maxN));
assert(all(cell2mat(x4.Domain.Elements) == 0:constN));
assert(all(size(x1)==[1,1]));
assert(all(size(x2)==[1,1]));
assert(all(size(x3)==[1,1]));
assert(all(size(x4)==[1,1]));
assert(x1.Solver.getParentGraph == fg.Solver);
assert(x2.Solver.getParentGraph == fg.Solver);
assert(x3.Solver.getParentGraph == fg.Solver);
assert(x4.Solver.getParentGraph == fg.Solver);
assert(strcmp('Binomial',x1.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Binomial',x2.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Binomial',x3.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Binomial',x4.Factors{1}.VectorObject.getFactorFunction.getName));


x5 = Binomial(N, p, [2,4]);
x6 = Binomial(constN, p, [2,4]);
x7 = Binomial(N, constP, [2,4]);
x8 = Binomial(constN, constP, [2,4]);
assert(isa(x5,'Discrete'));
assert(isa(x6,'Discrete'));
assert(isa(x7,'Discrete'));
assert(isa(x8,'Discrete'));
assert(all(cell2mat(x5.Domain.Elements) == 0:maxN));
assert(all(cell2mat(x6.Domain.Elements) == 0:constN));
assert(all(cell2mat(x7.Domain.Elements) == 0:maxN));
assert(all(cell2mat(x8.Domain.Elements) == 0:constN));
assert(all(size(x5)==[2,4]));
assert(all(size(x6)==[2,4]));
assert(all(size(x7)==[2,4]));
assert(all(size(x8)==[2,4]));
assert(x5(1,1).Solver.getParentGraph == fg.Solver);
assert(x6(1,1).Solver.getParentGraph == fg.Solver);
assert(x7(1,1).Solver.getParentGraph == fg.Solver);
assert(x8(1,1).Solver.getParentGraph == fg.Solver);
assert(strcmp('Binomial',x5.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Binomial',x6.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Binomial',x7.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Binomial',x8.Factors{1}.VectorObject.getFactorFunction.getName));

x9 = Binomial(NAlt, pAlt, fgAlt);
assert(x9.Solver.getParentGraph == fgAlt.Solver);
assert(isa(x9,'Discrete'));

end


% Test ExchangeableDirichlet
function test4(debugPrint, repeatable)

fgAlt = FactorGraph();
fgAlt.Solver = 'Gibbs';

fg = FactorGraph();
fg.Solver = 'Gibbs';

N = 37;
constAlpha = 3.27;
alpha = Real;
alphaAlt = Real;

x1 = ExchangeableDirichlet(N, alpha);
x2 = ExchangeableDirichlet(N, constAlpha);
assert(isa(x1,'RealJoint'));
assert(isa(x2,'RealJoint'));
assert(all(size(x1)==[1,1]));
assert(all(size(x2)==[1,1]));
assert(x1.Solver.getParentGraph == fg.Solver);
assert(x2.Solver.getParentGraph == fg.Solver);
assert(strcmp('ExchangeableDirichlet',x1.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('ExchangeableDirichlet',x2.Factors{1}.VectorObject.getFactorFunction.getName));

x3 = ExchangeableDirichlet(N, alpha, [2,4]);
x4 = ExchangeableDirichlet(N, constAlpha, [2,4]);
assert(isa(x3,'RealJoint'));
assert(isa(x4,'RealJoint'));
assert(all(size(x3)==[2,4]));
assert(all(size(x4)==[2,4]));
assert(x3(1,1).Solver.getParentGraph == fg.Solver);
assert(x4(1,1).Solver.getParentGraph == fg.Solver);
assert(strcmp('ExchangeableDirichlet',x3.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('ExchangeableDirichlet',x4.Factors{1}.VectorObject.getFactorFunction.getName));

x5 = ExchangeableDirichlet(N, alphaAlt, fgAlt);
assert(x5.Solver.getParentGraph == fgAlt.Solver);
assert(isa(x5,'RealJoint'));
assert(strcmp('ExchangeableDirichlet',x5.Factors{1}.VectorObject.getFactorFunction.getName));

end



% Test MultivariateNormal (only supports constant parameters)
function test5(debugPrint, repeatable)

fgAlt = FactorGraph();
fgAlt.Solver = 'Gibbs';

fg = FactorGraph();
fg.Solver = 'Gibbs';

mean = [0 1 2 3];
covariance = eye(4) + ones(4)*0.1;

x1 = MultivariateNormal(mean, covariance);
assert(isa(x1,'RealJoint'));
assert(all(size(x1)==[1,1]));
assert(strcmp('MultivariateNormal',x1.Factors{1}.VectorObject.getFactorFunction.getName));
    
x2 = MultivariateNormal(mean, covariance, [2,4]);
assert(isa(x2,'RealJoint'));
assert(all(size(x2)==[2,4]));
assert(strcmp('MultivariateNormal',x2.Factors{1}.VectorObject.getFactorFunction.getName));

end


% Test Multinomial
function test6(debugPrint, repeatable)

fgAlt = FactorGraph();
fgAlt.Solver = 'Gibbs';

fg = FactorGraph();
fg.Solver = 'Gibbs';

dim = 7;
maxN = 100;
constN = 37;
constAlpha = rand(1,dim);
alpha = Real(1,dim);
alphaJoint = RealJoint(dim);
alphaMixed = {alpha(1), alpha(2), rand, rand, alpha(3), rand, alpha(5)};
N = Discrete(0:maxN);
alphaAlt = Real(1,dim);
alphaJointAlt = RealJoint(dim);
NAlt = Discrete(0:maxN);

x1 = Multinomial(N, constAlpha);
x2 = Multinomial(constN, constAlpha);
x3 = Multinomial(N, alphaJoint);
x4 = Multinomial(constN, alphaJoint);
x5 = Multinomial(N, alpha);
x6 = Multinomial(constN, alpha);
x7 = MultinomialEnergyParameters(N, alpha);
x8 = MultinomialEnergyParameters(constN, alpha);
x9 = Multinomial(N, alphaMixed);
xa = Multinomial(constN, alphaMixed);
xb = MultinomialEnergyParameters(N, alphaMixed);
xc = MultinomialEnergyParameters(constN, alphaMixed);
assert(isa(x1,'Discrete'));
assert(isa(x2,'Discrete'));
assert(isa(x3,'Discrete'));
assert(isa(x4,'Discrete'));
assert(isa(x5,'Discrete'));
assert(isa(x6,'Discrete'));
assert(isa(x7,'Discrete'));
assert(isa(x8,'Discrete'));
assert(isa(x9,'Discrete'));
assert(isa(xa,'Discrete'));
assert(isa(xb,'Discrete'));
assert(isa(xc,'Discrete'));
assert(all(cell2mat(x1.Domain.Elements) == 0:maxN));
assert(all(cell2mat(x2.Domain.Elements) == 0:constN));
assert(all(cell2mat(x3.Domain.Elements) == 0:maxN));
assert(all(cell2mat(x4.Domain.Elements) == 0:constN));
assert(all(cell2mat(x5.Domain.Elements) == 0:maxN));
assert(all(cell2mat(x6.Domain.Elements) == 0:constN));
assert(all(cell2mat(x7.Domain.Elements) == 0:maxN));
assert(all(cell2mat(x8.Domain.Elements) == 0:constN));
assert(all(cell2mat(x9.Domain.Elements) == 0:maxN));
assert(all(cell2mat(xa.Domain.Elements) == 0:constN));
assert(all(cell2mat(xb.Domain.Elements) == 0:maxN));
assert(all(cell2mat(xc.Domain.Elements) == 0:constN));
assert(all(size(x1)==[1,dim]));
assert(all(size(x2)==[1,dim]));
assert(all(size(x3)==[1,dim]));
assert(all(size(x4)==[1,dim]));
assert(all(size(x5)==[1,dim]));
assert(all(size(x6)==[1,dim]));
assert(all(size(x7)==[1,dim]));
assert(all(size(x8)==[1,dim]));
assert(all(size(x9)==[1,dim]));
assert(all(size(xa)==[1,dim]));
assert(all(size(xb)==[1,dim]));
assert(all(size(xc)==[1,dim]));
assert(x1(1).Solver.getParentGraph == fg.Solver);
assert(x2(1).Solver.getParentGraph == fg.Solver);
assert(x3(1).Solver.getParentGraph == fg.Solver);
assert(x4(1).Solver.getParentGraph == fg.Solver);
assert(x5(1).Solver.getParentGraph == fg.Solver);
assert(x6(1).Solver.getParentGraph == fg.Solver);
assert(x7(1).Solver.getParentGraph == fg.Solver);
assert(x8(1).Solver.getParentGraph == fg.Solver);
assert(x9(1).Solver.getParentGraph == fg.Solver);
assert(xa(1).Solver.getParentGraph == fg.Solver);
assert(xb(1).Solver.getParentGraph == fg.Solver);
assert(xc(1).Solver.getParentGraph == fg.Solver);
assert(strcmp('Multinomial',x1.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Multinomial',x2.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Multinomial',x3.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Multinomial',x4.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',x5.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',x6.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',x7.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',x8.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',x9.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',xa.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',xb.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',xc.Factors{1}.VectorObject.getFactorFunction.getName));


y1 = Multinomial(N, constAlpha, [2,4]);
y2 = Multinomial(constN, constAlpha, [2,4]);
y3 = Multinomial(N, alphaJoint, [2,4]);
y4 = Multinomial(constN, alphaJoint, [2,4]);
y5 = Multinomial(N, alpha, [2,4]);
y6 = Multinomial(constN, alpha, [2,4]);
y7 = MultinomialEnergyParameters(N, alpha, [2,4]);
y8 = MultinomialEnergyParameters(constN, alpha, [2,4]);
y9 = Multinomial(N, alphaMixed, [2,4]);
ya = Multinomial(constN, alphaMixed, [2,4]);
yb = MultinomialEnergyParameters(N, alphaMixed, [2,4]);
yc = MultinomialEnergyParameters(constN, alphaMixed, [2,4]);
assert(isa(y1,'Discrete'));
assert(isa(y2,'Discrete'));
assert(isa(y3,'Discrete'));
assert(isa(y4,'Discrete'));
assert(isa(y5,'Discrete'));
assert(isa(y6,'Discrete'));
assert(isa(y7,'Discrete'));
assert(isa(y8,'Discrete'));
assert(isa(y9,'Discrete'));
assert(isa(ya,'Discrete'));
assert(isa(yb,'Discrete'));
assert(isa(yc,'Discrete'));
assert(all(cell2mat(y1.Domain.Elements) == 0:maxN));
assert(all(cell2mat(y2.Domain.Elements) == 0:constN));
assert(all(cell2mat(y3.Domain.Elements) == 0:maxN));
assert(all(cell2mat(y4.Domain.Elements) == 0:constN));
assert(all(cell2mat(y5.Domain.Elements) == 0:maxN));
assert(all(cell2mat(y6.Domain.Elements) == 0:constN));
assert(all(cell2mat(y7.Domain.Elements) == 0:maxN));
assert(all(cell2mat(y8.Domain.Elements) == 0:constN));
assert(all(cell2mat(y9.Domain.Elements) == 0:maxN));
assert(all(cell2mat(ya.Domain.Elements) == 0:constN));
assert(all(cell2mat(yb.Domain.Elements) == 0:maxN));
assert(all(cell2mat(yc.Domain.Elements) == 0:constN));
assert(all(size(y1)==[2,4,dim]));
assert(all(size(y2)==[2,4,dim]));
assert(all(size(y3)==[2,4,dim]));
assert(all(size(y4)==[2,4,dim]));
assert(all(size(y5)==[2,4,dim]));
assert(all(size(y6)==[2,4,dim]));
assert(all(size(y7)==[2,4,dim]));
assert(all(size(y8)==[2,4,dim]));
assert(all(size(y9)==[2,4,dim]));
assert(all(size(ya)==[2,4,dim]));
assert(all(size(yb)==[2,4,dim]));
assert(all(size(yc)==[2,4,dim]));
assert(y1(1).Solver.getParentGraph == fg.Solver);
assert(y2(1).Solver.getParentGraph == fg.Solver);
assert(y3(1).Solver.getParentGraph == fg.Solver);
assert(y4(1).Solver.getParentGraph == fg.Solver);
assert(y5(1).Solver.getParentGraph == fg.Solver);
assert(y6(1).Solver.getParentGraph == fg.Solver);
assert(y7(1).Solver.getParentGraph == fg.Solver);
assert(y8(1).Solver.getParentGraph == fg.Solver);
assert(y9(1).Solver.getParentGraph == fg.Solver);
assert(ya(1).Solver.getParentGraph == fg.Solver);
assert(yb(1).Solver.getParentGraph == fg.Solver);
assert(yc(1).Solver.getParentGraph == fg.Solver);
assert(strcmp('Multinomial',y1.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Multinomial',y2.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Multinomial',y3.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Multinomial',y4.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',y5.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',y6.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',y7.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',y8.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',y9.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',ya.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',yb.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',yc.Factors{1}.VectorObject.getFactorFunction.getName));


z1 = Multinomial(NAlt, constAlpha, fgAlt);
z2 = Multinomial(constN, constAlpha, fgAlt);
z3 = Multinomial(NAlt, alphaJointAlt, fgAlt);
z4 = Multinomial(constN, alphaJointAlt, fgAlt);
z5 = Multinomial(NAlt, alphaAlt, fgAlt);
z6 = Multinomial(constN, alphaAlt, fgAlt);
z7 = MultinomialEnergyParameters(NAlt, alphaAlt, fgAlt);
z8 = MultinomialEnergyParameters(constN, alphaAlt, fgAlt);
assert(isa(z1,'Discrete'));
assert(isa(z2,'Discrete'));
assert(isa(z3,'Discrete'));
assert(isa(z4,'Discrete'));
assert(isa(z5,'Discrete'));
assert(isa(z6,'Discrete'));
assert(isa(z7,'Discrete'));
assert(isa(z8,'Discrete'));
assert(z1(1).Solver.getParentGraph == fgAlt.Solver);
assert(z2(1).Solver.getParentGraph == fgAlt.Solver);
assert(z3(1).Solver.getParentGraph == fgAlt.Solver);
assert(z4(1).Solver.getParentGraph == fgAlt.Solver);
assert(z5(1).Solver.getParentGraph == fgAlt.Solver);
assert(z6(1).Solver.getParentGraph == fgAlt.Solver);
assert(z7(1).Solver.getParentGraph == fgAlt.Solver);
assert(z8(1).Solver.getParentGraph == fgAlt.Solver);
assert(strcmp('Multinomial',z1.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Multinomial',z2.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Multinomial',z3.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('Multinomial',z4.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',z5.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialUnnormalizedParameters',z6.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',z7.Factors{1}.VectorObject.getFactorFunction.getName));
assert(strcmp('MultinomialEnergyParameters',z8.Factors{1}.VectorObject.getFactorFunction.getName));


end

