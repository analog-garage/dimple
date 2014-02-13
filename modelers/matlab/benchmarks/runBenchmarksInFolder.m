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

function runBenchmarksInFolder(benchmarking, folderPath)
    here_path = path;
    mfiles = dir(fullfile(folderPath, 'benchmark*.m'));
    for k = 1:numel(mfiles)
        [~, name] = fileparts(mfiles(k).name);
        addpath(folderPath);
        try
            if nargout(name) == 1
                f = str2func(name);
                benchmarks = f();
                for i = 1 : numel(benchmarks)
                    benchmark = benchmarks(i);
                    g = @() neverAbort(benchmark.f);
                    fprintf('Running benchmark %s\n', benchmark.name);
                    benchmarking.DoRun(benchmark.name, ...
                        benchmark.warmupIterations, ...
                        benchmark.iterations, ...
                        benchmark.doGC, ...
                        g);
                end
            end
        catch exception
            path(here_path);
            rethrow(exception);
        end
        path(here_path);
    end
end

function abort = neverAbort(f)
    f();
    abort = false;
end
