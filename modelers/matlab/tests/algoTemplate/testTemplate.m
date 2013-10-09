%function testTemplate()

    %Test Factor and Belief
    fg = FactorGraph();
    fg.Solver = 'template';
    d = Discrete([0 1],3,1);
    fg.addFactor(@xorDelta,d);
    d1Input = [3 4];
    d2Input = [6 7];
    d3Input = [10 20];
    d(1).Input = d1Input;
    d(2).Input = d2Input;
    d(3).Input = d3Input;
    fg.solve();
    assertTrue(all(d(3).Belief == (d1Input + d2Input + d3Input)'));

    %Test variable
    fg = FactorGraph();
    fg.Solver = 'template';
    outside = Discrete([0 1],3,1);
    inside = Discrete([0 1]);
    for i = 1:3
        fg.addFactor(@xorDelta,outside(i),inside);
    end
    outside(1).Input = d1Input;
    outside(2).Input = d2Input;
    outside(3).Input = d3Input;
    inInput = [40 50];
    inside.Input = inInput;
    fg.solve();
    assertTrue(all(outside(3).Belief() == (d1Input + d2Input + d3Input + inInput)'));

    %Eventually add support for and test rolled up graphs
    a = Bit();
    b = Bit();
    ng = FactorGraph(a,b);
    ng.addFactor(@xorDelta,a,b);
    b = BitStream();
    fg = FactorGraph();
    fg.Solver = 'template';
    fs = fg.addFactor(ng,b,b.getSlice(2));
    fs.BufferSize = 1;
    b.get(1).Input = [0.7 0.3];
    fg.initialize();
    fg.solveOneStep();
    fg.advance();
    fg.solveOneStep();
    b.Variables(2).Belief
   
    
%end