%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2011, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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
