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

function testFactorFunctions()

% Skip this test if the Communications Toolbox is unavailable.
if isempty(which('gf')), return; end

m = 3;

numElements = 2^m;
domain = 0:numElements-1;

constant = 3;

gfx = gf(domain,m);
prim_poly = gfx.prim_poly;

fg1 = FactorGraph;
fg2 = FactorGraph;
fg3 = FactorGraph;
fg4 = FactorGraph;
fg5 = FactorGraph;
fg6 = FactorGraph;
fg7 = FactorGraph;
fg8 = FactorGraph;
fg9 = FactorGraph;
fg10 = FactorGraph;
v = FiniteFieldVariable(prim_poly, 1, 22);
b = Bit(1,2*m);

% TODO overloaded operators

faf = fg1.addFactor('FiniteFieldAdd', v(1:3));
fao = fg2.addFactor(@finiteFieldAdd,  v(4:6));
fmf = fg3.addFactor('FiniteFieldMult', v(7:9));
fmo = fg4.addFactor(@finiteFieldMult,  v(10:12));
fcf = fg5.addFactor('FiniteFieldMult', v(13), constant, v(14));
fco = fg6.addFactor(@finiteFieldMult,  v(15), constant, v(16));
% FIXME
% fpf = fg7.addFactor('FiniteFieldProjection', v(17), [1 3], b(1:2));
% fpo = fg8.addFactor(@finiteFieldProjection,  v(18), [1 3], b(3:4));
fpf = fg7.addFactor('FiniteFieldProjection', v(17), m-1:-1:0, b(1:m));
fpo = fg8.addFactor(@finiteFieldProjection,  v(18), m-1:-1:0, b(m+1:2*m));
facf = fg9.addFactor('FiniteFieldAdd', v(19), constant, v(20));
faco = fg10.addFactor(@finiteFieldAdd, v(21), constant, v(22));

assert(~isempty(strfind(faf.Solver.toString, 'CustomFiniteFieldAdd')));
assert(~isempty(strfind(fao.Solver.toString, 'CustomFiniteFieldAdd')));
assert(~isempty(strfind(fmf.Solver.toString, 'CustomFiniteFieldMult')));
assert(~isempty(strfind(fmo.Solver.toString, 'CustomFiniteFieldMult')));
assert(~isempty(strfind(fcf.Solver.toString, 'CustomFiniteFieldConstantMult')));
assert(~isempty(strfind(fco.Solver.toString, 'CustomFiniteFieldConstantMult')));
assert(~isempty(strfind(fpf.Solver.toString, 'CustomFiniteFieldProjection')));
assert(~isempty(strfind(fpo.Solver.toString, 'CustomFiniteFieldProjection')));
assert(~isempty(strfind(facf.Solver.toString, 'STableFactor')));    % No custom factor for this case
assert(~isempty(strfind(faco.Solver.toString, 'STableFactor')));    % No custom factor for this case
assert(~isempty(strfind(faf.VectorObject.getFactorFunction.getContainedFactorFunction,'FiniteFieldAdd')));
assert(~isempty(strfind(fmf.VectorObject.getFactorFunction.getContainedFactorFunction,'FiniteFieldMult')));
assert(~isempty(strfind(fcf.VectorObject.getFactorFunction.getContainedFactorFunction,'FiniteFieldMult')));
assert(~isempty(strfind(fpf.VectorObject.getFactorFunction.getContainedFactorFunction,'FiniteFieldProjection')));
assert(~isempty(strfind(facf.VectorObject.getFactorFunction.getContainedFactorFunction,'FiniteFieldAdd')));

% Evaluate factor function
nullScore3 = [];
nullScore2 = [];
for i=1:numElements
    gi = gfx(i);
    
    expectedConstMult = gi * constant;
    expectedProjection = dec2bin(gi.x, m)-'0'; % Convert to bit array
    expectedConstAdd = gi + constant;

    for j=1:numElements
        gj = gfx(j);
        
        expectedAdd = gi + gj;
        expectedMult = gi * gj;
        
        for k=1:numElements
            gk = gfx(k);
            
            v(1:3).Guess = [gk, gi, gj];
            v(7:9).Guess = {gk, gi, gj};    % Test cell form of Guess
            score1 = fg1.Score();
            score3 = fg3.Score();
            
            if (isempty(nullScore3))
                nullScore3 = score1;
            end
            
            if (gk == expectedAdd)
                assertElementsAlmostEqual(score1, nullScore3);
            else
                assert(isinf(score1));
            end
            
            if (gk == expectedMult)
                assertElementsAlmostEqual(score3, nullScore3);
            else
                assert(isinf(score3));
            end

        end
    
        v(13:14).Guess = [gj, gi];
        v(17).Guess = gi;
        gjProj = dec2bin(gj.x, m)-'0'; % Convert to bit array
        b(1:m).Guess = gjProj;
        v(19:20).Guess = [gj, gi];
        score5 = fg5.Score();
        score7 = fg7.Score();
        score9 = fg9.Score();
        
        assert(all(v(13:14).Guess == [gj, gi]));
        assert(v(17).Guess == gi);
        
        if (isempty(nullScore2))
            nullScore2 = score5;
        end

        
        if (gj == expectedConstMult)
            assertElementsAlmostEqual(score5, nullScore2);
        else
            assert(isinf(score5));
        end
        
        if (all(gjProj == expectedProjection))
            assertElementsAlmostEqual(score7, nullScore2);
        else
            assert(isinf(score7));
        end
        
        if (gj == expectedConstAdd)
            assertElementsAlmostEqual(score9, nullScore2);
        else
            assert(isinf(score9));
        end


    end

end

end
