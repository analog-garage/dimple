
iters = 1;
numSolves = 1;
domainSize = 70;
seed = 1;
scheduler = 'FloodingScheduler';
M = 100;
N = 100;
addlabels = false;
threadMode = 0;
presolve = true;
numThreads = 1;


[total_time1,fg1,results1] = runGraph(iters,numSolves,domainSize,...
              seed,scheduler,M,N,addlabels,numThreads,...
          threadMode,presolve);

total_time1

numThreads = 16;


[total_time2,fg1,results2] = runGraph(iters,numSolves,domainSize,...
              seed,scheduler,M,N,addlabels,numThreads,...
          threadMode,presolve);

total_time2

total_time1/total_time2

norm(results1(:)-results2(:))