function testAddFactorTableErrors()
    fg = FactorGraph();

    fail = false;

    try
        table = fg.createTable([0 0; 0 0; 1 1],[1 1 1],DiscreteDomain({1,0}),DiscreteDomain({1,0}));
    catch E
        assertTrue(findstr(E.message,'Table Factor contains multiple rows with same set of indices') > 0);
        fail = true;
    end

    assertTrue(fail);



    fg = FactorGraph();
    fail = false;
    try
        table = fg.createTable([0 0; 2 2; 1 1],[1 1 1],DiscreteDomain({1,0}),DiscreteDomain({1,0}));
    catch E
        assertTrue(findstr(E.message,'index: 2 is larger than domain of variable number 0')>0);
        fail = true;
    end

    assertTrue(fail);

end
