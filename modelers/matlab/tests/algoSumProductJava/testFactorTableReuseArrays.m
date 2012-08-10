function testFactorTableReuseArrays( )
    
    fg = FactorGraph();

    %This should not throw an error.  It will if we try to make the domains
    %match and java fails to see that arrays are equal
    fg.addFactor(@(x) 1, Discrete({[1 2 3]}));
    fg.addFactor(@(x) 1, Discrete({[1 2 3]}));
    
end

