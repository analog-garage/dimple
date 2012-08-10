%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

N = 4;
numBits = 4;

%%%%%%%%%%%%%%
%Create Graph
%%%%%%%%%%%%%%

[msGraph,b] = createMagicSquaresGraph(N);


%%%%%%%%%%%%%%
%Set Input
%%%%%%%%%%%%%%
priorMask =    [0 1 1 1; ...
                1 1 1 0; ...
                0 1 0 1; ...
                0 0 1 0];
            

m = magic(N);
answer = zeros(N,N,numBits);
for i = 1:N
    for j = 1:N
        if priorMask(i,j)
            b(i,j,:).Input = num2vec(m(i,j)-1,numBits);
        end
        answer(i,j,:) = num2vec(m(i,j)-1,numBits);
    end
end

drawMagicSquare(answer,2,'Target Magic Square');


%%%%%%%%%%%%
%Solve
%%%%%%%%%%%%
doplot = 1;
numIters = 50;
if doplot
    for i = 1:numIters
        disp(['num iterations: ' num2str(i)]);
        beliefs = b.Belief;
        b_tmp = beliefs(:,:,:,1);
        drawMagicSquare(b_tmp,1,'Guess Magic Square');
        drawnow();
        msGraph.Solver.iterate();
    end
else
    msGraph.Solver.setNumIterations(numIters);
    msGraph.solve();
end

beliefs = b.Belief;

guess = zeros(N,N);
for i = 1:N
    for j = 1:N
        vec = b(i,j,:).Belief;
        %vec = vec(1,1,:,1);
        num = vec2num(vec);
        guess(i,j) = num;
    end
end

disp(guess);
errors = sum(sum(guess ~= magic(N)-1));
disp(['Num errors: ' num2str(errors)]);
