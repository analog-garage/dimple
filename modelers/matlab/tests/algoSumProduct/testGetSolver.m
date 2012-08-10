function  testGetSolver()

    if strcmp(class(getSolver()),'CSolver') == 0
        fg = FactorGraph();
        assertTrue(isa(fg.Solver,'com.analog.lyric.dimple.solvers.sumproduct.SFactorGraph'));
        b = Bit(3,1);
        assertEqual(b.Solver,{[];[];[]});
        
        f = fg.addFactor(@xorDelta,b);
        assertTrue(isa(f.Solver,'com.analog.lyric.dimple.solvers.sumproduct.STableFactor'));
        
        assertTrue(isa(b(1).Solver,'com.analog.lyric.dimple.solvers.sumproduct.SVariable'));
    end
end

