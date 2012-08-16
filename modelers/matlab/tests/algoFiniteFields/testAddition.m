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

function testAddition()

    % Skip this test if the Communications Toolbox is unavailable.
    if isempty(which('gf')), return; end
    
    m = 2;
    
    numElements = 2^m;
    domain = 0:numElements-1;
    
    tmp = gf(domain,m);
    prim_poly = tmp.prim_poly;

    x = gf(repmat(domain,numElements,1),m);
    y = x';
    z = x + y;

    %%%%%%%%%%%%%%%%%%%%
    %Test addition
    ff1 = FiniteFieldVariable(prim_poly);
    ff2 = FiniteFieldVariable(prim_poly);
    ff3 = FiniteFieldVariable(prim_poly);

    fg = FactorGraph();


    %Set constant
    fg.addFactor(@finiteFieldAdd,ff1,ff2,ff3);



    for i = 1:prod(size(x))


        %Get x integer
        priors = zeros(1,numElements);
        ind = x(i);
        ind = ind.x+1;    
        priors(ind) = 1;
        ff1.Input = priors;

        priors = zeros(1,numElements);
        ind = y(i);
        ind = ind.x+1;
        priors(ind) = 1;
        ff2.Input = priors;


        fg.solve();

        expected = z(i);
        expected = expected.x;
        assertEqual(double(expected),double(ff3.Value));
    

    end
    
end
