%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

clc;

N = 4;

%%%%%%%%%%%%%%
%Create Graph
%%%%%%%%%%%%%%

[msGraph,b] = createMagicSquaresGraph(N);


%%%%%%%%%%%%%%
%Set Input
%%%%%%%%%%%%%%
priors = rand(N,N,4);
for i = 1:N
    for j = 1:N
        for k = 1:4
            setBitInput(b(i,j,k),rand);
        end
    end
end

%%%%%%%%%%%%
%Solve
%%%%%%%%%%%%
setNumIterations(msGraph,100);

tic
solve(msGraph);
toc
beliefs = getBelief(b);

guess = zeros(N,N);
for i = 1:N
    for j = 1:N
        vec = getBelief(b(i,j,:));
        num = vec2num(vec);
        %num = softbyte2gauss(vec);
        guess(i,j) = num;
    end
end

guess

sum(guess,1)
sum(guess,2)
sum(diag(guess))
sum(diag(fliplr(guess)))
