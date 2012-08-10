function testBufferSize()

    %Test specifying buffer size when creating graph
    x = Bit();
    y = Bit();
    ng = FactorGraph(x,y);
    ng.addFactor(@xorDelta,x,y);

    fg = FactorGraph();
    b = BitStream();
    fg.addFactor(ng,5,b,b.getSlice(2));

    assertEqual(length(fg.Variables),6);
    assertEqual(length(fg.Factors),5);
    
    data = repmat([.4 .6],20,1);
    runMMtest(fg,b,data);

    %test specifying buffer size later
    %  test works when making bigger
    fg = FactorGraph();
    b = BitStream();
    fg.addFactor(ng,5,b,b.getSlice(2));

    fgs = fg.FactorGraphStreams{1};
    assertEqual(fgs.BufferSize,5);
    
    fgs.BufferSize = 10;

    assertEqual(length(fg.Variables),11);
    assertEqual(length(fg.Factors),10);

    data = repmat([.4 .6],20,1);    
    runMMtest(fg,b,data);    
    
    %Test changing buffer size on fly
    %  test works when making smaller (data not lost)
    fg = FactorGraph();
    b = BitStream();
    fg.addFactor(ng,10,b,b.getSlice(2));
    fgs = fg.FactorGraphStreams{1};
    
    fgs.BufferSize = 3;

    assertEqual(length(fg.Variables),4);
    assertEqual(length(fg.Factors),3);

    data = repmat([.4 .6],20,1);    
    
    runMMtest(fg,b,data);

    %Now test that we can do this *after* setting the data
    fg = FactorGraph();
    b = BitStream();
    fg.addFactor(ng,10,b,b.getSlice(2));
    
    dataSource = com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource(data);
    b.DataSource = dataSource;

    fgs = fg.FactorGraphStreams{1};
    
    fgs.BufferSize = 3;

    assertEqual(length(fg.Variables),4);
    assertEqual(length(fg.Factors),3);

    data = repmat([.4 .6],20,1);    

    runMMtest(fg,b,data,dataSource);
    
end
