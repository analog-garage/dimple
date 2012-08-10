function setSolver(solver,varargin)
    global g_dimpleSolver;
    global g_dimpleModeler;
    
    if ischar(solver)
       registry = getSolverRegistry();
       solver = registry.get(solver);
       solver = solver(varargin{:});
    end
    
    if strcmp(class(solver),'CSolver')==1
        g_dimpleModeler = ModelFactory();
    else
        g_dimpleModeler = com.analog.lyric.dimple.matlabproxy.ModelFactory();
    end
    g_dimpleModeler.setSolver(solver);
    g_dimpleSolver = solver;
end
