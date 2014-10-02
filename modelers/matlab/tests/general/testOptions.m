function testOptions()
%testOptions Runs unit test of Dimple's option mechanism.
%
%   This only tests the overall option mechanism, not the behavior
%   of individual option settings.

env = DimpleEnvironment.active();
env.clearLocalOptions();

fg = FactorGraph();
v = Bit(2,2);
fg.addFactor('Xor',v);

% Make sure all options in dimpleOptions() are accepted.
optionNames = dimpleOptions();
for (i = 1:numel(optionNames))
    optionName = optionNames{i};
    val = fg.getOption(optionName);
    fg.setOption(optionName, val);
    fg.unsetOption(optionName);
end
assertTrue(isempty(getLocalOptions(fg)));

% Try a bogus key
try
    fg.getOption('Foo.bar');
    assertTrue(false, 'Expected an error');
catch err
end

assertEqual({1 1; 1 1}, v.getOption('SolverOptions.iterations'));
v.setOption('SolverOptions.iterations', 3);
assertEqual({3 3; 3 3}, v.getOption('SolverOptions.iterations'));
v(1,1).setOption('SolverOptions.iterations', 4);
v(2,:).setOption('SolverOptions.iterations', 5);
assertEqual({4 3; 5 5}, v.getOption('SolverOptions.iterations'));
assertEqual({3;5}, v(:,2).getOption('SolverOptions.iterations'));
v(2,:).unsetOption('SolverOptions.iterations');
assertEqual({4 3; 1 1}, v.getOption('SolverOptions.iterations'));
v.clearLocalOptions();
assertEqual({1 1; 1 1}, v.getOption('SolverOptions.iterations'));

env.setOption('SolverOptions.iterations', 42);
assertEqual(42, env.getOption('SolverOptions.iterations'));
assertEqual(42, v(1,1).getOption('SolverOptions.iterations'));
v(2,2).setOption('SolverOptions.iterations',12);
assertEqual({42 42; 42 12}, v.getOption('SolverOptions.iterations'));

v.clearLocalOptions();
env.clearLocalOptions();
fg.clearLocalOptions();

assertTrue(isempty(fg.getLocalOptions()));
fg.setOption('SolverOptions.iterations', 23);
fg.setOption('DimpleOptions.randomSeed', 42);
options = fg.getLocalOptions();
assertEqual({'DimpleOptions.randomSeed', 42; 'SolverOptions.iterations', 23}, options);
fg.clearLocalOptions();
assertTrue(isempty(fg.getLocalOptions()));
fg.setOptions(options);
assertEqual({'DimpleOptions.randomSeed', 42; 'SolverOptions.iterations', 23}, options);

end

