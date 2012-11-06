%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testNames()
    fgRoot = makeSimpleThreeLevelGraphs();
    fgMid  = fgRoot.getGraphByName('Mid');
    
    %% getFactorByName, getFactorByUUID
    fRoot   = fgRoot.getFactorByName('fRoot');
    fRoot2  = fgRoot.getFactorByUUID(fRoot.UUID);
    assertEqual(fRoot.UUID, fRoot2.UUID);    
    fMid     = fgRoot.getFactorByName('Root.Mid.fMid');
    fMid2    = fgMid.getFactorByName('fMid');
    assertEqual(fMid.UUID, fMid2.UUID);
    fMid5 = fgMid.getFactorByUUID(fMid.UUID);
    assertEqual(fMid.UUID, fMid5.UUID );
    
    %% getGraphByName, getGraphByUUID
    fgLeaf  = fgRoot.getGraphByName('Root.Mid.Leaf');
    fgLeaf2 = fgMid.getGraphByName('Leaf');
    fgLeaf3 = fgMid.getGraphByUUID(fgLeaf2.UUID);    
    assertEqual(fgLeaf.UUID, fgLeaf2.UUID);
    assertEqual(fgLeaf.UUID, fgLeaf3.UUID);

    %% getVariableByName, getVariableByUUID
    vRootB0 = fgRoot.getVariableByName('vRootB_vv0');
    vRootB02 = fgRoot.getVariableByUUID(vRootB0.UUID);
    assertEqual(vRootB0.UUID, vRootB02.UUID);    
    vMidO1 =  fgRoot.getVariableByName('Root.Mid.vMidO_vv1');
    vMid012 = fgMid.getVariableByName('vMidO_vv1');
    assertEqual(vMidO1.UUID, vMid012.UUID);

    %% change names
    fMid.Name = 'fMidX';
    fMid_ = fgRoot.getFactorByName('Root.Mid.fMid');    
    assertEqual(fMid_, []);
    fMid_ = fgRoot.getFactorByName('Root.Mid.fMidX');
    assertEqual(fMid.UUID, fMid_.UUID);
    
    fgMid.Name = 'MidX';
    fgMid_ = fgRoot.getGraphByName('Root.Mid');    
    assertEqual(fgMid_, []);
    fgMid_ = fgRoot.getGraphByName('Root.MidX');
    assertEqual(fgMid.UUID, fgMid_.UUID);
    fgMid.Name = 'Mid';
    
    vMidO1.Name = 'vMidO_vv1X';
    vMidO1_ = fgRoot.getVariableByName('Root.Mid.vMidO_vv1');    
    assertEqual(vMidO1_, []);
    vMidO1_ = fgRoot.getVariableByName('Root.Mid.vMidO_vv1X');
    assertEqual(vMidO1.UUID, vMidO1_.UUID);
    vMidO1.Name = 'fMid';

    fgRoot.Name = '';
    assertTrue(size(fgRoot.Name, 2) == 0);
    assertTrue(size(fgRoot.ExplicitName, 2) == 0);
    fgRoot.Name = 'Root';
    
    %% calls shouldn't crash
    assertTrue(size(fgRoot.Name, 2) ~= 0);
    assertTrue(size(fgRoot.ExplicitName, 2) ~= 0);
    assertTrue(size(fgRoot.QualifiedName, 2) ~= 0);
    assertTrue(size(fgRoot.Label, 2) ~= 0);
    assertTrue(size(fgRoot.QualifiedLabel, 2) ~= 0);
    assertTrue(size(char(fgRoot.UUID),2) ~=0);
    assertEqual(fgRoot.getVariableByName('xxx'), []);
    assertEqual(fgRoot.getVariableByUUID(java.util.UUID.randomUUID()), []);
    assertEqual(fgRoot.getFactorByName('xxx'), []);
    assertEqual(fgRoot.getFactorByUUID(java.util.UUID.randomUUID()), []);
    assertEqual(fgRoot.getGraphByName('xxx'), []);
    assertEqual(fgRoot.getGraphByUUID(java.util.UUID.randomUUID()), []);
    
    assertTrue(size(fRoot.Name, 2) ~= 0);
    assertTrue(size(fRoot.ExplicitName, 2) ~= 0);
    assertTrue(size(fRoot.QualifiedName, 2) ~= 0);
    assertTrue(size(fRoot.Label, 2) ~= 0);
    assertTrue(size(fRoot.QualifiedLabel, 2) ~= 0);
    assertTrue(size(char(fRoot.UUID),2) ~=0);

    assertTrue(size(vRootB0.Name, 2) ~= 0);
    assertTrue(size(vRootB0.ExplicitName, 2) ~= 0);
    assertTrue(size(vRootB0.QualifiedName, 2) ~= 0);
    assertTrue(size(vRootB0.Label, 2) ~= 0);
    assertTrue(size(vRootB0.QualifiedLabel, 2) ~= 0);
    assertTrue(size(char(vRootB0.UUID),2) ~=0);

    
    %% now construct a graph by modifying child graphs;
    % verify that and top-down constructed graph give same result. 

    fgRoot = makeSimpleThreeLevelGraphs();
        
        
    % root
    vRootC  = Variable([0, 1], 1, 3);
    fgRootC = FactorGraph(vRootC(1));
    fRootC  = fgRootC.addFactor(@xorDelta, vRootC);

        
    % mid
    vMidC  = Variable([0, 1], 1, 3);

    fgMidC = FactorGraph(vMidC(1));
    % add mid
    fgMidC = fgRootC.addFactor(fgMidC, vRootC(3));
    % add factor to mid
    fMidC = fgMidC.addFactor(@xorDelta, vRootC(3), vMidC(2:3));

    % leaf
    vLeafC  = Variable([0, 1], 1, 3);

    fgLeafC = FactorGraph(vLeafC(1));
    % add leaf
    fgLeafC = fgMidC.addFactor(fgLeafC, vMidC(3));
    % add factor to leaf
    fLeafC = fgLeafC.addFactor(@xorDelta, vMidC(3), vLeafC(2:3));

    vRootC.setNames('vRootO');
    vRootC(1).Name = 'vRootB_vv0';
    vMidC.setNames('vMidO');
    vLeafC.setNames('vLeafO');

    fgRootC.Name = 'Root';
    fgMidC.Name  = 'Mid';
    fgLeafC.Name = 'Leaf';

    fRootC.Name  = 'fRoot';
    fMidC.Name   = 'fMid';
    fLeafC.Name  = 'fLeaf';
        
    diffs = fgRoot.getFactorGraphDiffsByName(fgRootC);
    if ~diffs.noDiffs()
        disp(fgRoot.getAdjacencyString());
        disp(fgRootC.getAdjacencyString());
        disp(diffs);
    end
    assertTrue(diffs.noDiffs());
    
    % random input
    vsRoot = fgRoot.Variables;
    vsRootC = fgRootC.Variables;
    assertEqual(size(vsRoot), size(vsRootC));

    for x = 1:size(vsRoot)
        r = rand();
        row = [r 1-r];

        v = vsRoot{x};
        v.Input = row;
        
        vC = fgRootC.getVariableByName(v.QualifiedName);
        vC.Input = row;
    end
    
    fgRoot.Solver.setNumIterations(20);
    fgRootC.Solver.setNumIterations(20);
    
    fgRoot.solve();
    fgRootC.solve();
    for x = 1:size(vsRoot)
        v = vsRoot{x};
        vC = fgRootC.getVariableByName(v.QualifiedName);
        assertElementsAlmostEqual(v.Belief,vC.Belief);
    end
end
