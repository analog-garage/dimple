%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
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

%{
Multivariate Kalman Filter
%}

%Position at time 0
p0 = [20 20];

%Velocity at time 0
v0 = [0 5];

%acceleration at time 0
a0 = [0 0];

%Force vector
Fw = [1 1];

%State at time 0
x00 = [p0 v0 a0 Fw]';

%Predicted state at time 0
xhat00 = [0 0 0 0 0 0 0 0]';

%Predicted covariance at time 0
P00 = eye(8)*1000;

%How many time steps do we want to take
%timesteps = 60;
timesteps = 40;

%Operate once a second.
dt = 1;

%Used for friction.
gamma = .1;
m = 1;


%F is the state transition model.  (Given current state, what's the next 
%state?)
%new position is a function of old position, velocity, acceleration
%New velocity is old velocity + acceleration
%new acceleration is a function of velocity, friction, and force. 

F = [1  0   dt          0           dt^2/2  0       0       0;
    0  1   0           dt          0       dt^2/2  0       0;
    0  0   1           0           dt/2    0       0       0;
    0  0   0           1           0       dt/2    0       0;
    0  0   -gamma/m    0           0       0       Fw(1)   0;
    0  0   0           -gamma/m    0       0       0       Fw(2);
    0  0   0           0           0       0       1       0
    0  0   0           0           0       0       0       1
    ];

%H is the matrix that projects down to the observation.
H = [1 0 0 0 0 0 0 0;
    0 1 0 0 0 0 0 0];

setSolver('Gaussian');

%Create the FactorGraph for a single time step
fz = RealJoint(numel(p0));
fz.Name = 'fz';
fv = RealJoint(numel(p0));
fv.Name = 'fv';
fznonoise = RealJoint(numel(p0));
fx = RealJoint(numel(x00));
fxnext = RealJoint(numel(x00));
nested = FactorGraph(fx,fxnext,fznonoise,fv,fz);
nested.addFactor(@constmult,fznonoise,H,fx);
nested.addFactor(@add,fz,fv,fznonoise);
nested.addFactor(@constmult,fxnext,F,fx);

%Now we create the rolled up graph.
fzs = RealJointStream(numel(p0));
fvs = RealJointStream(numel(p0));
fznonoise = RealJointStream(numel(p0));
fxs = RealJointStream(numel(x00));
fg = FactorGraph();
rf = fg.addRepeatedFactor(nested,fxs.getSlice(1),fxs.getSlice(2),...
                     fznonoise.getSlice(1),fvs.getSlice(1),fzs.getSlice(1));
                 
%If we set the buffersize to something other than the default of 1, this
%will no longer be a Kalman filter.  Backwards messages will improve the
%guesses.
%rf.BufferSize = 10;

%Create data sources.
zDataSource = MultivariateDataSource();
vDataSource = MultivariateDataSource();

%sigma for the observation of x and y
sigmax = 4;
sigmay = 4;

%Initialize arrays to save data.
pxs = zeros(timesteps,1);
pys = zeros(timesteps,1);
mxs = zeros(timesteps,1);
mys = zeros(timesteps,1);

gxs = zeros(timesteps,1);
gys = zeros(timesteps,1);

x = x00;
xhat = xhat00;
P = P00;

R = [sigmax^2 0; 
     0      sigmay^2;];

%Now we iterate to do the kalman filter algorithm.
%We perform the kalman filter math here so that we can compare against Dimple
%at the end.
for i = 1:timesteps
    
    %Record the actual location.
    pxs(i) = x(1);
    pys(i) = x(2);
    
    %Generate the noise to add to the observation.
    v = randn(2,1).*[sigmax; sigmay];
    
    %Generate the observation.
    z = H*x + v;
    
    %Set the Dimple data sources so we can solve with DMPl later.
    zDataSource.add(z,eye(2)*1e-100);
    vDataSource.add(zeros(2,1),R);
    
    %Store in the resulting xs and ys
    mxs(i) = z(1);
    mys(i) = z(2);

    %xhat is state estimate
    
    %Predicted state estimate
    xhatkgivenkm1 = F*xhat;
    
    %Predicted estimate covariance
    Pkgivenkm1 = F*P*F'; % + Q;
    
    %innovation or measurement residual
    ytilda = z - H*xhatkgivenkm1;
    
    %Innovation covariance
    S = H*Pkgivenkm1*H' + R;
    
    %Optimal Kalman Gain
    K = Pkgivenkm1*H'*S^-1;
    
    %updated state estimate
    xhat = xhatkgivenkm1 + K*ytilda;
    
    %Updated estimate covariance
    P = (eye(size(K,1)) - K*H) * Pkgivenkm1;    
    
    %Retrieve the positions
    gxs(i) = xhat(1);
    gys(i) = xhat(2);
    
    %update the actual state
    x = F*x;
    

    
end

%Now set the data sources.
fzs.DataSource = zDataSource;
fvs.DataSource = vDataSource;

%Create arrays to save data.
fgxs = zeros(timesteps,1);
fgys = zeros(timesteps,1);

%Step through time and solve the Factor Graph.
for i = 1:timesteps
   fg.solve();
   fgxs(i) = fxs.FirstVar.Belief.Means(1);
   
   fgys(i) = fxs.FirstVar.Belief.Means(2);
         
   if fg.hasNext()
       fg.advance();
   else
       break;
   end
end


%Plot stuff
hold off;

%Plot the actual positions.
plot(pxs,pys,'g.-');
hold on;

%plot the noisy measurements
plot(mxs,mys,'r.-');
axis equal;

%plot the guesses.
plot(gxs,gys,'b.-');

%plot the factor graph guess
plot(fgxs,fgys,'mo-');

legend('Actual Positions','Noisy Measurements','Kalman Filter Guesses','Dimple guesses');
xlabel('x position');
ylabel('y position');
