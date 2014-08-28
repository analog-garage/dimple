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

function testProjection()

    % Skip this test if the Communications Toolbox is unavailable.
    if ~hasCommunicationToolbox('testProjection')
        return;
    end
    
    m = 3;

    numElements = 2^m;
    domain = 0:numElements-1;
    
    tmp = gf(domain,m);
    prim_poly = tmp.prim_poly;

    for i = 0:numElements-1

        bitValue = dec2bin(i,m)-'0';

        ff = FiniteFieldVariable(prim_poly);
        bits = Bit(m,1);

        fg = FactorGraph();

        fg.addFactor(@finiteFieldProjection,ff,0:m-1,flipud(bits));
        
        %%%%%
        %give 100% probabilities for bits and check gfs are right
        bits.Input = bitValue;
        fg.solve();
        assertEqual(double(ff.Value),i);


        
        %%%%
        %give 100% probabilities for gfs and see if bits are right
        ff = FiniteFieldVariable(prim_poly);
        bits = Bit(m,1);

        fg = FactorGraph();
        
        fg.addFactor(@finiteFieldProjection,ff,0:m-1,flipud(bits));

        %%%%%
        %give 100% probabilities for bits and check gfs are right
        priors = zeros(1,numElements);
        priors(i+1) = 1;
        ff.Input = priors;
        fg.solve();
        
        assertTrue(all(bitValue == bits.Value'));
        
        
    end
end
