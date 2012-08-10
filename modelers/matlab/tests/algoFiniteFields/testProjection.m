function testProjection()

    % Skip this test if the Communications Toolbox is unavailable.
    if isempty(which('gf')), return; end
    
    m = 3;

    numElements = 2^m;
    domain = 0:numElements-1;
    
    tmp = gf(domain,m);
    prim_poly = tmp.prim_poly;

    for i = 0:numElements-1

        bitValue = dec2bin(i,m)-'0';

        ff = FiniteFieldVariable(prim_poly);
        bits = Bit(m,1);

        fg = FactorGraph();

        fg.addFactor(@finiteFieldProjection,ff,0:m-1,flipud(bits));
        
        %%%%%
        %give 100% probabilities for bits and check gfs are right
        bits.Input = bitValue;
        fg.solve();
        assertEqual(double(ff.Value),i);


        
        %%%%
        %give 100% probabilities for gfs and see if bits are right
        ff = FiniteFieldVariable(prim_poly);
        bits = Bit(m,1);

        fg = FactorGraph();
        
        fg.addFactor(@finiteFieldProjection,ff,0:m-1,flipud(bits));

        %%%%%
        %give 100% probabilities for bits and check gfs are right
        priors = zeros(1,numElements);
        priors(i+1) = 1;
        ff.Input = priors;
        fg.solve();
        
        assertTrue(all(bitValue == bits.Value'));
        
        
    end
end
