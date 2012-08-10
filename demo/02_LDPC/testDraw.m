%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2011, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

num = 20;


b = Bit(num,num);
b.Input = ones(num,num)*.8;
vals = ones(num,num);
vals(1,1) = 0;
vals(3,3) = 0;
tic
f = drawLDPC(b,vals);
set(f,'Position',[10 10 700 700])
toc
