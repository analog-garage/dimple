%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [f boardWidth boardHeight] = drawLDPC(s,vals)
    
    numWidth = 150;
    numHeight = 150;
    sz = size(s);
    rows = sz(1);
    cols = sz(2);
    
    boardHeight = numHeight*rows;
    boardWidth = numWidth*cols;

    f = figure(1);
    
    hold off;
    %figure;
    plot(1,1);
    hold on;

    zero_img = flipdim(imread('zero.jpg'),1);
    one_img = flipdim(imread('one.jpg'),1);
    imgs = {one_img,zero_img};

    beliefs = s.Belief;
    values = s.Value;
    
    for row = 1:rows
        for col = 1:cols
            xLeft = (col-1)*numWidth+1;
            xRight = xLeft+numWidth;
            yBottom = (row-1)*numHeight+1;
            yTop = yBottom+numHeight;
            b = beliefs(row,col);
            b = [b 1-b];
            v = values(row,col);
                        
            
            
            for i = 1:2
                %rgb = imread(['num' num2str(i) '.jpg']);
                m = image(imgs{i},'XData',[xLeft xRight],'YData',[yBottom yTop ]);
                alpha(m,b(i));
            end

            if v ~= vals(row,col)
                plot([xLeft xRight],[yBottom,yTop],'Color','red','LineWidth',1);
                plot([xRight xLeft],[yBottom,yTop],'Color','red','LineWidth',1);
            end
        end
    end

    xlim([0 boardWidth]);
    ylim([0 boardHeight]);
end
