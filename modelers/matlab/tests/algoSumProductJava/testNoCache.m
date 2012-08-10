function testNoCache()
    fg = FactorGraph();

    myfunc = @(x,y) rand();

    b = Bit(2,1);

    for i = 1:2
        fg.addFactorNoCache(myfunc,b);
    end

    assertTrue(fg.Factors{1}.IFactor.getModelerObject().getFactorFunction()~= ...
        fg.Factors{2}.IFactor.getModelerObject().getFactorFunction());
end
