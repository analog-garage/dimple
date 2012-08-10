function testMultiplication1()

    % Skip this test if the Communications Toolbox is unavailable.
    if isempty(which('gf')), return; end
    
    m = 3;

    numElements = 2^m;
    domain = 0:numElements-1;

    tmp = gf(domain,m);
    prim_poly = tmp.prim_poly;

    x = gf(repmat(domain,numElements,1),m);
    y = x';
    z = x .* y;
    %x = gf(1,m);
    %y = gf(1,m);
    %z = x*y;

    %%%%%%%%%%%%%%%%%%%%
    %Test addition
    ff1 = FiniteFieldVariable(prim_poly);
    ff2 = FiniteFieldVariable(prim_poly);
    ff3 = FiniteFieldVariable(prim_poly);

    fg = FactorGraph();


    %Set constant
    fg.addFactor(@finiteFieldMult,ff1,ff2,ff3);



    for i = 1:prod(size(x))

        %Get x integer
        priors = zeros(1,numElements);
        ind = x(i);
        ind = ind.x+1;
        priors(ind) = 1;
        ff1.Input = priors;

        priors = zeros(1,numElements);
        ind = y(i);
        ind = ind.x+1;
        priors(ind) = 1;
        ff2.Input = priors;


        fg.solve();

        expected = z(i);
        expected = expected.x;
        assertEqual(double(expected),double(ff3.Value));


    end

end
