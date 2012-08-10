function [ fg, external_vars ] = getFiniteFieldMatrixEquation(prim_poly, A)
% A is a matrix containing finite field *constants* (i.e. ints in the range
% [0,2^n-1]).
%
% This function produces a sub-factor-graph with border variables
% x_1,...,x_n.  It enforces the (matrix) relation:
%       Ax=0

num_rows=size(A,1);
num_cols=size(A,2);

external_vars=FiniteFieldVariable(prim_poly,num_cols,1);
fg=FactorGraph(external_vars);

for i=1:num_rows
    linear_equation_graph=getFiniteFieldLinearEquation(prim_poly, A(i,:));
    fg.addGraph(linear_equation_graph,external_vars);
end

