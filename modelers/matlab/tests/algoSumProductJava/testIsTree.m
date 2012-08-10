function testIsTree()

    %%%%%%%%%%%%%%%%%%%%%%%%%%%
    %Simple Tests
    %%%%%%%%%%%%%%%%%%%%%%%%%%%

    fg = FactorGraph();
    assertTrue(fg.isTree);

    b = Bit(2,1);

    fg.addFactor(@xorDelta,b);

    assertTrue(fg.isTree);

    f = fg.addFactor(@xorDelta,b);

    assertFalse(fg.isTree);

    fg.removeFactor(f);

    assertTrue(fg.isTree);

    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%
    %Tests involving nesting
    %%%%%%%%%%%%%%%%%%%%%%%%%%%
    b = Bit(3,1);
    fgbottom = FactorGraph(b);
    ivbottom = Bit();
    fgbottom.addFactor(@xorDelta,b(1),b(2),ivbottom);
    fgbottom.addFactor(@xorDelta,b(2),ivbottom);
    
    b = Bit(2,1);
    fgmiddle = FactorGraph(b);
    ivmiddle = Bit();
    fgmiddle.addFactor(fgbottom,b(1),ivmiddle(1),ivmiddle);
    fgmiddle.addFactor(fgbottom,b(2),ivmiddle(1),ivmiddle);
    
    fg = FactorGraph();
    b = Bit(2,1);
    fg.addFactor(fgmiddle,b);
       
    %isTreeTop
    assertTrue(fg.isTreeTop);
    assertFalse(fg.NestedGraphs{1}.isTreeTop);
    assertTrue(fg.NestedGraphs{1}.NestedGraphs{1}.isTreeTop);
    
    %isTreeFlat
    assertFalse(fg.isTree);
    assertFalse(fg.NestedGraphs{1}.isTree);
    assertTrue(fg.NestedGraphs{1}.NestedGraphs{1}.isTree);
    
    %isTree somewhere in middle
    assertFalse(fg.isTree(1));
    assertFalse(fg.NestedGraphs{1}.isTree(1));
    assertTrue(fg.NestedGraphs{1}.NestedGraphs{1}.isTree(1));
    
    %Create a better graph for determining if tree down a level but not two
    b = Bit();
    fg1 = FactorGraph(b);
    iv = Bit();
    fg1.addFactor(fgmiddle,b,iv);
    
    fg2 = FactorGraph();
    b = Bit();
    fg2.addFactor(fg1,b);
    
    assertTrue(fg2.isTree(0));
    assertTrue(fg2.isTree(1));
    assertFalse(fg2.isTree(2));
    
    fg = FactorGraph();
    a = Bit();
    b = Bit();
    ng = FactorGraph(a,b);
    ng.addFactor(@xorDelta,a,b);
    a = Bit();
    b = Bit();
    fg.addFactor(ng,a,b);
    assertTrue(fg.isTree());
    
end
