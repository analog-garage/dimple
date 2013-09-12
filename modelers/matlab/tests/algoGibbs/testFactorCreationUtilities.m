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

dtrace(debugPrint, '++testComplexVariables');

if (repeatable)
    seed = 1;
    rs=RandStream('mt19937ar');
    RandStream.setGlobalStream(rs);
    reset(rs,seed);
end

test1(debugPrint, repeatable);

dtrace(debugPrint, '--testComplexVariables');

end

% Test creation of various kinds of factors
function test1(debugPrint, repeatable)

fgAlt = FactorGraph();
fgAlt.Solver = 'Gibbs';

fg = FactorGraph();
fg.Solver = 'Gibbs';

testHelper(@Bernoulli, 1, 0, 0, 'Bit', fg, fgAlt);
testHelper(@Beta, 2, 0, 0, 'Real', fg, fgAlt);
testHelper(@Categorical, 10, 11, 12, 'Discrete', fg, fgAlt);
testHelper(@CategoricalEnergyParameters, 10, 11, 0, 'Discrete', fg, fgAlt);
testHelper(@Dirichlet, 0, 0, 11, 'RealJoint', fg, fgAlt);
testHelper(@Gamma, 2, 0, 0, 'Real', fg, fgAlt);
testHelper(@InverseGamma, 2, 0, 0, 'Real', fg, fgAlt);
testHelper(@LogNormal, 2, 0, 0, 'Real', fg, fgAlt);
testHelper(@NegativeExpGamma, 2, 0, 0, 'Real', fg, fgAlt);
testHelper(@Normal, 2, 0, 0, 'Real', fg, fgAlt);
testHelper(@Rayleigh, 1, 0, 0, 'Real', fg, fgAlt);
testHelper(@VonMises, 2, 0, 0, 'Real', fg, fgAlt);

fg.initialize();    % Make sure this doesn't crash

end

% Test various forms of factor creation
function testHelper(utilityFunction, numRealParameters, numVectorParameters, numJointParameters, outputType, graph, altGraph)

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
            pConst = [];
            pMixed = [];
            
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

