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

function testFlooding()

    N = 10;

    fg = FactorGraph();
    b = Bit(N,N);
    fg.addFactorVectorized(@(a,b) rand(), b(:,1:end-1),b(:,2:end));
    fg.addFactorVectorized(@(a,b) rand(), b(1:end-1,:),b(2:end,:));
    b.Input = rand(N,N);
    fg.NumIterations = 10;
    fg.solve();
    x = b.Belief;

    modes = fg.Solver.getMultithreadingManager().getModes();

    for mode_index = 2:length(modes)
        mode = modes(mode_index);
        fg.Solver.getMultithreadingManager().setMode(mode);
        fg.Solver.useMultithreading(true);
        fg.solve();
        y = b.Belief;
        assertTrue(norm(x(:)-y(:)) == 0);
    end
end