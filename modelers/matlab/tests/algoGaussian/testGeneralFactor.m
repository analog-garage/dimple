

v = Real(3,1);

fg = FactorGraph();
fg.Solver = 'Gaussian';
fg.Solver.setNumSamples(100000);
fg.Solver.setSeed(0);

%%%%%%%%%%%%%%%%%%%%%%%%
%Test add
inputs = [9 2; ...
          3 2; ...
          6 2];

for i = 1:3

    tmpinputs = inputs;
    
    tmpinputs(i,:) = [0 Inf];
    
    v(1).Input = tmpinputs(1,:);
    v(2).Input = tmpinputs(2,:);
    v(3).Input = tmpinputs(3,:);

    f = fg.addFactor(com.analog.lyric.dimple.test.GaussianAddFactorFunction(),v(1),v(2),v(3));

    fg.solve();

    actualBelief = v(i).Belief;
    
    fg.removeFactor(f);
    
    f = fg.addFactor(@add,v(1),v(2),v(3));

    fg.solve();

    expectedBelief = v(i).Belief;
    
    fg.removeFactor(f);
    
    diff = abs(actualBelief - expectedBelief);
    assertTrue(all(diff < .02));

end

%%%%%%%%%%%%%%%%%%%%%%%%
%Test square
a = v(1);
b = v(2);
f = fg.addFactor(com.analog.lyric.dimple.solvers.gaussian.factorfunctions.GaussianSquareFactorFunction(),a,b);

a.Input = [25 10];
b.Input = [0 Inf];

fg.solve();

expectedBelief = [0; 5];
diff = abs(b.Belief-expectedBelief);
assertTrue(all(diff < .02));

a.Input = [0 Inf];
b.Input = [20 12];

fg.solve();

expectedMu = [b.Input(1)^2 + b.Input(2)^2];
assertTrue(abs(a.Belief(1)-expectedMu) < 1);


%%%%%%%%%%%%%
%Test swedish fish
assertEqual(char(f.Solver.getFactor.getFactorFunction().runSwedishFish()),'mmmmm');


fg.Solver.setMaxNumTries(10);

exFound = false;
message = '';
try
    fg.solve();
catch E
    exFound = true;
    message = E.message;
end

assertTrue(exFound);
assertTrue(findstr(message,'Failed to get desired number of samples')>0);


%%%%%%%%%%%%
%Test cube

fg = FactorGraph();
fg.Solver = 'Gaussian';
fg.Solver.setNumSamples(10000);

a = Real();
b = Real();

f = fg.addFactor(com.analog.lyric.dimple.solvers.gaussian.factorfunctions.GaussianPowerFactorFunction(3),a,b);

a.Input = [27 10];
b.Input = [0 Inf];

fg.solve();

expectedBelief = [3; .45];
diff = abs(b.Belief-expectedBelief);
assertTrue(all(diff < .1));

