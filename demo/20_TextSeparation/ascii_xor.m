function [ ret ] = ascii_xor( a,b,x )
    % Assume that a and b are ASCII characters.  Then interpret as 8-bit chars
    % and XOR them together to get x.
    ret = ( bitxor(a+0,b+0)==x);


end

