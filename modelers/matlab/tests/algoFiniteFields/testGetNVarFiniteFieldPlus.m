function testGetNVarFiniteFieldPlus()

    % Skip this test if the Communications Toolbox is unavailable.
    if isempty(which('gf')), return; end
    
    global getNVarFiniteFieldPlusGraphCache___
    getNVarFiniteFieldPlusGraphCache___ = [];
    
    m = 2;
    
    numElements = 2^m;
    domain = 0:numElements-1;
    
    tmp = gf(domain,m);
    prim_poly = tmp.prim_poly;

    [graph,vars] = getNVarFiniteFieldPlus(prim_poly,4);
    
    for aindex = 1:numElements
        for bindex = 1:numElements
            for cindex = 1:numElements
                a = tmp(aindex);
                b = tmp(bindex);
                c = tmp(cindex);
                d = a+b+c;
                

                priors = zeros(1,numElements);
                priors(aindex) = 1;
                vars(1).Input = priors;
                priors = zeros(1,numElements);
                priors(bindex) = 1;
                vars(2).Input = priors;
                priors = zeros(1,numElements);
                priors(cindex) = 1;
                vars(3).Input = priors;
                
                graph.Solver.setNumIterations(2);
                graph.solve();
                
                ans = d.x;
                ans = double(ans);
                assertEqual(ans,vars(4).Value);
            end
        end
    end
    
    %TODO: test that different poly results in different graph
    getNVarFiniteFieldPlus(3,4);
    getNVarFiniteFieldPlus(prim_poly,4);
    assertEqual(getNVarFiniteFieldPlusGraphCache___.NumGraphs,2);

end
