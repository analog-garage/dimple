function testDepthFirstSearch()

    fg = FactorGraph();
    b = Bit(6,1);
    for i = 1:6
        b(i).Name = sprintf('b%d',i);
    end
    f1 = fg.addFactor(@xorDelta,b(1:4));
    f1.Name = 'f1';
    f2 = fg.addFactor(@xorDelta,b(4:6));
    f2.Name = 'f2';

    nodes = fg.depthFirstSearch(b(1),0);

    assertEqual(length(nodes),1);
    assertEqual(nodes{1}.Name,b(1).Name);

    nodes = fg.depthFirstSearch(b(1),1);
    assertEqual(length(nodes),2);
    assertEqual(nodes{1}.Name,b(1).Name);
    assertEqual(nodes{2}.Name,f1.Name);

    nodes = fg.depthFirstSearch(b(1),2);
    assertEqual(length(nodes),5);
    assertEqual(nodes{1}.Name,b(1).Name);
    assertEqual(nodes{2}.Name,f1.Name);
    assertEqual(nodes{3}.Name,b(2).Name);
    assertEqual(nodes{4}.Name,b(3).Name);
    assertEqual(nodes{5}.Name,b(4).Name);


    %Test depth first search top, flat, somewhere inbetween (in depth first
    %search test)
    fg = FactorGraph();
    b = Bit(3,1);
    
    fgbottom = FactorGraph(b);
    ivbottom = Bit();
    ivbottom.Name = 'ivbottom';
    f = fgbottom.addFactor(@xorDelta,[b; ivbottom]);
    f.Name = 'fbottom';
    
    b = Bit(2,1);
    fgmiddle = FactorGraph(b);
    ivmiddle = Bit();
    ivmiddle.Name = 'ivmiddle';
    f = fgmiddle.addFactor(fgbottom,[ivmiddle; b]);
    f.Name = 'fgmiddle';
    
    b = Bit(3,1);
    for i = 1:3
        b(i).Name = sprintf('btop%d',i);
    end
    f1 = fg.addFactor(fgmiddle,b(1),b(2));
    f2 = fg.addFactor(fgmiddle,b(2),b(3));
    f1.Name = 'fm1';
    f2.Name = 'fm2';
    
    %fg.plot('labels',1);
    
    %Test Top
    nodes = fg.depthFirstSearchTop(b(2),0);
    expectedNames = {'btop2'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearchTop(b(2),1);
    expectedNames = {'btop2','fm1','fm2'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearchTop(b(2),2);
    expectedNames = {'btop2','fm1','btop1','fm2','btop3'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearchTop(b(2),3);
    expectedNames = {'btop2','fm1','btop1','fm2','btop3'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
 
    %Test Flat
    nodes = fg.depthFirstSearch(b(2),0);
    expectedNames = {'btop2'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearch(b(2),1);
    expectedNames = {'btop2','fbottom','fbottom'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end

    nodes = fg.depthFirstSearch(b(2),2);
    expectedNames = {'btop2','fbottom','ivmiddle','btop1','ivbottom','fbottom','ivmiddle','btop3','ivbottom'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearch(b(2),3);
    expectedNames = {'btop2','fbottom','ivmiddle','btop1','ivbottom','fbottom','ivmiddle','btop3','ivbottom'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearchFlat(b(2),3);
    expectedNames = {'btop2','fbottom','ivmiddle','btop1','ivbottom','fbottom','ivmiddle','btop3','ivbottom'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    %Test inbetween
    nodes = fg.depthFirstSearch(b(2),0,1);
    expectedNames = {'btop2'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearch(b(2),1,1);
    expectedNames = {'btop2','fgmiddle','fgmiddle'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearch(b(2),2,1);
    expectedNames = {'btop2','fgmiddle','ivmiddle','btop1','fgmiddle','ivmiddle','btop3'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearch(b(2),3,1);
    expectedNames = {'btop2','fgmiddle','ivmiddle','btop1','fgmiddle','ivmiddle','btop3'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    %Test searching in sub graph
    ivmiddle = fg.NestedGraphs{1}.VariablesTop{1};
    
    
    nodes = fg.depthFirstSearchTop(ivmiddle,0);
    expectedNames = {'ivmiddle'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.depthFirstSearchTop(ivmiddle,1);
    expectedNames = {'ivmiddle','fgmiddle'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.NestedGraphs{1}.depthFirstSearchTop(ivmiddle,2);
    expectedNames = {'ivmiddle','fgmiddle'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
    nodes = fg.NestedGraphs{1}.depthFirstSearch(ivmiddle,2);
    expectedNames = {'ivmiddle','fbottom','ivbottom'};
    assertEqual(length(expectedNames),length(nodes));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},nodes{i}.Name);
    end
    
end
