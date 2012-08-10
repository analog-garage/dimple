function testNormalization()

    if strcmp(class(getSolver()),'CSolver') == 0

        x = Variable({1,0});
        x.Input = [2 8];
        fg = FactorGraph();
        fg.addFactor(@xorDelta,x);
        b = x.Belief;
        assertElementsAlmostEqual([.2 .8],b);
    end
end
