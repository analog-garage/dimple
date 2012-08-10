function testPrinting

    fg = FactorGraph();
    expectedName = 'MyFactorGraph';
    fg.Name = expectedName;
    x = extractName(evalc('fg'));
    assertTrue(isequal(x,expectedName));
    
    b = Bit(3,1);
    expectedName = 'MyBit';
    b(1).Name = expectedName;
    x = extractName(evalc('b(1)'));
    assertTrue(isequal(x,expectedName));
    
    f = fg.addFactor(@xorDelta,b);
    expectedName = 'MyFactor';
    f.Name = expectedName;
    x = extractName(evalc('f'));
    assertTrue(isequal(x,expectedName));
   
    expectedName = 'fg';
    fg.Label = expectedName;
    x = extractName(evalc('fg'));
    assertTrue(isequal(x,expectedName));
    
    
    expectedName = 'b';
    b.Label = expectedName;
    x = extractName(evalc('b(1)'));
    assertTrue(isequal(x,expectedName));
    x = extractName(evalc('b(2)'));
    assertTrue(isequal(x,expectedName));
    x = extractName(evalc('b(3)'));
    assertTrue(isequal(x,expectedName));
    
    
    expectedName = 'f';
    f.Label = expectedName;
    x = extractName(evalc('f'));
    assertTrue(isequal(x,expectedName));
    
    function name = extractName(result)
        ind = find(result=='=',1);
        name = strtrim(result(ind+1:end));
    end

end
