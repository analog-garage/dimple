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

function testNested()

    a = Bit();
    b = Bit();
    ng = FactorGraph(a,b);
    ng.addFactor(@(a,b) rand(),a,b);

    fg = FactorGraph();


    b = Bit(3,1);
    fg.addFactor(ng,b(1),b(2));
    fg.addFactor(ng,b(2),b(3));
    fg.addFactor(@(a,b) rand(), b(1),b(2));

    rand('seed',1);
    b.Input = rand(3,1);

    fg.solve();
    x = b.Belief;

   
    
    modes = fg.Solver.getMultithreadingManager().getModes();
    for mode_index = 2:length(modes)
        mode = modes(mode_index);
        fg.Solver.useMultithreading(true);
        fg.Solver.getMultithreadingManager().setMode(mode);
        fg.solve();
        y = b.Belief;
        assertTrue(norm(x(:)-y(:))==0);
    end
end