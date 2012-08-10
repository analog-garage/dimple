tic

burnin = 100;
samples = 300;
spacing = 10;
results = chimplify(@myprogram,burnin,samples,spacing);


toc

mat = cell2mat(results);
both = reshape(mat,2,numel(mat)/2);
weights = both(1,:);
numflips = both(2,:);
hist(weights);
