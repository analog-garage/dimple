function testBlastFromPastCornerCases()

    %Test blastFromPastFactors go away when we reset
    a = Bit();
    b = Bit();
    c = Bit();
    ng = FactorGraph(a,b,c);
    ng.addFactor(@xorDelta,a,b,c);

    fg = FactorGraph();
    s = BitStream();
    p = Bit();
    p.Name = 'param';
    fs = fg.addFactor(ng,s,s.getSlice(2),p);
    fs.BufferSize = 5;


    fg.advance();

    expectedVars = 7;
    expectedFactors = 7;

    assertEqual(expectedVars,length(fg.Variables));
    assertEqual(expectedFactors,length(fg.Factors));

    fg.reset();

    expectedVars = 7;
    expectedFactors = 5;

    assertEqual(expectedVars,length(fg.Variables));
    assertEqual(expectedFactors,length(fg.Factors));


    %What happens if twoFSs share a parameter.  Do we update blast from pasts correctly?
    const1 = [.6 .4];
    const2 = [.7 .3];
    input = .2;

    b = Bit();
    ng1 = FactorGraph(b);
    ng1.addFactor(@constFactor,b,const1);

    b = Bit();
    ng2 = FactorGraph(b);
    ng2.addFactor(@constFactor,b,const2);

    fg = FactorGraph();
    b = Bit();
    b.Input = input;
    fg.addRepeatedFactor(ng1,b);
    fg.addRepeatedFactor(ng2,b);

    fg.initialize();

    fg2 = FactorGraph();
    c = Bit();
    c.Input = input;

    for i = 1:10
      fg.solve(false);

      fg2.addFactor(@constFactor,c,const1);
      fg2.addFactor(@constFactor,c,const2);
      fg2.solve();

      assertElementsAlmostEqual(b.Belief,c.Belief);

      fg.advance();
    end

    assertEqual(4,length(fg.Factors));
    assertEqual(1,length(fg.Variables));

    fg.reset();

    assertEqual(2,length(fg.Factors));
    assertEqual(1,length(fg.Variables));

    %mix param with stream
    %Fix memory leak
end
