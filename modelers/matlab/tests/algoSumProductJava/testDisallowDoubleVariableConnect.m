function testDisallowDoubleVariableConnect()

    fg = FactorGraph();
    b = Bit(3,1);
    fg.addFactor(@xorDelta,b);
   
    fg2 = FactorGraph();
    
    exceptionThrown = 0;
    
    try
        fg2.addFactor(@xorDelta,b);
    catch errorException
        exceptionThrown = 1;
    end
    
    assertTrue(exceptionThrown==1);

end

