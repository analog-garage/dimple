%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function valid = CheckSolution(vars,solution)
    valid = 1;
    for i = 1:9
        for j = 1:9
            value = vars(i,j).Value;
            if value ~= solution(i,j)
                valid = 0;
                break;
            end
        end
        if ~valid
            break;
        end
    end
end
