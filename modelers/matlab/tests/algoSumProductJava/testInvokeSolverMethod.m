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
function testInvokeSolverMethod
    fg = FactorGraph();
    b = Bit(10,2);
    fs = fg.addFactorVectorized(@xorDelta,{b 1});
    fs.invokeSolverMethod('setK',uint32(5));
    for i = 1:length(fs)
       assert(fs(i).Solver.getK()==5);
    end
    fg.invokeSolverMethod('setDamping',.4);
    fg.initialize();
    for i = 1:length(b)
       assert(b(i).Solver.getDamping(0)==.4);
    end
    damping = b.invokeSolverMethodWithReturnValue('getDamping',uint32(0));
    for i = 1:length(damping)
        assert(damping{i}==.4);
    end
    
    b.invokeSolverMethod('setDamping',uint32(0),0.676);
end



