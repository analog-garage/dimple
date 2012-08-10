function testMultiplication3
    
    % Skip this test if the Communications Toolbox is unavailable.
    if isempty(which('gf')), return; end
    
    %{
    Set uniform priors on 1,w,w^2, for X and Y.  Output should be
    p(Z=1) = 1/9
    p(Z=w) = 2/9
    p(Z=w^2) = 3/9
    p(Z=w^3) = 2/9
    p(Z=w^4) = 1/9
    %}

    m = 4;
    tmp = gf(0,m);
    prim_poly = tmp.prim_poly;

    x = FiniteFieldVariable(prim_poly);
    y = FiniteFieldVariable(prim_poly);
    z = FiniteFieldVariable(prim_poly);

    fg = FactorGraph();

    fg.addFactor(@finiteFieldMult,x,y,z);

    pows = x.Solver.getTables().getPowerTable();

    input = zeros(size(x.Domain.Elements));
    input(1+pows(1+0)) = 1/3;
    input(1+pows(1+1)) = 1/3;
    input(1+pows(1+2)) = 1/3;
    x.Input = input;
    y.Input = input;
    fg.Solver.setNumIterations(1);
    fg.solve();

    assertElementsAlmostEqual(1/9,z.Belief(1+pows(1)));
    assertElementsAlmostEqual(2/9,z.Belief(1+pows(2)));
    assertElementsAlmostEqual(3/9,z.Belief(1+pows(3)));
    assertElementsAlmostEqual(2/9,z.Belief(1+pows(4)));
    assertElementsAlmostEqual(1/9,z.Belief(1+pows(5)));
    
 
end
