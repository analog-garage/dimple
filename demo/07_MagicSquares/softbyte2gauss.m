%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [reconMean,reconSig,reconPx] = softbyte2gauss(moment,var_moment,minVal,maxVal)
    numbits = length(moment);
    if nargin < 3
        minVal = 0;
        maxVal = 2^numbits-1;
    else
        error('not yet supported');
    end
    
    reconMean = 0;
    for i = 1:length(moment)
        reconMean = reconMean + 2^(i-1)*moment(i);
    end
    
    reconSig = 0;
    
    
    if nargin >= 2
        recon_moment = 0;
        for i = 1:length(var_moment)
            recon_moment = recon_moment + 2^(i-1)*var_moment(i);
        end
        reconSig = sqrt(recon_moment-reconMean^2);
    end
    
    reconPx = 0;
    
end
