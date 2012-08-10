function solvers = getSolvers()
    %disp('++algoDummy.getSolvers')
    solvers = {};
    disp('ADDING [com.analog.lyric.dimple.solvers.sumproduct.Solver] as a DUMMY example');
    solvers = appendCell(solvers, com.analog.lyric.dimple.solvers.sumproduct.Solver());
    %disp('--algoDummy.getSolvers')
end
