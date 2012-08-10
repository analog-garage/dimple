%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function drawMagicSquare(b,figureNum,titleString)

    if nargin < 2
        figureNum = 1;
    end
    if nargin < 3
        titleString = 'Magic Square';
    end

    N = 4;
    numBits = 4;
    if nargin < 1
        b = rand(N,N,numBits);
    end

    squareWidth = 100;
    margin = 5;
    maxVal = (squareWidth)*N;

    x = ones(maxVal,maxVal,3);

    %TODO: This could probably be sped up by vectorizing some of the code.
    for i = 1:N
        for j = 1:N

            leftX = (j-1)*(squareWidth)+1;
            rightX = (j)*(squareWidth)+1;
            upperY = (N-i+1)*squareWidth+1;
            lowerY = (N-i)*squareWidth+1;

            minScale = 1/8;
            maxScale = .95;

            xScale = b(i,j,4)*(maxScale-minScale)+minScale;
            xMargin = int32(floor((squareWidth-squareWidth*xScale)/2));
            leftX = leftX+xMargin;
            rightX = rightX-xMargin;

            yScale = (1-b(i,j,4))*(maxScale-minScale)+minScale;
            yMargin = int32(floor((squareWidth-squareWidth*yScale)/2));
            upperY = upperY-yMargin;
            lowerY = lowerY+yMargin;

            %xSz = squareWidth-margin*2;

            for k = lowerY:upperY
                for l = leftX:rightX
                    x(k,l,:) = b(i,j,1:3)*.9;
                end
            end

        end
    end

    figure(figureNum), image(x);
    title(titleString);
end
