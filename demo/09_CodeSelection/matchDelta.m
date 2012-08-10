function [ output_args ] = matchDelta( x,y,z )
% "z" is an "output" bit, in that it equals 0 if x==y, and is unset if
% x!=y.  If x and y were bits, then z = x NAND y.

if x==y
    if z==1
        output_args=0;
    else
        output_args=1;
    end
else
    output_args=0.5;
    %output_args=1;
end

%{
if and(x==y, z==1)
    output_args=0;
    %output_args=0.001;
else
    output_args=1;
end
%}

end

