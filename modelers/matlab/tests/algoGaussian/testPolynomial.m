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

function testPolynomial()

    %%%%%%%%%%%%%%%%%%%%%%%%%%
    %Setup graph

    fg = FactorGraph();
    fg.Solver = 'Gaussian';

    y = ComplexVar();
    x = ComplexVar();


    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %test no inputs
    y.Input = MultivariateMsg([3 4],eye(2)*Inf);
    x.Input = MultivariateMsg([1 2],eye(2)*Inf);
    
    factor = fg.addFactor(@polynomial,y,x,0,1);
    fg.solve();
    
    assertElementsAlmostEqual(x.Belief.Means,[0 0]');
    assertElementsAlmostEqual(y.Belief.Means,[0 0]');
    assertTrue(min(diag(y.Belief.Covariance))>=1e6);
    assertTrue(min(diag(x.Belief.Covariance))>=1e6);
    
    
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %test forward real
    xval = 4;
    x.Input = MultivariateMsg([xval 0],eye(2)*1e-9);
    
    fg.solve();

    assertElementsAlmostEqual(y.Belief.Means,[xval 0]');
    assertTrue(max(max(y.Belief.Covariance))<1e-6);


    %y = 2x+3x^3
    fg.removeFactor(factor);
    factor = fg.addFactor(@polynomial,y,x,[0 1],[2 3]);
    fg.solve();
    expectedY = 2*xval + 3*xval^3;
    assertElementsAlmostEqual(y.Belief.Means,[expectedY; 0]);
    assertTrue(max(max(y.Belief.Covariance))<1e-2);


    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %test forward imag
    %y = x
    x.Input = MultivariateMsg([0 xval],eye(2)*1e-9);

    fg.removeFactor(factor);
    factor = fg.addFactor(@polynomial,y,x,0,1);
    fg.solve();
    assertElementsAlmostEqual(y.Belief.Means,[0; xval]);
    assertTrue(max(max(y.Belief.Covariance))<1e-6);


    %y = 2x+3x^3
    fg.removeFactor(factor);
    factor = fg.addFactor(@polynomial,y,x,[0 1],[2 3]);
    fg.solve();
    expectedY = 2*xval + 3*xval^3;
    assertElementsAlmostEqual(y.Belief.Means,[0; expectedY]);
    assertTrue(max(max(y.Belief.Covariance))<1e-2);


    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %test forward complex
    xval = 4+5*sqrt(-1);
    x.Input = MultivariateMsg([real(xval) imag(xval)],eye(2)*1e-60);

    fg.solve();
    expectedY = 2*xval + 3*abs(xval)^2*xval;
    assertElementsAlmostEqual(y.Belief.Means,[real(expectedY); imag(expectedY)]);
    %TODO: Why so large?
    assertTrue(max(max(y.Belief.Covariance))<1e-2);


    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %test backward real
    x.Input = MultivariateMsg([0 0],eye(2)*Inf);
    factor.Solver.setNumIterations(100);

    expectedX = 4;
    yval = 2*expectedX + 3*abs(expectedX)^2*expectedX;
    %TODO: why do I need to set to 1e-5.  Ack!
    y.Input = MultivariateMsg([real(yval) imag(yval)],eye(2)*1e-5);
    
    fg.solve();

    assertElementsAlmostEqual(x.Belief.Means,[real(expectedX); 0]);
    assertTrue(max(max(y.Belief.Covariance))<1e-4);

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %test backward imag
    x.Input = MultivariateMsg([0 0],eye(2)*Inf);

    factor.Solver.setNumIterations(100);

    expectedX = 4*sqrt(-1);
    yval = 2*expectedX + 3*abs(expectedX)^2*expectedX;
    y.Input = MultivariateMsg([real(yval) imag(yval)],eye(2)*1e-9);
    
    fg.solve();

    assertElementsAlmostEqual(x.Belief.Means,[real(expectedX); imag(expectedX)]);
    assertTrue(max(max(x.Belief.Covariance))<1e-6);


    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %test backward complex
    x.Input = MultivariateMsg([0 0],eye(2)*Inf);

    expectedX = 4+sqrt(-1)*5;
    yval = 2*expectedX + 3*abs(expectedX)^2*expectedX;
    y.Input = MultivariateMsg([real(yval) imag(yval)],eye(2)*1e-5);
    fg.solve();
    assertElementsAlmostEqual(x.Belief.Means,[real(expectedX); imag(expectedX)]);
    assertTrue(max(max(x.Belief.Covariance))<1e-6);
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %Test complex coefficients forward
    fg.removeFactor(factor);
    coeffs = [1+3*sqrt(-1) 2+4*sqrt(-1)];
    factor = fg.addFactor(@polynomial,y,x,[0 1],real(coeffs),imag(coeffs));
    
    xval = 4+5*sqrt(-1);
    yval = polyeval(coeffs,xval);
    x.Input = MultivariateMsg([real(xval) imag(xval)],eye(2)*1e-9);
    y.Input = MultivariateMsg([0 0],eye(2)*Inf);
   
    fg.solve();
    
    %TODO: pretty different
    assertTrue(max(y.Belief.Means - [real(yval); imag(yval)])<1e-5);
        
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %Test complex coefficients backwards
    x.Input = MultivariateMsg([0 0],eye(2)*Inf);
    y.Input = MultivariateMsg([real(yval) imag(yval)],eye(2)*1e-15);
   
    factor.Solver.setNumIterations(100);
    fg.solve();

    assertTrue(max(x.Belief.Means-[real(xval); imag(xval)])<1e-12);
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %TODO:
    %test variance.
    %{
    fg.removeFactor(factor);
    fg.addFactor(@polynomial,y,x,0,1);
    xr.Input = [4 1];
    xi.Input = [4 1];
    yr.Input = [0 Inf];
    yi.Input = [0 Inf];
    fg.solve();
    %}

end




