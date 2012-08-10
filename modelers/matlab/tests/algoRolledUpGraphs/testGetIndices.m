function testGetIndices

    a = Bit();
    b = Bit();
    ng = FactorGraph(a,b);
    ng.addFactor(@xorDelta,a,b);

    fg = FactorGraph();

    s = BitStream();

    fg.addFactor(ng,4,s,s.getSlice(2));


    assertEqual(s.FirstVarIndex,1);
    assertEqual(s.LastVarIndex,5);
    assertTrue(s.FirstVar==s.get(1));
    assertTrue(s.LastVar==s.get(5));

    fg.advance();

    assertEqual(s.FirstVarIndex,2);
    assertEqual(s.LastVarIndex,6);
    assertTrue(s.FirstVar==s.get(2));
    assertTrue(s.LastVar==s.get(6));
end
