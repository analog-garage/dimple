%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2014 Analog Devices, Inc.
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

function run()
    benchmarking = Benchmarking('dimple');
    
    runBenchmarksInFolder(benchmarking, 'ImageDenoising')
    runBenchmarksInFolder(benchmarking, 'StereoVision')
    
    configuration = char(benchmarking.benchmarkDataset.getProperties().getProperty('configuration'));
    configuration = regexprep(configuration, '[^a-zA-Z0-9]', '_');
    createDate = char(benchmarking.benchmarkDataset.getProperties().getProperty('create.date'));
    createDate = regexprep(createDate, '[^a-zA-Z0-9]', '_');
    outputFilename = sprintf('%s_%s.xml', configuration, createDate);
    benchmarking.SaveToXml(outputFilename);
end

