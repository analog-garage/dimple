function solvers = getSolvers()
    %disp('++algoMinSum.getSolvers')
    solvers = {};
    solvers = appendCell(solvers, 'minsum');
    %solvers = appendCell(solvers, com.analog.lyric.dimple.solvers.minsumfixedpoint.Solver());
    %disp('--algoMinSum.getSolvers')
end
