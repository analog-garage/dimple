%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function H = blockVandermonde()
    % Generate an LDPC code with a block Vandermonde-like structure.

    blocksize=31;
    blockrows=4;
    blockcols=11;
    shifted=[eye(blocksize) ; eye(blocksize)];

    H=zeros(blockrows*blocksize,blockcols*blocksize);
    for i=0:blockcols-1
        for j=0:blockrows-1
            k=i*j;
            H(blocksize*j+1:blocksize*(j+1), blocksize*i+1:blocksize*(i+1))...
                =shifted(k+1:k+blocksize,:);
        end
    end
end


