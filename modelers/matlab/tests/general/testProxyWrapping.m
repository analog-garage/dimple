function testProxyWrapping()
%TESTPROXYWRAPPING Tests for wrapping of Dimple objects with proxy objects

assert(isempty(wrapProxyObject([])));

try
    wrapProxyObject(42);
    assert(false);
catch err
    assert(all(err.message == 'not supported'));
end

domain = DiscreteDomain(1:2);
domain2 = wrapProxyObject(domain.IDomain);
assert(isa(domain2,'DiscreteDomain'));
assert(domain.IDomain == domain2.IDomain);

domain = RealDomain(0,10);
domain2 = wrapProxyObject(domain.IDomain);
assert(isa(domain2,'RealDomain'));
assert(domain.IDomain == domain2.IDomain);

% TODO: more test cases...

% Make sure that variables from factors have correctly wrapped domains.
fg = FactorGraph();
fg.addFactor(@xorDelta, Discrete(1:2,1,2));
assert(all(cell2mat(fg.Variables{1}.Domain.Elements(:)) == [1;2]));
assert(all(cell2mat(fg.Factors{1}.Variables{1}.Domain.Elements(:)) == [1;2]));

end

