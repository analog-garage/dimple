function testFactorBelief()


    %Create a Factor Graph
    b = Bit(3,1);
    fg = FactorGraph();
    f = fg.addFactor(@funkyFactor,b(1),b(2),b(3));

    %Set inputs
    input = [.8 .8 .6];
    b.Input = input;

    %We have to solver right now
    fg.Solver.setNumIterations(1);
    fg.solve();

    %funkyFactor only allows these two combos.
    expectedDomain = {0,0,0; 1,1,1};
    assertEqual(expectedDomain,f.Domain);

    %Bit stores values as 1,0.
    expectedIndices = int32([1,1,1; 2,2,2]);
    assertEqual(expectedIndices,f.Indices);

    %Let's calculate the belief by hand and compare to Dimple.
    val0 = prod(1-input)*funkyFactor(0,0,0);
    val1 = prod(input)*funkyFactor(1,1,1);
    total = val0 + val1;
    val0 = val0 / total;
    val1 = val1 / total;
    expectedBelief = [val0; val1];
    assertElementsAlmostEqual(expectedBelief,f.Belief);

    %Now we create a full belief and compare to our expected.
    expectedFullBelief = zeros(2,2,2);
    expectedFullBelief(1,1,1) = val0;
    expectedFullBelief(2,2,2) = val1;
    assertElementsAlmostEqual(expectedFullBelief,f.FullBelief);

end
