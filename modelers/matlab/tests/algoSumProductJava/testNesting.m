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

function testNesting()
    b = Bit(4,1);
    for i = 1:4
        b(i).Name = sprintf('bbottom%d',i);
    end
    
    fg4bitXor = FactorGraph(b);
    %TODO: why can't I name this?
    %fg4bitXor.Name = '4BitXor';

    iv = Bit();
    iv.Name = 'ivbottom';
    
    f1 = fg4bitXor.addFactor(@xorDelta,b(1),b(2),iv);
    f2 = fg4bitXor.addFactor(@xorDelta,b(3),b(4),iv);
    f1.Name = 'f1bottom';
    f2.Name = 'f2bottom';
    
    b = Bit(6,1);
    for i = 1:6
       b(i).Name = sprintf('bmid%d',i); 
    end
    
    fg6bitXor = FactorGraph(b);
    %fg6bitXor.Name = '6BitXor';
    
    iv = Bit();
    iv.Name = 'ivmid';

    f1 = fg6bitXor.addFactor(fg4bitXor,b(1),b(2),b(3),iv);
    f2 = fg6bitXor.addFactor(fg4bitXor,b(4),b(5),b(6),iv);
    f3 = fg6bitXor.addFactor(@xorDelta,b(1),b(4));
    f1.Name = 'f1mid';
    f2.Name = 'f2mid';
    f3.Name = 'f3mid';
    
    b = Bit(10,1);
    for i = 1:10
        b(i).Name = sprintf('btop%d',i);
    end
    fg = FactorGraph();
    fg.Name = '10bitXor';
    
    iv = Bit();
    iv.Name = 'ivtop';
    f1 = fg.addFactor(fg6bitXor,b(1),b(2),b(3),b(4),b(5),iv);
    f2 = fg.addFactor(fg6bitXor,b(6),b(7),b(8),b(9),b(10),iv);
    f3 = fg.addFactor(@xorDelta,b(1),b(6));
    
    f1.Name = 'f1top';
    f2.Name = 'f2top';
    f3.Name = 'f3top';
    
    
    
    %Test ability to retrieve nested graphs.
    ngs = fg.NestedGraphs;
    assertTrue(ngs{1} == f1);
    assertFalse(ngs{1} == f2);
    assertTrue(ngs{2} == f2);
    assertFalse(ngs{2} == f1);
    
    
    %Test ability to retrieve FactorsTop()
    
    %In root graph
    assertEqual(length(fg.FactorsTop),3);
    assertTrue(fg.FactorsTop{1}==f1);
    assertTrue(fg.FactorsTop{2}==f2);
    assertTrue(fg.FactorsTop{3}==f3);
    
    
    %In graph at mid level
    assertEqual(length(f1.FactorsTop),3);
    assertEqual(f1.FactorsTop{1}.Name,'f1mid');
    assertEqual(f1.FactorsTop{2}.Name,'f2mid');
    assertEqual(f1.FactorsTop{3}.Name,'f3mid');

    %In graph at low level
    assertEqual(length(f1.FactorsTop{1}.FactorsTop),2);
    assertEqual(f1.FactorsTop{1}.FactorsTop{1}.Name,'f1bottom');
    assertEqual(f1.FactorsTop{1}.FactorsTop{2}.Name,'f2bottom');
    
    %Test ability to retrieve Factors
    
    %At top level
    expectedNames = {'f1bottom','f2bottom','f1bottom','f2bottom','f3mid',...
        'f1bottom','f2bottom','f1bottom','f2bottom','f3mid','f3top'};
    assertEqual(length(fg.Factors),length(expectedNames));
    for i = 1:length(fg.Factors)
       assertEqual(fg.Factors{i}.Name,expectedNames{i}); 
    end
    
    %Test ability to retrieve FactorsFlat
    assertEqual(length(fg.FactorsFlat),length(expectedNames));

    
    %at mid level
    expectedNames = {'f1bottom','f2bottom','f1bottom','f2bottom','f3mid'};
    assertEqual(length(fg.FactorsTop{1}.Factors),length(expectedNames));
    for i = 1:length(fg.FactorsTop{1}.Factors)
       assertEqual(fg.FactorsTop{1}.Factors{i}.Name,expectedNames{i}); 
    end
    
    %at low level
    expectedNames = {'f1bottom','f2bottom'};
    assertEqual(length(fg.FactorsTop{1}.FactorsTop{1}.Factors),length(expectedNames));
    for i = 1:length(expectedNames)
        assertEqual(fg.FactorsTop{1}.FactorsTop{1}.Factors{i}.Name,expectedNames{i});
    end
    
    
    %Test ability to get factors at arbitrary level
    
    %from top
    factors = fg.getFactors(1);
    expectedNames = {'f1mid','f2mid','f3mid','f1mid','f2mid','f3mid','f3top'};
    assertEqual(length(factors),length(expectedNames));
    for i = 1:length(factors)
        assertEqual(factors{i}.Name,expectedNames{i});
    end
    
    %from middle
    expectedNames = {'f1bottom','f2bottom','f1bottom','f2bottom','f3mid'};
    factors = fg.getFactors(0);
    factors = factors{1}.getFactors(1);
    assertEqual(length(factors),length(expectedNames));
    for i = 1:length(factors)
        assertEqual(factors{i}.Name,expectedNames{i});
    end
        
    
    %Test ability to retrieve VariablesTop,Flat, Somewhere inbetween
    
    %VariablesTop
    %btop1-10 and ivtop
    
    %top level
    expectedNames = {};
    for i = 1:10
        expectedNames{i} = b(i).Name;
    end
    expectedNames = [expectedNames(1:5) 'ivtop' expectedNames(6:10)];
    assertEqual(length(expectedNames),length(fg.VariablesTop));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},fg.VariablesTop{i}.Name);
    end
    
    %mid level
    assertEqual(length(fg.NestedGraphs{1}.VariablesTop),1);
    assertEqual(fg.NestedGraphs{1}.VariablesTop{1}.Name,'ivmid');
    
    %bottom level
    assertEqual(length(fg.NestedGraphs{1}.NestedGraphs{1}.VariablesTop),1);
    assertEqual(fg.NestedGraphs{1}.NestedGraphs{1}.VariablesTop{1}.Name,'ivbottom');
        
    %Variables
    
    %top level
    expectedNames = [expectedNames {'ivmid', 'ivbottom', 'ivbottom', 'ivmid', 'ivbottom', 'ivbottom'}];
    vars = fg.Variables;
    assertEqual(length(expectedNames),length(vars));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},vars{i}.Name);
    end
    
    %Variables Flat
    assertEqual(length(expectedNames),length(fg.VariablesFlat));

    %mid level
    expectedNames = {'ivmid', 'ivbottom', 'ivbottom'};
    vars = fg.NestedGraphs{1}.Variables;    
    assertEqual(length(expectedNames),length(vars));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},vars{i}.Name);
    end
    
    %low level
    vars = fg.NestedGraphs{1}.NestedGraphs{1}.Variables;
    expectedNames = {'ivbottom'};
    assertEqual(length(expectedNames),length(vars));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},vars{i}.Name);
    end
        
    %Variables Middle
    
    %middle level
    vars = fg.getVariables(1);
    expectedNames = {};
    for i = 1:10
        expectedNames{i} = b(i).Name;
    end
    expectedNames = [expectedNames(1:5) 'ivtop' expectedNames(6:10)];
    expectedNames = [expectedNames, 'ivmid', 'ivmid'];
    assertEqual(length(expectedNames),length(vars));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},vars{i}.Name);
    end
    
    %bottom level
    vars = fg.NestedGraphs{1}.getVariables(1);
    expectedNames = {'ivmid','ivbottom','ivbottom'};
    assertEqual(length(expectedNames),length(vars));
    for i = 1:length(expectedNames)
        assertEqual(expectedNames{i},vars{i}.Name);
    end
    
        
    
    %NonGraphFactorsTop
    assertEqual(length(fg.NonGraphFactorsTop),1);
    assertEqual(fg.NonGraphFactorsTop{1}.Name,'f3top');
    
    assertEqual(length(fg.NestedGraphs{1}.NonGraphFactorsTop),1);
    assertEqual(fg.NestedGraphs{1}.NonGraphFactorsTop{1}.Name,'f3mid');

    assertEqual(length(fg.NestedGraphs{1}.NestedGraphs{1}.NonGraphFactorsTop),2);
    assertEqual(fg.NestedGraphs{1}.NestedGraphs{1}.NonGraphFactorsTop{1}.Name,'f1bottom');
    assertEqual(fg.NestedGraphs{1}.NestedGraphs{1}.NonGraphFactorsTop{2}.Name,'f2bottom');
    
    %NonGraphFactors
    assertEqual(length(fg.NonGraphFactors),(2*2+1)*2+1);
    
    %NonGraphFactorsFlat
    assertEqual(length(fg.NonGraphFactorsFlat),(2*2+1)*2+1);
    
    %NonGraphFactors in between
    factors = fg.getNonGraphFactors(1);
    assertEqual(length(factors),3);
    expectedNames = {'f3mid','f3mid','f3top'};
    for i = 1:length(expectedNames)
       assertEqual(expectedNames{i},factors{i}.Name); 
    end
    
    %at lower level
    factors = fg.NestedGraphs{1}.getNonGraphFactors(1);
    assertEqual(length(factors),5);
    expectedNames = {'f1bottom','f2bottom','f1bottom','f2bottom','f3mid'};
    for i = 1:length(expectedNames)
       assertEqual(expectedNames{i},factors{i}.Name); 
    end
    
    
    %Test Variable's ability to get Factors Top, Flat, somwehre inbetween
    %Test FactorsTop
    factors = b(1).FactorsTop;
    expectedNames = {'f1top','f1top','f3top'};
    assertEqual(length(factors),length(expectedNames));
    for i = 1:length(expectedNames)
       assertEqual(expectedNames{i},factors{i}.Name); 
    end
    
    factors = fg.NestedGraphs{1}.VariablesTop{1}.FactorsTop;
    expectedNames = {'f1mid','f2mid'};
    assertEqual(length(factors),length(expectedNames));
    for i = 1:length(expectedNames)
       assertEqual(expectedNames{i},factors{i}.Name); 
    end

    %Test Factors
    
    %Top level
    expectedNames = {'f1bottom','f3mid','f3top'};
    factors = b(1).Factors;
    assertEqual(length(factors),length(expectedNames));
    for i = 1:length(expectedNames)
       assertEqual(expectedNames{i},factors{i}.Name); 
    end
    
    %mid level
    factors = fg.NestedGraphs{1}.VariablesTop{1}.Factors;
    expectedNames = {'f2bottom','f2bottom'};
    assertEqual(length(factors),length(expectedNames));
    for i = 1:length(expectedNames)
       assertEqual(expectedNames{i},factors{i}.Name); 
    end
    
    %Test FactorsFlat
    assertEqual(3,length(b(1).FactorsFlat));
    
    %Test Factor somewhere inbetween
    factors = b(1).getFactors(1);
    expectedNames = {'f1mid','f3mid','f3top'};
    assertEqual(length(factors),length(expectedNames));
    for i = 1:length(expectedNames)
       assertEqual(expectedNames{i},factors{i}.Name); 
    end
    
    
end
    
