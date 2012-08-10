
function testConstMult

    fg = FactorGraph();
    a = Real();
    b = Real();
    c = 5;

    fg.Solver = 'Gaussian';
    
    fg.addFactor(@constmult,a,b,c);

    a.Input = [10 1];
    fg.solve();

    assertEqual(b.Belief,[10/5; 1/5]);
    
    a.Input = [0 Inf];
    b.Input = [10, 1];
    
    fg.solve();
    
    assertEqual(a.Belief,[10*5; 1*5]);

end
