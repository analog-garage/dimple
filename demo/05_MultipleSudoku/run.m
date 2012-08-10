figure(gcf);

s = Sudoku();
s.setNumIterations(25);
itr = 3;
for idx = 1:itr
    %easy
    puzzle = Ps(.4);
    %medium
    %puzzle = Ps(.55);
    s.initPuzzle(puzzle);
    r = s.solve();
    if s.isValid()
        %s.setNumIterations(25);
        s.showSolve(25);
    end
end
