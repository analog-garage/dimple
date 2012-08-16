%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

function testConstMult()

    

    % Skip this test if the Communications Toolbox is unavailable.
    if isempty(which('gf')), return; end
    
    m = 2;
    
    numElements = 2^m;
    domain = 0:numElements-1;
    
    %Get 2^m items in the finite field and the primitive poly
    tmp = gf(domain,m);
    prim_poly = tmp.prim_poly;

    for constAsSecondParam = 0:1
        %Iterate all 2^m possible values of the constant
        for yindex = 2:length(tmp)
            y = tmp(yindex);

            %Create our two variables
            ff1 = FiniteFieldVariable(prim_poly);
            ff2 = FiniteFieldVariable(prim_poly);

            %Get y integer
            yint = y.x;
            yint = double(yint);

            %Create a new Factor Graph
            fg = FactorGraph();

            %Set constant
            if constAsSecondParam
                f = fg.addFactor(@finiteFieldMult,ff1,yint,ff2);
            else
                f = fg.addFactor(@finiteFieldMult,yint,ff1,ff2);
            end

            assertEqual(class(f.Solver),'com.analog.lyric.dimple.solvers.sumproduct.FiniteFieldConstMult');

            %Iterate values for the variables
            for xindex = 1:length(tmp)

                x = tmp(xindex);

                %test multiply
            %Set the priors of ff1 according to tmp(xindex)
            %which is one of the elements of the Galois Field
                priors = zeros(1,numElements);
                blah = x;
                blah = blah.x;
                blah = double(blah)+1;
                priors(blah) = 1;

                ff1.Input = priors;

                %Set the other variable to uniform distribution
                ff2.Input = ones(1,numElements)/numElements;

                %Solve
                fg.solve();

                %Run the MATLAB GF math to compare against
                z = x * y;

                z = z.x;
                z = double(z);

                assertEqual(z,double(ff2.Value));

                %Now, we do the same thing for division
            %(Setting ff2 to some value and ff1 to uniform dist
                priors = zeros(1,numElements);


                blah = x;
                blah = blah.x;
                blah = double(blah)+1;
                priors(blah) = 1;
                ff2.Input = priors;
                ff1.Input = ones(1,numElements)/numElements;

                fg.solve();

                z = x / y;
                blah = z.x;
                blah = double(blah);
                assertEqual(double(blah),double(ff1.Value));

            end
        end
    end
  
    %Test Schedule


end
