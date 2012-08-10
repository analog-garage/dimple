function [ fg, external_vars ] = getFiniteFieldLinearEquation(prim_poly, C)
% C is a vector containing finite field *constants* (i.e. ints in the range
% [0,2^d-1]).
%
% This function produces a sub-factor-graph with border variables
% x_1,...,x_n.  It enforces the relation:
% c_1*x_1  +  c_2*x_2  +  ...  +  c_n*x_n = 0

n=length(C);
external_vars=FiniteFieldVariable(prim_poly,n,1);
fg=FactorGraph(external_vars);

num_nonzero_elements=nnz(C);
if (num_nonzero_elements==0)
    return;
elseif (num_nonzero_elements==1)
    zero_distribution=zeros(length(external_vars(1).Domain),1);
    zero_distribution(1)=1;
    fg.addFactor(@inputFactor, zero_distribution, external_vars(1).Domain);
    return;
end

products=FiniteFieldVariable(prim_poly,num_nonzero_elements,1);
j=0;
for i=1:n
    if C(i)~=0
        j=j+1;
        fg.addFactor(@finiteFieldMult,external_vars(i),C(i),products(j));
    end
end

[fg2] = getNVarFiniteFieldPlus(prim_poly,num_nonzero_elements);
fg.addGraph(fg2,products);

end

