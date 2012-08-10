function [ output_args ] = testAddFactorWithVarVector( )

    %Should not error
    fg=FactorGraph();

    X=Variable(0:1,3,1);
    mask=[0.3; 1; 0.3];

    evalfunc=@(x,mask) exp(-sum(1-sum(mask.*x)^2));

    f = fg.addFactor(evalfunc,X,mask);

    %Also should not error
    fg=FactorGraph();

    X=Variable(0:1,1,3);
    mask=[0.3 1 0.3];

    evalfunc=@(x,mask) exp(-sum(1-sum(mask.*x)^2));

    f = fg.addFactor(evalfunc,X,mask);

end

