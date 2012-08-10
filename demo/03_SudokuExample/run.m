%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Create input for puzzle
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

puzzle = [4 0 0 0 0 8 3 0 5; ...
          5 6 8 0 0 0 0 0 0; ...
          7 0 0 5 0 0 9 0 4; ...
          0 0 5 0 1 2 6 0 0; ...
          8 0 0 0 5 0 0 0 3; ...
          0 0 1 8 4 0 2 0 0; ...
          2 0 4 0 0 5 0 0 6; ...
          0 0 0 0 0 0 8 4 1; ...
          1 0 7 6 0 0 0 0 9];
    
solution = [
4 1 9 2 7 8 3 6 5; ...
5 6 8 4 3 9 1 7 2; ...
7 2 3 5 6 1 9 8 4; ...
3 4 5 7 1 2 6 9 8; ...
8 7 2 9 5 6 4 1 3; ...
6 9 1 8 4 3 2 5 7; ...
2 8 4 1 9 5 7 3 6; ...
9 5 6 3 2 7 8 4 1; ...
1 3 7 6 8 4 5 2 9];
      


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Build Sudoku Factor Graph
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

perm = BuildPermutationFactorGraph(9);

vars = Variable(1:9,9,9);

vars4D = reshape(vars,3,3,3,3);

fg = FactorGraph();

for i = 1:3
    for j = 1:3
        %rows
        fg.addFactor(perm,reshape(vars4D(:,:,i,j),9,1));
        %columns
        fg.addFactor(perm,reshape(vars4D(i,j,:,:),9,1));
        %boxes
        fg.addFactor(perm,reshape(vars4D(:,i,:,j),9,1));
    end
end


   
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Run Algorithm
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%Set priors

for row = 1:9
  for col = 1:9
    if (puzzle(row,col) ~= 0 )
      ps = zeros(1,9);
      ps(puzzle(row,col)) = 1;
      vars(row,col).Input = ps;
    end
  end
end


%Now let's iterate and, for each iteration, check if we have the right
%answer.
drawSudoku(vars);
figure(gcf);
fg.initialize();


for i = 1:25
    disp(['iteration: ' num2str(i)]);
    
    fg.Solver.iterate(1);
    
    drawSudoku(vars);
    valid = CheckSolution(vars,solution);
    if valid
        break
    end
    pause(.01);
end
drawSudoku(vars);

valid
