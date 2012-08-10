%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [probability]=observation_function_Tutorial(state,observation)
 
switch state    
    case 'sunny'
        
        switch observation
            
            case 'walk'
                probability=0.7;
            case 'book'
                probability=0.1;
            case 'cook'
                probability=0.2;
        end
        
    case 'rainy'
        
        switch observation
           
            case 'walk'
                probability=0.2;
            case 'book'
                probability=0.4;
            case 'cook'
                probability=0.4;
        end
end
