
iters = 1;
numSolves = 1;
domainSize = 70;
seed = 1;
scheduler = 'FloodingScheduler';
M = 50;
N = 50;
addlabels = false;
threadMode = 0;
presolve = true;

modes = [0 1 0];
threads = [16 16 1];
results = cell(length(modes),1);

for i = 1:length(modes)
    
    M = 200;
    N = 200;
    iters = 2;
    domainSize = 2;
    numThreads = threads(i);
    threadMode = modes(i);
    
    [total_time,fg1,results1] = runGraph(iters,numSolves,domainSize,...
                  seed,scheduler,M,N,addlabels,numThreads,...
              threadMode,presolve);
          
          results{i} = [total_time];
          
    M = 4;
    N = 4;
    iters = 10;
    domainSize = 75;
    numThreads = threads(i);
    threadMode = modes(i);
    
    [total_time,fg1,results1] = runGraph(iters,numSolves,domainSize,...
                  seed,scheduler,M,N,addlabels,numThreads,...
              threadMode,presolve);
          
          results{i}(2) = [total_time];
          

end

%{
[t1,fg1,results1] = runGraph(iters,numSolves,domainSize,...
                  seed,scheduler,M,N,addlabels,numThreads,...
              threadMode,presolve);

[t2,fg2,results2] = runGraph(iters,numSolves,domainSize,...
                  seed,scheduler,M,N,addlabels,1,...
              threadMode,presolve);
%}
          
          
%big graph small factors
%small graph big factors
%medium graph medium factors
%medium graph big factors

%Flooding
%Sequential

%one iteration
%many iterations

%Time dependency graph creation

%Threading mode - Off, Bill, Jeff, Jeff w Shawn Dependency graph

%{
hold off;
plot(1:3,[2 2.2 3],'r*-');
hold on;
plot(1:3,[2 1.5 2],'g*-');
set(gca,'xtick',1:3);
set(gca,'xticklabel',{'bob','fred','boo'});
%}
