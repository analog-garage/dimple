function [out]=softjoint(X,Y);
global mask sigma
val=(sum(mask.*X));
out=exp(-(val-Y)^2/(2*sigma^2));


