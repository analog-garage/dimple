%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [probability]=MC_transition_Tutorial(state1,state2)
 
switch state1
    case 'sunny'
        if state2=='sunny'
            probability=0.8;
        else
            probability=0.2;
            
        end
        
    case 'rainy'
        probability =0.5;
end 
