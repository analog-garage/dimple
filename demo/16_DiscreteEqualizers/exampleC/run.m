%% Third example for an equalizer

%% Model: X_i, i=1..n are generated from N(0,sigma_1^2) uniformly at random (
%% (!!! Unlike the previous equalizers this model's underlying variables are
%% continuous!!!)
%% Observations:
%% Y_i = a_1 X_{i-1} + a_2 X_i + a_3 X_{i+1} + N(0,\sigma^2)
%% Goal: from measurements Y_i, find most likely values for X_i

%% Equalizer is loopy
%% Observations Y_i are continuous but included in the solver
%% Uses a continuous BP solver or a softened Gibbs solver

global mask sigma sigma_1


N=256;  %number of variables. Boundary conditions: X_0=X_1025=1
sigma=0.4;
sigma_1=0.3;

% generating the problem first

mask=[0.3 1 0.3];
realX=sigma_1*randn(1,N);

realY=zeros(1,N);

%boundary condition first:
localX=[1 realX(1) realX(2)];
realY(1)=sum(localX.*mask);
localX=[realX(N-1) realX(N)  1];
realY(N)=sum(localX.*mask);

%other points
for i=2:N-1
       localX=realX(i-1:i+1);
       val=(sum(localX.*mask));
       realY(i)=val;
end
realG=sigma*randn(1,N);
realY=realY+realG;

error_init=norm(realY-realX);
fprintf('Initial L2 error:%d\n',error_init);


% Factor Graph

fg=FactorGraph();
fg.Solver = com.lyricsemi.dimple.solvers.particleBP.Solver();
tic;
X=Real([-3*sigma_1 3*sigma_1],com.lyricsemi.dimple.FactorFunctions.SimpleNormal(0,sigma_1),1,N);
Y=cell(1,N);
for i=1:N
Y{i}=Real([realY(i)-0.01 realY(i)+0.01],com.lyricsemi.dimple.FactorFunctions.SimpleNormal(realY(i),0.01),1,1);
end


%creating a boundary variable which always takes value 1
Xboundary=Real([1-0.01 1+0.01],com.lyricsemi.dimple.FactorFunctions.SimpleNormal(1,0.01),1,1);



%boundary conditions
func=com.lyricsemi.dimple.FactorFunctions.LinearEquation(mask,0.3);
fg.addFactor(func,Y{1}, Xboundary, X(1),  X(2));
fg.addFactor(func,Y{N}, X(N-1),  X(N),  Xboundary);

for i=2:N-1
    fg.addFactor(func,Y{i}, X(i-1),X(i),X(i+1));
end


for i=1:N
   X(i).Solver.setNumParticles(20);
   X(i).Solver.setResamplingUpdatesPerParticle(10);
   Y{i}.Solver.setNumParticles(1);
   Y{i}.Solver.setResamplingUpdatesPerParticle(1);
end
Xboundary.Solver.setNumParticles(1);
Xboundary.Solver.setResamplingUpdatesPerParticle(0);


fg.initialize();
tic;
fg.Solver.iterate(20); 
toc;

tput=N/toc;
error_final=sum(X.Value~=realX);
fprintf('Final number of errors:%d\n',error_final);
fprintf('Throughput: %d bits per second\n',tput);
