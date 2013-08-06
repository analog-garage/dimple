num_countries=20;
num_max_colors=10;
[Adj_matrix,Vertex_info,C_info]=voronoi_box(num_countries); % fails sometimes - rarely

setSolver('lp');


fg=FactorGraph();


ColorMax=Variable(1:num_max_colors,1,1);
Colors=Variable(1:num_max_colors,num_countries,1);
not_equal=@(x,y) (x~=y);
less_than=@(x,y) (x<=y);

for i=1:num_countries-1
    for j=(i+1):num_countries
        if Adj_matrix(i,j)
            fg.addFactor(not_equal,Colors(i),Colors(j));
        end
    end
end

for i=1:num_countries
    fg.addFactor(less_than,Colors(i),ColorMax);
end

ColorMax.Input=exp(-(1:10));



fg.Solver.setLPSolver('glpkIP');
fg.solve();
bestval=ColorMax.Value;
figure(2);
colors=rand(bestval,3); % probably should replace with something more visually pleasant
for idx=1:num_countries
    coords=Vertex_info(C_info{idx},:);
    color_idx=Colors(idx).Value;
    patch(coords(:,1),coords(:,2),colors(color_idx,:));
end





