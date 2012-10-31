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

    %Test the basic case
    fg = FactorGraph();
    N = 20;
    b = Bit(N,1);
    fac = @(x,y) x+y;

    factors = fg.addFactorVectorized(fac,b(1:end-1),b(2:end));

    for i = 1:(N-1)
       f = b(i).Factors{end};
       assertTrue(f==factors{i});
       next = f.Variables{end};
       assertTrue(next==b(i+1));
    end
    
    factors{1}.FactorTable.Weights = [3 4 5];
    for i = 2:(N-1)
       assertEqual(factors{i}.FactorTable.Weights,[3 4 5]'); 
    end

    %Test the case where we're adding factors on one dimension
    b = Bit(N,3);
    fg = FactorGraph();
    factors = fg.addFactorVectorized(@xorDelta,{b,1});

    for i = 1:N
        for j = 1:3
           assertTrue(b(i,j).Factors{1} == factors{i});
        end
    end
    
    %Test the other dimension
    b = Bit(3,N);
    fg = FactorGraph();
    factors = fg.addFactorVectorized(@xorDelta,{b,2});

    for i = 1:N
        for j = 1:3
           assertTrue(b(j,i).Factors{1} == factors{i});
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
    %fg = FactorGraph();
    %{
    b = Bit(3,1);
    nfg = FactorGraph(b);
    nfg.addFactor(@xorDelta,b);
    fg = FactorGraph();
    b = Bit(N,3);
    fg.addFactorVectorized(nfg,{b,1});
    %}
end

