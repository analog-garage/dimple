%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function isvalid = adderUnitDelta(a_in,b_in,c_in,c_out,s_out)
   isvalid = (s_out == mod(a_in+b_in+c_in,2)) && ...
             (c_out == (a_in+b_in+c_in >= 2)); 
end
