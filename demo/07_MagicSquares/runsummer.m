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

clc;

N = 4;
maxVal = N^2-1;
numBits = ceil(log2(maxVal));
sumToVal = 30;
sumToValNumBits = ceil(log2(sumToVal));

%createSumGraph(N,numBits,addToBits)
s = createSumGraph(N,numBits,sumToValNumBits);


g = newGraph();

b = addBit(g,N,numBits);
answer = addBit(g,sumToValNumBits);

addGraph(g,s,b,answer);

%solvefor_ind = randint(1,1,N);
solvefor_ind = 10;
solvefor_ind

m = magic(N);
for i = 1:N
    if i ~= solvefor_ind
        num = m(i)-1;
        vec = num2vec(num,numBits);
        setBitInput(b(i,:),vec);
    end
end

setNumIterations(g,100);
solve(g);

beliefs = getBelief(b);
results = zeros(N,1);
for i = 1:N
    results(i) = vec2num(beliefs(i,:,1) > .5);
end
