function testComboTableZeros()

    fac = @(x,y) 0;
    fg = FactorGraph();
    b = Bit(2,1);
    
    error = 0;
    try
        fg.addFactor(fac,b);
    catch
        error = 1;        
    end
    
    assertTrue(error==1);

end
