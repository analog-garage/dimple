
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Set solver type so we can create reals
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
setSolver('Gaussian');

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Build graph
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
a = Real();
b = Real();

ng = FactorGraph(a,b);
ng.addFactor(@constmult,b,a,1.1);

fg = FactorGraph();
s = RealStream();

fg.addFactor(ng,s,s.getSlice(2));

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%set data
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

data = [1 .1; repmat([0 Inf],10,1)];
dataSource = DoubleArrayDataSource(data);

s.DataSource = dataSource;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Solve
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

fg.initialize();

while fg.hasNext()
    fg.solve(false);    
    disp(s.FirstVar.Belief(1));    
    fg.advance();
end
