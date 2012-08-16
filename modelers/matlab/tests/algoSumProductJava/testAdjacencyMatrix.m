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

function [A,labels] = getAdjacencyMatrix(obj,nodes)
    b = Bit(2,1);

    fg1 = FactorGraph(b);
    ib = Bit(2,1);
    ib(1).Name = 'ib1';
    ib(2).Name = 'ib2';
    f1 = fg1.addFactor(@xorDelta,b(1),ib(1),ib(2));
    f2 = fg1.addFactor(@xorDelta,b(2),ib(1),ib(2));
    f1.Name = 'f1';
    f2.Name = 'f2';

    b = Bit(2,1);
    b(1).Name = 'b1';
    b(2).Name = 'b2';
    fg2 = FactorGraph(b);
    f = fg2.addFactor(fg1,b);
    f.Name = 'fg1';

    %Test getAdjacencyMatrix(nodes)
    [A,labels] = fg2.getAdjacencyMatrix({b(1),b(2),f});
    expectedA = zeros(3);
    expectedA(1,3) = 1;
    expectedA(3,1) = 1;
    expectedA(2,3) = 1;
    expectedA(3,2) = 1;
    expectedLabels = {'b1','b2','fg1'};

    assertEqual(A,expectedA);
    assertEqual(labels,expectedLabels');

    %Test top
    [A,labels] = fg2.getAdjacencyMatrixTop();
    assertEqual(A,expectedA);
    assertEqual(labels,expectedLabels');



    [A,labels] = fg2.getAdjacencyMatrix({b(1),b(2),...
        fg2.NestedGraphs{1}.Variables{1},fg2.NestedGraphs{1}.Variables{2}...
        fg2.Factors{1},fg2.Factors{2},...
        });

    expectedA = zeros(6);
    expectedA(1,5) = 1;
    expectedA(2,6) = 1;
    expectedA(3,5) = 1;
    expectedA(3,6) = 1;
    expectedA(4,5) = 1;
    expectedA(4,6) = 1;
    expectedA = expectedA' + expectedA;

    assertEqual(expectedA,A);

    expectedLabels = {'b1','b2','ib1','ib2','f1','f2'};
    assertEqual(labels,expectedLabels');

    %test getAdjacencyMatrixFlat()
    [A,labels] = fg2.getAdjacencyMatrixFlat();
    expectedLabels = {'b1','b2','ib1','ib2','f1','f2'};
    assertEqual(labels,expectedLabels');

    %test getAdjacencyMatrix(nestingLevel) We need a three level graph for this
    b = Bit();
    fgbottom = FactorGraph(b);
    fbottom = fgbottom.addFactor(@xorDelta,b);
    fbottom.Name = 'fbottom';

    b = Bit();
    fgmiddle = FactorGraph(b);
    fmiddle = fgmiddle.addFactor(fgbottom,b);
    fmiddle.Name = 'fmiddle';

    b = Bit();
    b.Name = 'b';
    fg = FactorGraph(b);
    ftop = fg.addFactor(fgmiddle,b);
    ftop.Name = 'ftop';

    [A,labels] = fg.getAdjacencyMatrix(1);
    expectedA = fliplr(eye(2));
    assertEqual(expectedA,A);
    expectedLabels = {'b','fmiddle'}';
    assertEqual(expectedLabels,labels);


    %test getAdjacency Matrix a level down
    [A,labels] = fg.NestedGraphs{1}.getAdjacencyMatrix();
    expectedA = fliplr(eye(2));
    expectedLabels = {'b','fbottom'};
    assertEqual(A,expectedA);
    assertEqual(labels,expectedLabels');
end
