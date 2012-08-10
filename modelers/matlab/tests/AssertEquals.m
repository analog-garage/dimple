%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function AssertEquals(expected,actual,testName)
    if (abs(expected-actual) > eps)
        disp([testName ': Failed']);
        disp(['  expected: ' num2str(expected)]);
        disp(['  actual:   ' num2str(actual)]);
    end
end
