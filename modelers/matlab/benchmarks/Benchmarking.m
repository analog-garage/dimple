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

classdef Benchmarking < handle
    
    properties
        benchmarkDataset;
        dataCollectors;
    end
    
    methods
        function obj = Benchmarking(label)
            benchmarkCreator = com.analog.lyric.benchmarking.BenchmarkCreator(label);
            obj.benchmarkDataset = benchmarkCreator.getBenchmarkDataset();
            hostname = obj.benchmarkDataset.getProperties().getProperty('hostname');
            configuration = sprintf('%s/matlab', char(hostname));
            obj.benchmarkDataset.getProperties().setProperty('configuration', configuration);
            executionTimeCollector = com.analog.lyric.benchmarking.ExecutionTimeCollector();
            gcDataCollector = com.analog.lyric.benchmarking.GCDataCollector();
            memoryUsageSamplingCollector = com.analog.lyric.benchmarking.MemoryUsageSamplingCollector();
            obj.dataCollectors = [ executionTimeCollector gcDataCollector memoryUsageSamplingCollector ];
        end
        
        function DoRun(obj, label, warmupIterations, iterations, doGC, benchmarkHandle)
            benchmarkRun = com.analog.lyric.benchmarking.BenchmarkRun;
            runProperties = benchmarkRun.getProperties();
            runProperties.setProperty('label', label);
            runProperties.setProperty('warmupIterations', int2str(warmupIterations));
            runProperties.setProperty('iterations', int2str(iterations));
            runProperties.setProperty('GC', int2str(doGC));
            java.lang.Runtime.getRuntime().gc();
            for iter = 0:(warmupIterations - 1)
                abort = benchmarkHandle();
                if abort, break, end
                if doGC
                    java.lang.Runtime.getRuntime().gc();
                end
            end
            for iter = 0:(iterations - 1)
                benchmarkRunIteration = com.analog.lyric.benchmarking.BenchmarkRunIteration;
                benchmarkRunIteration.setIteration(iter);
                for i=1:length(obj.dataCollectors)
                    obj.dataCollectors(i).startCollection()
                end
                abort = benchmarkHandle();
                if abort, break, end
                for i=1:length(obj.dataCollectors)
                    obj.dataCollectors(i).finishCollection()
                end
                for i=1:length(obj.dataCollectors)
                    obj.dataCollectors(i).postResults(benchmarkRunIteration)
                end
                benchmarkRun.getIterations().add(benchmarkRunIteration);
                if doGC
                    java.lang.Runtime.getRuntime().gc();
                end
            end
            obj.benchmarkDataset.getBenchmarkRuns().add(benchmarkRun);
        end
        
        function SaveToXml(obj, filename)
            serializer = com.analog.lyric.benchmarking.BenchmarkDatasetXmlSerializer;
            serializer.serialize(java.io.FileWriter(filename), obj.benchmarkDataset);
        end
    end
end
