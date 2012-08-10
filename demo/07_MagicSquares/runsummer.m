%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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
