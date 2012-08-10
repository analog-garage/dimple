%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function num = vec2num(vec,twoscomplement)
    if nargin < 2
        twoscomplement = 0;
    end
    
    if ~twoscomplement
        num = 0;
        for i =1:length(vec)
           tmp = double(vec(i) > .5);
           num = num+bitshift(tmp,i-1);
        end
    else
        sn = vec(end);
        if (sn > 0)
            vec = ~vec;
        end
        
        num = 0;
        for i =1:(length(vec)-1);
           tmp = double(vec(i) > .5);
           num = num+bitshift(tmp,i-1);
        end        
        if sn > 0
           num = -num -1; 
        end
    end
end
