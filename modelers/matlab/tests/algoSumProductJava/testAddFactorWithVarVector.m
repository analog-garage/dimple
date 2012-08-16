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

function [ output_args ] = testAddFactorWithVarVector( )

    %Should not error
    fg=FactorGraph();

    X=Variable(0:1,3,1);
    mask=[0.3; 1; 0.3];

    evalfunc=@(x,mask) exp(-sum(1-sum(mask.*x)^2));

    f = fg.addFactor(evalfunc,X,mask);

    %Also should not error
    fg=FactorGraph();

    X=Variable(0:1,1,3);
    mask=[0.3 1 0.3];

    evalfunc=@(x,mask) exp(-sum(1-sum(mask.*x)^2));

    f = fg.addFactor(evalfunc,X,mask);

end

