function testAddFactorErrors( )

    msg = '';
    try

        domain2={[0;0];[0;1];[1;0];[1;1]}; 
        X=Variable(domain2,2,1); 
        fg=FactorGraph(); 
        weirdxor=@(x) 1;
        fg.addFactor(weirdxor,X);
    catch E
        msg = E.message;
    end

    pass = ~isempty(findstr(msg,'Dimple does not currently support'));
    assertTrue(pass);
end

