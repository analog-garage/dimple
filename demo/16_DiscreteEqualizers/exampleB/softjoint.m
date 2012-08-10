function [out]=softjoint(mask,sigma,X,Y)

val=(sum(mask.*X));
out=exp(-(val-Y)^2/(2*sigma^2));


