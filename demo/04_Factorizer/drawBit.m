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
