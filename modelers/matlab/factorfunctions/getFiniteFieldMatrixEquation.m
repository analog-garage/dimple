%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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

