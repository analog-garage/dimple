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


end

