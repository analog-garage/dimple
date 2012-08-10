function testVariable()

    msg = '';
    try
        FiniteFieldVariable([3 3]);
    catch E
        msg = E.message;
    end
    pass = ~isempty(findstr(msg,'FiniteFieldVariable expects a polynomial to be specified as a single decimal number'));
    assertTrue(pass);
end

