function numbits = getNumBits(num)
    msb = 1;
    while num > 1
       num = bitshift(num,-1);
       msb = msb+1;
    end
    numbits = msb;
end
