if (1)
    load ('data/xword1.mat');
    raw_crossword=Apr0811;
    % "raw_crossword" is the crossword puzzle with answers.  Nonplayable
    % (i.e. "black") squares are marked "-1"; letters A-Z are labeled 0-25.
end
height=size(raw_crossword,1);
width=size(raw_crossword,2);

if (1)
    load ('digram_stats.mat');
end

% p is probability that we provide the correct answer to the factor graph
p=.9;

FG=FactorGraph();

domain=0:25;
squares=Variable(domain,height,width);


% horizontal digrams
for h=1:height
    for w=1:width-1
        if and( raw_crossword(h,w)>=0, raw_crossword(h,w+1)>=0)
            addFactor(FG, @digrams, squares(h,w), squares(h,w+1), digram_stats);
        end
    end
end

% vertical digrams
for h=1:height-1
    for w=1:width
        if and( raw_crossword(h,w)>=0, raw_crossword(h+1,w)>=0)
            addFactor(FG, @digrams, squares(h,w), squares(h+1,w), digram_stats);
        end
    end
end

% Add in some letters
unknown_squares=zeros(height,width);
for h=1:height
    for w=1:width
        if raw_crossword(h,w)>=0
            if rand<p
                correct=zeros(26,1);
                correct(raw_crossword(h,w)+1)=1;
                squares(h,w).Input=correct;
            else
                unknown_squares(h,w)=1;
            end
        end
    end
end

FG.Solver.setNumIterations(20);
FG.solve();
for h=1:height
    for w=1:width
        if unknown_squares(h,w)==1
            fprintf('[%2d,%2d]: %d  %d\n',h,w,raw_crossword(h,w),squares(h,w).Value);
        end
    end
end






