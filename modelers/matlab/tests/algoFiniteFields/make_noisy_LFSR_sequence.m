function [true_data noisy_probs]=...
    make_noisy_LFSR_sequence(degree, primitive_poly, ...
    initial_fill, data_length, noise_level)
% Given an LFSR and an initial fill, generate a linear recursive
% sequence (in "true_data"_.  Then corrupt it, return the
% probabilities of the true values.

if or(noise_level<0, noise_level>1)
    error('noise_level must be in the range [0,1]\n');
end

true_data=zeros(data_length,1);
noisy_probs=zeros(data_length,1);

fill=initial_fill;
for i=1:data_length
    true_data(i)=bitget(fill,degree);
    % BSC / discrete noise
    noisy_probs(i)=true_data(i)*(1-2*noise_level) + noise_level;
    if rand()<noise_level
        noisy_probs(i)=1-noisy_probs(i);
    end
    
    fill=bitshift(fill,1);
    fill=bitand(fill,2^degree-1);
    if true_data(i)==1
        fill=bitxor(fill,primitive_poly);
    end
end

end

