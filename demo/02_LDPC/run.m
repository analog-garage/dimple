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

% Do Not Modify. For use with automated testing.
global Dimple_DEMO_RESULT Dimple_TESTING_DEMOS;
if Dimple_TESTING_DEMOS, maxSnr = 1; else maxSnr = 8; end

% Check for required toolbox.
if isempty(which('awgn'))
    disp('Communications toolbox is required.');
    Dimple_DEMO_RESULT = -1;
    return;
end

solver = com.lyricsemi.dimple.solvers.sumproduct.Solver();
setSolver(solver);
rng();

berName = 'MyRun';
description = 'MyRun';

names = {'Benchmarks/Analytic_Thresh.mat','Benchmarks/SW_LLR_MinSum.mat'};
descriptions = {'Analytic','MinSum'};

cr = LdpcRunner(20,1);

LyricBerTool.run(berName,description,cr,0:.5:maxSnr,200,1e8,20,1,names,descriptions);

% Do not modify. For use with automated testing.
Dimple_DEMO_RESULT = 0;
