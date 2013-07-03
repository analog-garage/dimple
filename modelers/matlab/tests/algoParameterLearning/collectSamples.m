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

%%%
% This function uses the gibbs sampler to generate samples for a given 
% graph
function samples = collectSamples(fg,burnIns,numSamples,scansPerSamples)

    BURN_INS = burnIns;
    NUM_SAMPLES = numSamples;
    SCANS_PER_SAMPLE = scansPerSamples;

    fg.Solver = 'gibbs';
    fg.Solver.setBurnInScans(BURN_INS);
    fg.Solver.setNumSamples(NUM_SAMPLES);
    fg.Solver.setScansPerSample(SCANS_PER_SAMPLE);
    fg.Solver.saveAllSamples();
    fg.Solver.setSeed(1);
    fg.Solver.saveAllScores();
    fg.solve();

    numvars = length(fg.Variables);
    samples = zeros(NUM_SAMPLES,numvars);

    for i = 1:numvars
        samples(:,i) = cell2mat(cell(fg.Variables{i}.Solver.getAllSamples()));
    end
    
end