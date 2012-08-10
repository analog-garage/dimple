function testSetSolver()

    origSolver = getSolver();

    %test register

    %used name
    
    waserror = false;
    try
        registerSolver('particleBP',5);
    catch E
        waserror = true;
    end
    

    assertTrue(waserror);

    %unused name
    registerSolver('blah',@com.analog.lyric.dimple.solvers.sumproduct.Solver);
    setSolver('blah');
    solver = getSolver();
    assertTrue(isa(solver,'com.analog.lyric.dimple.solvers.sumproduct.Solver'));


    %test unregister
    %used name
    unregisterSolver('blah');

    %unused name
    waserror = false;
    try
        unregisterSolver('Mom');
    catch E
        waserror = true;
    end

    assertTrue(waserror);

    %test getSolverNames
    names = getSolverNames();

    %make sure more than one
    assertTrue(~isempty(names));

    for i = 1:length(names)
        name = names{i};
        
        if ~isequal('csolver',name)
            setSolver(name);
            fg = FactorGraph();
            solver = fg.Solver;
        end
        %expectedSolverType = registry.get(name);
        %assertTrue(isa(solver,class(expectedSolverType())));
    end

    fg = FactorGraph();
    fg.Solver = 'MinSum';
    assertTrue(isa(fg.Solver,'com.analog.lyric.dimple.solvers.minsum.SFactorGraph'));

    fg.Solver = 'SumProduct';
    assertTrue(isa(fg.Solver,'com.analog.lyric.dimple.solvers.sumproduct.SFactorGraph'));

    fg.setSolver('Gibbs',1,1,1,1);
    assertTrue(isa(fg.Solver,'com.analog.lyric.dimple.solvers.gibbs.SFactorGraph'));


    fg.setSolver('particlebp');
    assertTrue(isa(fg.Solver,'com.analog.lyric.dimple.solvers.particleBP.SFactorGraph'));

    fg.setSolver('gaussian');
    assertTrue(isa(fg.Solver,'com.analog.lyric.dimple.solvers.gaussian.SFactorGraph'));

    setSolver(origSolver);
end
