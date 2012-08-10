function testVariable()
    b = Bit();
    
    errorThrown = false;
    try
        b.asdf = 1;
    catch
        errorThrown = true;
    end
    
    assertTrue(errorThrown);
    
    assertTrue(size(b,1)==1);
        
 
end
