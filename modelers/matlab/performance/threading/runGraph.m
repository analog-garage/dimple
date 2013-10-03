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

function [total_time,fg,results] = runGraph(iters,numSolves,domainSize,...
                  seed,scheduler,M,N,addlabels,numThreads,...
              threadMode,presolve,solver)
          
    fprintf('running iters: %d solves: %d domainSize: %d ',...
        iters,numSolves,domainSize);
    fprintf('scheduler: %s M: %d N: %d threads: %d ',scheduler,M,N,numThreads);
    fprintf('threadMode: %s presolve: %d ... \n',threadMode,presolve);
    rand('seed',seed);
    randn('seed',seed);
    
    fg = FactorGraph();
    fg.Scheduler = scheduler;
    fg.Solver = solver;
    b = Discrete(1:domainSize,M,N);


    f1 = fg.addFactorVectorized(@(a,b) rand(), b(:,1:N-1), b(:,2:N));
    f2 = fg.addFactorVectorized(@(a,b) rand(), b(1:M-1,:), b(2:M,:));

    if addlabels
        
        for i = 1:M
            for j = 1:N
                b(i,j).Name = sprintf('b%d_%d',i,j);
            end
        end

        for i = 1:size(f1,1)
            for j = 1:size(f1,2)
               f1(i,j).Name = sprintf('f_horz_%d_%d',i,j); 
            end
        end

        for i = 1:size(f2,1)
            for j = 1:size(f2,2)
               f2(i,j).Name = sprintf('f_vert_%d_%d',i,j); 
            end
        end
    end
    

   
    b.Input = rand(M,N,domainSize);

    m = fg.VectorObject.getModelerNode(0);
    fg.NumIterations = iters;
    if numThreads == 1
        fg.Solver.setUseMultithreading(false);
    else
        fg.Solver.setUseMultithreading(true);
    end
    
    setDimpleNumThreads(numThreads);    
    fg.Solver.getMultithreadingManager().setNumWorkers(numThreads);
    fg.Solver.getMultithreadingManager().setMode(threadMode);

    if presolve
        fg.solve();
    end

    
    tic
    com.analog.lyric.dimple.matlabproxy.testutil.RepeatedSolve.solveRepeated(m,numSolves);
    total_time = toc;

    results = b.Belief;

    %fg.initialize();

end