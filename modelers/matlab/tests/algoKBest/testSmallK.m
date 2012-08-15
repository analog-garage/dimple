function testSmallK()

    N = 10;


    vars = Discrete(1:N,3,1);

    func = @(x,y,z) x==y && y==z;

    fg = FactorGraph();

    f = fg.addFactor(func,vars(1),vars(2),vars(3));

    vars.Input = 1:N;


    %fg.Solver = 'MinSum';

    fg.Solver = 'minsum';
    f.Solver.setK(2);

    %fg.NumIterations = 10;

    fg.solve();

    fg.solve();

    expected10 = 10^3/(10^3+9^3);
    expected9 = 9^3/(10^3+9^3);

    expected = zeros(1,9);
    expected(9) = expected9;
    expected(10) = expected10;

    for i = 1:3
        assertElementsAlmostEqual(expected,vars(i).Belief);
    end

end
