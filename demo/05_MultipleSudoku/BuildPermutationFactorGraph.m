%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2011, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function fg = BuildPermutationFactorGraph(N)

  vars = Variable(1:N,N,1);
    fg = FactorGraph(vars);

  for i=1:N-1
     for j=i+1:N
       fg.addFactor(@unequal, vars(i), vars(j));
     end
  end
end
