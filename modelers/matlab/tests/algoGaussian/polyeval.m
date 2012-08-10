function result = polyeval(coeffs,x)
    sz = size(x);
    x = reshape(x,numel(x),1);
    %y = Sum over k c*(a^2+b^2)^k*x
    result = zeros(size(x));
    y = real(x).^2+imag(x).^2;
    for i = 1:numel(coeffs)
       result = result + coeffs(i) * y.^(i-1).*x;
    end
    
    result = reshape(result,sz);
    
end
