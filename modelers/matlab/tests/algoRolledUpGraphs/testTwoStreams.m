function testTwoStreams()

    in = Bit();
    out = Bit();
    ng = FactorGraph(in,out);
    ng.addFactor(@xorDelta,in,out);

    fg = FactorGraph();
    b = BitStream();

    fg.addFactor(ng,b,b.getSlice(2));

    c = BitStream();

    fg.addFactor(ng,c,c.getSlice(2));

    bsrc = DoubleArrayDataSource();
    csrc = DoubleArrayDataSource();


    bsrc.add(repmat([.8 .2],5,1));
    csrc.add(repmat([.6 .4],5,1));

    b.DataSource = bsrc;
    c.DataSource = csrc;

    fg.initialize();

    fgb = FactorGraph();
    fgb_bit = Bit();
    fgb_bit.Input = .2;

    fgc = FactorGraph();
    fgc_bit = Bit();
    fgc_bit.Input = .4;


    i = 1;
    while fg.hasNext()
        fg.solve(false);

        nextbBit = Bit();
        nextcBit = Bit();

        fgb.addFactor(@xorDelta,fgb_bit,nextbBit);
        fgc.addFactor(@xorDelta,fgc_bit,nextcBit);

        nextbBit.Input = .2;
        nextcBit.Input = .4;

        bbelief = b.FirstVar.Belief;
        cbelief = c.FirstVar.Belief;

        fgb.solve();
        fgc.solve();

        bbeliefexp = fgb_bit.Belief;
        cbeliefexp = fgc_bit.Belief;
        assertElementsAlmostEqual(bbeliefexp,bbelief(2));
        assertElementsAlmostEqual(cbeliefexp,cbelief(2));

        fg.advance();
        i = i+1;

        fgb_bit = nextbBit;
        fgc_bit = nextcBit;
    end


    %Test two streams with one variables source
    in1 = [.8 .2];
    in2 = [.7 .3];
    v = Bit();
    ng1 = FactorGraph(v);
    ng1.addFactor(@constFactor,v,in1);

    v = Bit();
    ng2 = FactorGraph(v);
    ng2.addFactor(@constFactor,v,in2);

    s = BitStream();
    data = repmat([.6 .4],20,1);
    s.DataSource = DoubleArrayDataSource(data);

    fg = FactorGraph();
    fg.addFactor(ng1,s);
    fg.addFactor(ng2,s);

    fg2 = FactorGraph();
    b = Bit();
    fg2.addFactor(@constFactor,b,in1);
    fg2.addFactor(@constFactor,b,in2);
    b.Input = .4;
    fg2.solve();
    %b.Belief

    fg.initialize();

    s.FirstVar.Name = 'firstVar';
    s.LastVar.Name = 'lastVar';
    
    while fg.hasNext()
        fg.solve(false);        
        assertElementsAlmostEqual(s.FirstVar.Belief(2),b.Belief);        
        fg.advance();
    end

    %TODO: should I test different buffer sizes?

end
