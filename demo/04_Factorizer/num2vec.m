function vec = num2vec(num,numBits)
    num = round(num);
    sn = sign(num);
    if sn < 0
        num = num+1;
    end
    num = abs(num);
        
    vec = zeros(1,numBits);
    for i = 1:numBits
        vec(1,i) = bitand(num,bitshift(1,i-1)) ~= 0;
    end
    
    if sn < 0
        vec = ~vec;
    end
end
