%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

function testPlotting()

    fg = FactorGraph();
    b = Bit(6,1);
    for i = 1:6
        b(i).Name = sprintf('b%d',i);
    end
    f1 = fg.addFactor(@xorDelta,b(1:4));
    f1.Name = 'f1';
    f2 = fg.addFactor(@xorDelta,b(4:6));
    f2.Name = 'f2';

    numVars = length(fg.Variables);
    numFactors = length(fg.Factors);
    numNodes = numVars + numFactors;

    %test no args
    [x,y] = fg.plot('test');

    assertEqual(length(x),numNodes);
    assertEqual(length(y),numNodes);

    %test depth
    [x,y] = fg.plot('test','depth',b(1),0);
    assertEqual(length(x),1);
    [x,y] = fg.plot('test','depth',b(1),1);
    assertEqual(length(x),2);
    [x,y] = fg.plot('test','depth',b(1),2);
    assertEqual(length(x),5);

    %test nodes
    [x,y] = fg.plot('test','nodes',{b(1),b(2),f1});
    assertEqual(length(x),3);

    %test not nodes and depth together
    founderror = false;
    msg = '';
    try
        fg.plot('nodes',{b(1),b(2),f1},'depth',b(1),2);
    catch E
        founderror = true;
        msg = E.message;
    end
    assertTrue(founderror);
    assertEqual(msg,'Cannot specify both depth and nodes');

    %test labels
    fg.plot('test','labels',1);

    %test color (cell array, pair, default)
    fg.plot('test','color',f1,'r','color',b(1),'g','color','b','color',{b(2),b(3)},{'r','r'});

    %test no duplicate
    founderror = false;
    msg = '';
    try
        fg.plot('color',f1,'b','color',f1,'b');
    catch E
        founderror = true;
        msg = E.message;
    end
    assertTrue(founderror);
    assertEqual(msg,'color already defined for node');

    founderror = false;
    msg = '';
    try
        fg.plot('color',{f1,f1},{'b','b'});
    catch E
        founderror = true;
        msg = E.message;
    end
    assertTrue(founderror);
    assertEqual(msg,'color already defined for node');
    
    %TODO: look at graph with layers on
    b = Bit(2,1);
    iv = Bit();
    leafg = FactorGraph(b);
    leafg.addFactor(@xorDelta,b(1),iv);
    leafg.addFactor(@xorDelta,b(2),iv);
    
    b = Bit(2,1);
    iv = Bit();
    midg = FactorGraph(b);
    midg.addFactor(leafg,b(1),iv);
    midg.addFactor(leafg,b(2),iv);
    
    %midg.plot('labels',true,'nesting',0);
    
    b = Bit(2,1);
    iv = Bit();
    topg = FactorGraph(b);
    topg.addFactor(midg,b(1),iv);
    topg.addFactor(midg,b(2),iv);
    
        
    [x,y] = topg.plot('labels',true,'nesting',0,'test');
    assertEqual(length(x),5);
    
    [x,y] = topg.plot('labels',true,'nesting',1,'test');
    assertEqual(length(x),9);
 
    [x,y] = topg.plot('labels',true,'nesting',2,'test');
    assertEqual(length(x),17);

    [x,y] = topg.plot('labels',true,'nesting',3,'test');
    assertEqual(length(x),17);

    [x,y] = topg.NestedGraphs{1}.NestedGraphs{1}.plot('labels',true,'test');
    assertEqual(length(x),5);

    [x,y] = topg.NestedGraphs{1}.plot('labels',true,'test');
    assertEqual(length(x),9);
    
    [x,y] = topg.NestedGraphs{1}.plot('labels',true,'test','nesting',0);
    assertEqual(length(x),5);
    
    %layers with breadth first search
    [x,y] = topg.plot('depth',iv,10,'nesting',0,'test');
    assertEqual(length(x),5);

    [x,y] = topg.plot('depth',iv,10,'nesting',1,'test');
    assertEqual(length(x),9);

    [x,y] = topg.plot('depth',iv,10,'nesting',2,'test');
    assertEqual(length(x),17);

    %Test boundary variables have different  shape?    
    %Test FactorGraphs have different  shape?
    %topg.NestedGraphs{1}.plot('nesting',3,'labels',0);
    
end

