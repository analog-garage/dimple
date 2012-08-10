function solver = getSolver()
    global g_dimpleSolver;
    if isempty(g_dimpleSolver)
        g_dimpleSolver = com.analog.lyric.dimple.solvers.sumproduct.Solver();
    end
    solver = g_dimpleSolver;
end
