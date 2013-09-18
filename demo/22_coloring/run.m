
num_max_colors=10; %upper bound on the number of colors required to color the graph
num_countries=10; % number of countries

load graph.mat; %contains information about a graph with ten polygons (or countries)

% Adj_matrix contains information about the adjacency of different
% countries - Adj_matrix(i,j) is 1 if and only if i and j share a border
% Vertex_info contains the location of the vertices of the polygon
% For each country i, C_info{i} contains the list of its corners as
% vertices index (where the indices are consistent with the rows of
% Vertex_info


setSolver('lp');


fg=FactorGraph();


ColorMax=Variable(1:num_max_colors,1,1); %maximum number of colors to be used
Colors=Variable(1:num_max_colors,num_countries,1); % color of each country
not_equal=@(x,y) (x~=y);
less_than=@(x,y) (x<=y);

for i=1:num_countries-1
    for j=(i+1):num_countries
        if Adj_matrix(i,j)
            fg.addFactor(not_equal,Colors(i),Colors(j)); % two neighboring countries cannot use the same color.
        end
    end
end

for i=1:num_countries
    fg.addFactor(less_than,Colors(i),ColorMax); % The ith color can only be used if at least i colors are used.
end

ColorMax.Input=exp(-(1:10)); %minimize the number of colors (L0 norm)



fg.Solver.setMatlabLPSolver('glpkIP');
fg.solve();
bestval=ColorMax.Value;
figure(2);
colors=rand(bestval,3); % probably should replace with something more visually pleasant
for idx=1:num_countries
    coords=Vertex_info(C_info{idx},:);
    color_idx=Colors(idx).Value;
    patch(coords(:,1),coords(:,2),colors(color_idx,:));
end





