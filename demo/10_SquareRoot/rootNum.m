function [recovered_x] = rootNum(z)

    x=floor(sqrt(z));
    if x*x ~= z
        error ('Input not a perfect square!\n');
    end
    
    numxbits=ceil(log2(x));
    numzbits=2*numxbits;
    sx = ones(numxbits,1)*.5;
    [recovered_x, recovered_z] = softSquare(sx,num2vec(z,numzbits),1);
    
    

end
