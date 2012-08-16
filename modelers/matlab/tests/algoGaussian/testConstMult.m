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


function testConstMult

    fg = FactorGraph();
    a = Real();
    b = Real();
    c = 5;

    fg.Solver = 'Gaussian';
    
    fg.addFactor(@constmult,a,b,c);

    a.Input = [10 1];
    fg.solve();

    assertEqual(b.Belief,[10/5; 1/5]);
    
    a.Input = [0 Inf];
    b.Input = [10, 1];
    
    fg.solve();
    
    assertEqual(a.Belief,[10*5; 1*5]);

end
