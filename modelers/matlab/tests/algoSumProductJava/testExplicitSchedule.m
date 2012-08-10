function testExplicitSchedule()
    %{
    OK, first we create a simple Factor Graph with a single xor connecting two 
    variables.
    %}

    fg = FactorGraph();
    b = Bit(2,1);
    f = fg.addFactor(@xorDelta,b);
    
    %Now we set an input message to the first bit
    b(1).Ports{1}.InputMsg = [.2 .8];
    
    %We can check that the belief uses that message
    assertElementsAlmostEqual(b(1).Belief,.8);
    
    %Now we set an input on the second variable and solve
    b(2).Input = .7;
    fg.solve();
    
    %And we can inspect the messages that were passed.
    assertElementsAlmostEqual(b(2).Ports{1}.InputMsg,[.5; .5]);
    assertElementsAlmostEqual(b(2).Ports{1}.OutputMsg,[.3; .7]);

    %Make sure initialize resets messages
    fg.initialize();
    assertElementsAlmostEqual(b(2).Ports{1}.OutputMsg,[.5; .5]);
    
    %Let's update a single node and make sure only one guy was updated.
    b(1).Input = .9;
    b(2).Input = .8;
    b(2).update();
    assertElementsAlmostEqual(b(1).Ports{1}.OutputMsg,[.5; .5]);
    assertElementsAlmostEqual(b(2).Ports{1}.OutputMsg,[.2; .8]);
    
    %or we can update both of them
    b.update();
    assertElementsAlmostEqual(b(1).Ports{1}.OutputMsg,[.1; .9]);
    assertElementsAlmostEqual(b(2).Ports{1}.OutputMsg,[.2; .8]);
    
    %Let's create a new graph for the next set of tests 
    fg = FactorGraph();
    b = Bit(3,1);
    f1 = fg.addFactor(@xorDelta,b(1),b(2));
    f2 = fg.addFactor(@xorDelta,b(1),b(3));
    
    %Let's update just one edge and make sure the other doesn't update.
    b(1).Input = .9;
    b(1).updateEdge(f1);
    assertElementsAlmostEqual(b(1).Ports{1}.OutputMsg,[.1; .9]);
    assertElementsAlmostEqual(b(1).Ports{2}.OutputMsg,[.5; .5]);
    
    %let's update the other edge with a different mechanism
    b(1).updateEdge(2);
    assertElementsAlmostEqual(b(1).Ports{2}.OutputMsg,[.1; .9]);

    %Let's re-initialize and use a final mechanism
    fg.initialize();
    b(1).updateEdge(b(1).getPortNum(f2));
    assertElementsAlmostEqual(b(1).Ports{2}.OutputMsg,[.1; .9]);
    assertElementsAlmostEqual(b(1).Ports{1}.OutputMsg,[.5; .5]);
    
    %Now let's try similar stuff with factors.

    %First we set things up
    fg.initialize();
    b(1).Input = .8;
    b(1).update();
    
    %Update one factor
    b(2).Ports{1}.OutputMsg = [.6; .4];
    f1.update()
    assertElementsAlmostEqual(f1.Ports{1}.OutputMsg,[.6; .4]);
    assertElementsAlmostEqual(f1.Ports{2}.OutputMsg,[.2; .8]);
    
    %Update a single edge of a factor
    fg.initialize();
    b(1).update();
    b(2).Ports{1}.OutputMsg = [.6; .4];
    f1.updateEdge(b(1));
    assertElementsAlmostEqual(f1.Ports{1}.OutputMsg,[.6; .4]);
    assertElementsAlmostEqual(f1.Ports{2}.OutputMsg,[.5; .5]);
    
    %Update an edge of a factor using 2nd mechanims
    fg.initialize();
    b(1).update();
    b(2).Ports{1}.OutputMsg = [.6; .4];
    f1.updateEdge(1);
    assertElementsAlmostEqual(f1.Ports{1}.OutputMsg,[.6; .4]);
    assertElementsAlmostEqual(f1.Ports{2}.OutputMsg,[.5; .5]);
    
    %Using third mechanism
    fg.initialize();
    b(1).update();
    b(2).Ports{1}.OutputMsg = [.6; .4];
    f1.updateEdge(f1.getPortNum(b(1)));
    assertElementsAlmostEqual(f1.Ports{1}.OutputMsg,[.6; .4]);
    assertElementsAlmostEqual(f1.Ports{2}.OutputMsg,[.5; .5]);
    

end
