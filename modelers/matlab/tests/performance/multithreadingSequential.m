ITERS = 1;
NUM_SOLVES = 1;
D = 75;

fg = FactorGraph();
%fg.Scheduler
%fg.Scheduler = 'SequentialScheduler';
M = 100;
N = 100;
b = Discrete(1:D,M,N);

rand('seed',1);

f1 = fg.addFactorVectorized(@(a,b) rand(), b(:,1:N-1), b(:,2:N));
f2 = fg.addFactorVectorized(@(a,b) rand(), b(1:M-1,:), b(2:M,:));

%{
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
%}

fg.NumIterations = 1;

b.Input = rand(M,N,D);

tic
fg.solve();
t1 = toc;

fg.NumIterations = ITERS;

tic
fg.Solver.solverepeated(NUM_SOLVES);
t1 = toc

x = b.Belief;

fg.Solver.setNumThreads(16);

fg.NumIterations = 1;

fg.Solver.setMultiThreadMode(0);

fg.Solver.solverepeated(0);

fg.NumIterations = ITERS;

tic
fg.Solver.solverepeated(NUM_SOLVES);
t2 = toc


y = b.Belief;

diff = x-y;
total = norm(diff(:));
fprintf('diff1: %f\n',total);

fprintf('ratio 1: %f\n',t1/t2);

%fg.VectorObject.getModelerNode(0).getSchedule()

%fg.Solver.saveDependencyGraph('mygraph.dot');