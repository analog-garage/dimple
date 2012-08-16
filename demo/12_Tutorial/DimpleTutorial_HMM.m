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

disp(sprintf('\n++Dimple Tutorial HMM\n'));

HMM= FactorGraph();
Domain={'sunny','rainy'};
MondayWeather=Variable(Domain);
TuesdayWeather=Variable(Domain);
WednesdayWeather=Variable(Domain);
ThursdayWeather=Variable(Domain);
FridayWeather=Variable(Domain);
SaturdayWeather=Variable(Domain);
SundayWeather=Variable(Domain);

HMM.addFactor(@MC_transition_Tutorial,MondayWeather,TuesdayWeather);
HMM.addFactor(@MC_transition_Tutorial,TuesdayWeather,WednesdayWeather);
HMM.addFactor(@MC_transition_Tutorial,WednesdayWeather,ThursdayWeather);
HMM.addFactor(@MC_transition_Tutorial,ThursdayWeather,FridayWeather);
HMM.addFactor(@MC_transition_Tutorial,FridayWeather,SaturdayWeather);
HMM.addFactor(@MC_transition_Tutorial,SaturdayWeather,SundayWeather);

HMM.addFactor(@observation_function_Tutorial,MondayWeather,'walk');
HMM.addFactor(@observation_function_Tutorial,TuesdayWeather,'walk');
HMM.addFactor(@observation_function_Tutorial,WednesdayWeather,'cook');
HMM.addFactor(@observation_function_Tutorial,ThursdayWeather,'walk');
HMM.addFactor(@observation_function_Tutorial,FridayWeather,'cook');
HMM.addFactor(@observation_function_Tutorial,SaturdayWeather,'book');
HMM.addFactor(@observation_function_Tutorial,SundayWeather,'book');

MondayWeather.Input=[0.7 0.3];
HMM.Solver.setNumIterations(20);
HMM.solve;

disp('TuesdayWeather.Belief after solve with 20 iterations:');
TuesdayWeather.Belief

HMM.Solver.setNumIterations(0);
HMM.solve;
disp('TuesdayWeather.Belief after solve with 0 iterations:');
TuesdayWeather.Belief
HMM.Solver.iterate(1);
disp('TuesdayWeather.Belief after solve with 1 iteration:');
TuesdayWeather.Belief
HMM.Solver.iterate(5);
disp('TuesdayWeather.Belief after solve with 5 more iterations:');
TuesdayWeather.Belief
disp('TuesdayWeather.Belief after solve with 14 more iterations:');
TuesdayWeather.Belief

disp(sprintf('--Dimple Tutorial HMM\n'));
