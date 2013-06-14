function testLP()
    bLog = false;
    dtrace(bLog, '++testLP');
    
    if (isempty(ver('optim')))
        dtrace(true, 'WARNING: testLP was skipped because optimization toolbox not installed');
        return;
    end
    
    setSolver('lp');

    X=Variable(0:1,1,1);
    X.Name='X';
    Y=Variable(0:1,1,1);
    Y.Name='Y';
    Z=Variable(0:1,1,1);
    Z.Name='Z';
    notbothone=@(x,y) 1-(x==1)*(y==1);
    fg=FactorGraph();
    fg.addFactor(notbothone,X,Y);
    fg.addFactor(notbothone,Y,Z);
    fg.addFactor(notbothone,X,Z);
    X.Input=[0.3;0.7];
    Y.Input=[0.2;0.8];
    Z.Input=[0.6;0.3];

    fg.solve();
    
    X.Belief
    assertTrue(abs(sum(X.Belief - [1 0])) < 1e-12);
    assertTrue(abs(sum(Y.Belief - [0 1])) < 1e-12);
    assertTrue(abs(sum(Z.Belief - [1 0])) < 1e-12);
    
    dtrace(bLog, '--testMinSum');
end

