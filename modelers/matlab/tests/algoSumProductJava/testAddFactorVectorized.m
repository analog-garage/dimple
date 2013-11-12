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

function testAddFactorVectorized()
    %Test return value for degenerate case
    b = Bit(2,1);
    fg = FactorGraph();
    f = fg.addFactorVectorized(@xorDelta,b(1),b(2));
    assertTrue(f==fg.Factors{1});

    %Test constants work
    b = Bit(10,1);
    fg = FactorGraph();
    fg.addFactorVectorized(@xorDelta,b,1);
    fg.solve();
    assertElementsAlmostEqual(b.Belief,ones(10,1));

    %Test mismatched vector sizes;
    a = Bit(9,1);
    b = Bit(10,1);
    fg = FactorGraph();
    message = '';
    try
        f = fg.addFactorVectorized(@xorDelta,a,b);
    catch e
        message = e.message;
    end
    assertEqual(message,'mismatch of matrix dimensions');

    %Test single var against multiple vars
    a = Bit();
    b = Bit(10,1);
    fg = FactorGraph();
    fs = fg.addFactorVectorized(@xorDelta,a,b);
    for i = 1:length(fs)
       assertTrue(fs(i) == b(i).Factors{1});
       assertTrue(fs(i) == a.Factors{i});
    end


    %test with factor table
    indices = [0 1; 1 0];
    weights = [1 2];
    b = Bit(10,2);
    fg = FactorGraph();
    f = fg.addFactorVectorized(indices,weights,{b,1});
    fg.solve();
    for i = 1:10
       assertTrue(b(i,1).Factors{1} == f(i));
       assertTrue(b(i,2).Factors{1} == f(i));
    end

    %Test the basic case
    fg = FactorGraph();
    N = 20;
    b = Bit(N,1);
    fac = @(x,y) x+y;

    factors = fg.addFactorVectorized(fac,b(1:end-1),b(2:end));

    for i = 1:(N-1)
       f = b(i).Factors{end};
       assertTrue(f==factors(i));
       next = f.Variables{end};
       assertTrue(next==b(i+1));
    end
    
    factors(1).FactorTable.Weights = [3 4 5];
    for i = 2:(N-1)
       assertEqual(factors(i).FactorTable.Weights,[3 4 5]'); 
    end

    %Test the case where we're adding factors on one dimension
    b = Bit(N,3);
    fg = FactorGraph();
    factors = fg.addFactorVectorized(@xorDelta,{b,1});

    for i = 1:N
        for j = 1:3
           assertTrue(b(i,j).Factors{1} == factors(i));
        end
    end
    
    %Test the other dimension
    b = Bit(3,N);
    fg = FactorGraph();
    factors = fg.addFactorVectorized(@xorDelta,{b,2});

    for i = 1:N
        for j = 1:3
           assertTrue(b(j,i).Factors{1} == factors(i));
        end
    end
    
    %Test degenerate case
    b = Bit(3,1);
    fg = FactorGraph();
    fg.addFactorVectorized(@xorDelta,b(1),b(2),b(3));
    assertEqual(length(fg.Factors),1);
    
    %Test another way of expressing that
    b = Bit(3,1);
    fg = FactorGraph();
    fg.addFactorVectorized(@xorDelta,{b,[]});
    assertEqual(length(fg.Factors),1);
    
    %Test real variables
    fg = FactorGraph();
    fg.Solver = 'gaussian';
    a = Real(N,1);
    b = Real(N,1);
    factors = fg.addFactorVectorized(@add,a,b);
    for i = 1:N
       assertTrue(a(i).Factors{1}==factors(i));
       assertTrue(b(i).Factors{1}==factors(i));
    end
    
    %Test multiple dimensions
    fg = FactorGraph();
    b = Bit(3,4,5);
    fs = fg.addFactorVectorized(@xorDelta,{b,[1 3]});
    for i = 1:5
        for j = 1:3
            f = fs(j,i);
            for k = 1:4
                f2 = b(j,k,i).Factors{1};
                assertTrue(f==f2);
            end
        end
    end
    
    %Test multiple dimensions not vectorized
    fg = FactorGraph();
    myfac = @(a) sum(a(:));
    b = Bit(2,2,2,2);
    fg = FactorGraph();
    fs = fg.addFactorVectorized(myfac,{b,[1 3]});
    for i = 1:2
        for j = 1:2
            f = fs(j,i);
            for m = 1:2
                for n = 1:2
                    f2 = b(j,m,i,n).Factors{1};
                    assertTrue(f==f2);
                end            
            end
        end
    end
    
    
    %Test nested graphs
    b = Bit(3,1);
    nfg = FactorGraph(b);
    nfg.addFactor(@xorDelta,b);
    fg = FactorGraph();
    b = Bit(N,3);
    graphs = fg.addFactorVectorized(nfg,{b,1});
    for i = 1:N
        for j = 1:3
           assertTrue(b(i,j).Factors{1} == graphs(i).Factors{1});
        end
    end
 
    %Test fix for bug 60
    fg=FactorGraph;
    a=Bit(3,1);
    b=Bit(3,4);
    f=fg.addFactorVectorized('Xor', a, {b,1});
    ainput = rand();
    binput = rand(1,4);
    a.Input = repmat(ainput,3,1);
    b.Input = repmat(binput,3,1);
    fg.solve();
    fg2 = FactorGraph;
    a2 = Bit();
    b2 = Bit(1,4);
    fg2.addFactor('Xor',a2,b2);
    a2.Input = ainput;
    b2.Input = binput;
    fg2.solve();
    
    for i = 1:3
       assertElementsAlmostEqual(a(i).Belief,a2.Belief);
       assertElementsAlmostEqual(b(i,:).Belief,b2.Belief);
    end
    
    fg=FactorGraph;
    a=Bit(1,3);
    b=Bit(4,3);
    f=fg.addFactorVectorized('Xor', a, {b,2});
    ainput = rand();
    binput = rand(4,1);
    a.Input = repmat(ainput,1,3);
    b.Input = repmat(binput,1,3);
    fg.solve();
    fg2 = FactorGraph;
    a2 = Bit();
    b2 = Bit(4,1);
    fg2.addFactor('Xor',a2,b2);
    a2.Input = ainput;
    b2.Input = binput;
    fg2.solve();
    
    for i = 1:3
       assertElementsAlmostEqual(a(i).Belief,a2.Belief);
       assertElementsAlmostEqual(b(:,i).Belief,b2.Belief);
    end
    
    
end

