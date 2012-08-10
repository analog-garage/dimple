function [fgRoot, fgMid, fgLeaf] = makeSimpleThreeLevelGraphs()
    vRoot = Variable([0, 1], 1, 3);
    vMid = Variable([0, 1], 1, 3);
    vLeaf = Variable([0, 1], 1, 3);
    
    vRoot.setNames('vRootO');
    vMid.setNames('vMidO');
    vLeaf.setNames('vLeafO');
    
    vRoot(1).Name = 'vRootB_vv0';
    vMid(1).Name  = 'vMidB_vv0';
    vLeaf(1).Name = 'vLeafB_vv0';
    
    fgRoot = FactorGraph(vRoot(1));
    fgMid  = FactorGraph(vMid(1));
    fgLeaf = FactorGraph(vLeaf(1));
    
    fgRoot.Name = 'Root';
    fgMid.Name  = 'Mid';
    fgLeaf.Name = 'Leaf';
    
    fRoot = fgRoot.addFactor(@xorDelta, vRoot);
    fMid  = fgMid.addFactor(@xorDelta, vMid);
    fLeaf = fgLeaf.addFactor(@xorDelta, vLeaf);
    
    fRoot.Name = 'fRoot';
    fMid.Name  = 'fMid';
    fLeaf.Name = 'fLeaf';
    
    fgMid.addFactor(fgLeaf, vMid(3));

    fgRoot.addFactor(fgMid, vRoot(3));
end
