function testFactorFunctionRegistry()
% Unit test for FactorFunctionRegistry class

ffr = FactorFunctionRegistry();
env = DimpleEnvironment.active();
ffr2 = env.FactorFunctions;

pffr = ffr.getProxyObject();
assertEqual(pffr, unwrapProxyObject(ffr));

% Both should refer to the same underlying Java FactorFunctionRegistry
pffr2 = ffr2.getProxyObject();
assertEqual(pffr.getDelegate(), pffr2.getDelegate());

ffr3 = wrapProxyObject(pffr);
assertEqual(pffr, ffr3.getProxyObject());

pffr.reset(); % Just in case some other test messed with the registry

assertEqual({'com.analog.lyric.dimple.factorfunctions'}, ffr.Packages);

xorHandle = ffr.get('Xor');
assertEqual('function_handle', class(xorHandle));
xor = xorHandle();
assertEqual('com.analog.lyric.dimple.factorfunctions.Xor', class(xor));
assertEqual('Xor', char(xor.getName()));
xor2 = xorHandle('xor2');
assertEqual('com.analog.lyric.dimple.factorfunctions.Xor', class(xor2));
assertEqual('xor2', char(xor2.getName()));
xor3 = ffr.instantiate('Xor', 'xor3');
assertEqual('com.analog.lyric.dimple.factorfunctions.Xor', class(xor3));
assertEqual('xor3', char(xor3.getName()));

try
    ffr.get('Barf');
    error('no exception thrown');
catch err
    assertEqual('Cannot find factor function [Barf]', err.message);
end

names = ffr.Names;
assert(iscellstr(names));
assert(issorted(names));
assert(numel(names) > 170); % This number should only grow as we add more builtins
assertEqual(1, sum(ismember(names,{'Normal'})));
assertEqual(1, sum(ismember(names,{'com.analog.lyric.dimple.factorfunctions.Normal'})));

ffr.addPackage('com.analog.lyric.dimple.test.matlabproxy');
assertEqual('com.analog.lyric.dimple.test.matlabproxy.MyFactorFunction', ...
    class(ffr.instantiate('MyFactorFunction')));

pffr.reset();

end

