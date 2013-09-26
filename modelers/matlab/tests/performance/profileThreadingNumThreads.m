
iters = 1;
numSolves = 1;
domainSize = 70;
seed = 1;
scheduler = 'FloodingScheduler';
addlabels = false;
presolve = true;
solver = 'sumproduct';

modes = [0 2 0];
threads = [16 16 1];
time_results = cell(length(modes),1);
results = cell(length(modes),1);
titles = {};


for i = 1:length(modes)
    experimentNum = 1;

    %Sweep threads
    numThreadsArray = [2 4 6 8 16 32 64 128];
    for j = 1:length(numThreadsArray)
        M = 40;
        N = 40;
        iters = 100;
        domainSize = 30;
        numThreads = threads(i);
        threadMode = modes(i);
        if numThreads ~= 1
           tmpThreads = numThreadsArray(j); 
        else
            tmpThreads = 1;
        end
        titles{experimentNum} = sprintf('M,N=40, Domain=30, threads=%d',numThreadsArray(j));
        scheduler = 'FloodingScheduler';
        [total_time,fg1,results1] = runGraph(iters,numSolves,domainSize,...
            seed,scheduler,M,N,addlabels,tmpThreads,...
            threadMode,presolve,solver);

        time_results{i}(experimentNum) = [total_time];
        results{i}{experimentNum} = results1;
        experimentNum = experimentNum+1;

    end
    
end

%Flooding
%Sequential

%one iteration
%many iterations

%Time dependency graph creation

%Threading mode - Off, Bill, Jeff, Jeff w Shawn Dependency graph
%%

lines = {'r*-','g*-','b*-'};

hold off;
for i = 1:length(time_results)
    tmpResults = time_results{i};
    tmpResults = time_results{end} ./ tmpResults;
    
    plot(1:length(tmpResults),tmpResults,lines{i});
    hold on;
    set(gca,'xtick',1:length(tmpResults));
    set(gca,'xticklabel',titles);
end
legend('bill','jeff','single');
ylabel('X times better than single threaded');
xlabel('experiment');

%%
modeNames = {'bill','jeff','single'};
for i = 1:length(results)-1
    for j = 1:length(results{i})
        a = results{i}{j};
        b = results{end}{j};
        diff = norm(a(:)-b(:));
        if diff ~= 0
            fprintf('ack!  Screwed up on mode: %s doing: %s\n',modeNames{i},titles{j});
        end
    end
end


