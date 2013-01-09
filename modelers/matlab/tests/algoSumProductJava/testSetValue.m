function testSetValue()

    %setValue of single dimensional thing
    d = Discrete({1,2,3},3,1);
    fg = FactorGraph();
    fg.addFactor(@(a) 1, d);
    value = randi(3,3,1);
    d.Value = value;
    assertEqual(d.Value,value);
    
    %setValue of doubles
    d = Discrete({1,2,3},3,2);
    fg = FactorGraph();
    fg.addFactor(@(a) 1, d);
    value = randi(3,3,2);
    d.Value = value;
    assertEqual(d.Value,value);

    d = Discrete({'a','bb','ccc'},2,1);
    fg = FactorGraph();
    fg.addFactor(@(a,b) 1, d(1),d(2));
    value = {'a'; 'bb'};
    msg = '';
    try
        d.Value = value;
    catch E
        msg = E.message;
    end

    assertEqual(msg,'Only scalar domains currently supported');

    %TODO: getValue when sovler not set

    %TODO: setValue when solver not set

    %TODO: setValue of Bits

    %TODO: setValue of strings

    %TODO: setValue when domainElements are arrays

    %TODO: setValue when domain is lost?
end
