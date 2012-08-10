function testReal()

    solver = getSolver();
    
    setSolver('Gaussian');

    y = Real();
    x = Real();
    ng = FactorGraph(x,y);

    ng.addFactor(@constmult,y,x,1.1);


    %TODO: allow same args as Real variable
    vars = RealStream();

    fg = FactorGraph();

    fg.addFactor(ng,vars,vars.getSlice(2));

    data = ones(10,2);

    vars.DataSource = com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource(data);


    fg.initialize();

    fg2 = FactorGraph();
    r = Real(size(data,1),1);

    for i = 1:(size(data,1)-1)
       fg2.addFactor(@constmult,r(i+1),r(i),1.1); 
    end

    i = 1;
    while fg.hasNext()
        r(i).Input = [1 1];
        r(i+1).Input = [1 1];

        fg.solve(false);
        fg2.solve();


        assertElementsAlmostEqual(vars.FirstVar.Belief,r(i).Belief);

        fg.advance();
        i = i + 1;
    end

    setSolver(solver);
end
