%We use a particle BP solver to find the distribution over c given
%c = a*b
%when a is a normal variable with mean = 3 and std = 1
%     b is a normal variable with mean = 5 and std = 1

%First we set the Solver
setSolver('particlebp');

%Create three real variables.
a = Real();
b = Real();
c = Real();

%Create the factor graph.
fg = FactorGraph();

%specify the number of particles per message
fg.Solver.setNumParticles(30);

%specify the 
fg.Solver.setResamplingUpdatesPerParticle(30);

%Create the factor.
realProduct = com.lyricsemi.dimple.FactorFunctions.RealProduct();
fg.addFactor(realProduct,c,a,b);

%Set the inputs for a and b.
a.Input = com.lyricsemi.dimple.FactorFunctions.SimpleNormal(3,1);
b.Input = com.lyricsemi.dimple.FactorFunctions.SimpleNormal(5,1);

%Optionally use tempering
fg.Solver.setInitialTemperature(1);
fg.Solver.setTemperingHalfLifeInIterations(1);

%Solve
fg.initialize();
for i = 1:3
    fprintf('iteration: %d\n',i);
    fg.Solver.iterate();
end

%Retrieve the beliefs
x = 0:.01:30;
y = c.Solver.getBelief(x);

%Plot the result and display the mean of c
plot(x,y,'*');
mn = x*y;
fprintf('We expect the mean of c to be around 15: %f\n',mn);

