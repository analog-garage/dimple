%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function drawBit(bitVal,xpos,ypos,numSize)
    hold on;
    global zero_;
    global one_;
    if (isempty(zero_))
        zero_ = flipdim(imread('num0.jpg'),1);
    end
    if (isempty(one_))
        one_ = flipdim(imread('num1.jpg'),1);
    end
    
    m2 = image(zero_,'XData',[xpos-numSize xpos+numSize],'YData',[ +ypos-numSize ypos+numSize]);
    %m2 = image(zero_,'XData',[-1 1],'YData',[-1 1]);
    alpha(m2,1-bitVal);
    m1 = image(one_,'XData',[xpos-numSize xpos+numSize],'YData',[ +ypos-numSize ypos+numSize]);
    %m1 = image(one_,'XData',[-1 1],'YData',[-1 1]);
    alpha(m1,bitVal);
end
