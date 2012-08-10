function solvers = getSolvers()
    %disp('++algoGibbs.getSolvers')
    solvers = {};
    solvers = appendCell(solvers, com.analog.lyric.dimple.solvers.gibbs.Solver());
    %disp('--algoGibbs.getSolvers')
end
