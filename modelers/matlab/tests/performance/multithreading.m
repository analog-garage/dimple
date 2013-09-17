

fg = FactorGraph();
M = 50;
N = 50;
b = Bit(M,N);

rand('seed',1);

fg.addFactorVectorized(@(a,b) rand(), b(:,1:N-1), b(:,2:N));
fg.addFactorVectorized(@(a,b) rand(), b(1:M-1,:), b(2:M,:));

ITERS = 100;

fg.NumIterations = 1;

b.Input = rand(M,N);

tic
fg.solve();
t1 = toc

fg.NumIterations = ITERS;

tic
fg.solve();
t1 = toc

x = b.Belief;

%fg.initialize();
fg.Solver.setNumThreads(5);

%fg.NumIterations = 100000;

for method = 5
    fprintf('method: %d\n',method);
    tic
    fg.Solver.solve2(method);
    t2 = toc

    y = b.Belief;

    diff = x-y;
    total = norm(diff(:));
    fprintf('diff: %f\n',total);

    fprintf('ratio: %f\n',t1/t2);
end
