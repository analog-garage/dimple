%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function valid = domainDelta(x)
    i = sqrt(-1);
    domain = {[1+i 0+2*i],[2 1+-5*i],[2 2]};
    valid = isequal(x,domain{1}) || isequal(x,domain{2});
end
