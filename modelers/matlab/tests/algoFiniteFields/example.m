%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

tic

setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
fg = FactorGraph();

primPoly = 2^7 + 2^1 + 2^0;
ff1 = FiniteFieldVariable(primPoly);
ff2 = FiniteFieldVariable(primPoly);
ff3 = FiniteFieldVariable(primPoly);

%TODO: do I want this to take a number as an argument as well?
fg.addFactor(@finiteFieldMult,1,ff2,ff3);
%fg.addFactor(@finiteFieldAdd,ff1,ff2,ff3);

%priors1 = zeros(128,1);
%priors1(2) = 1;

priors2 = zeros(128,1);
priors2(3) = 1;

%ff1.Input = priors1;
ff2.Input = priors2;

fg.Solver.setNumIterations(1);
fg.solve();
ff3.Value
toc
