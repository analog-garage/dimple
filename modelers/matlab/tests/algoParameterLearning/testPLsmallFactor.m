%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
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

function testPLsmallFactor()
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % Create single factor and vars
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    verbose = 0;
    
    %provide different tolerances for the differnet degrees
    diffs = [1e-8 1e-2 1e-2];

    %Make things predictable
    rand('seed',1);

    %Sweep over different degrees of factor
    for B = 2:4
        dims = ones(1,B)*2;

        %Create the graph
        fg = FactorGraph();
        b = Bit(B,1);
        weights = rand(dims);
        weights = weights / sum(weights(:));
        fg.addFactor(weights,b);

        %Generate samples
        N = 1000;
        fg.Solver = 'gibbs';
        fg.Solver.setBurnInScans(10);
        fg.Solver.setNumSamples(N);
        fg.Solver.setScansPerSample(2);
        fg.Solver.saveAllSamples();
        fg.Solver.setSeed(1);
        fg.Solver.saveAllScores();
        fg.solve();
        samples = zeros(N,B);
        for i = 1:B
           samples(:,i) =  cell2mat(cell(b(i).Solver.getAllSamples()));
        end

        %Calculate imperical weights
        imperical_weights = zeros(dims);

        for i = 1:size(samples,1)
            inds = num2cell(samples(i,:)+1);
            ind = sub2ind(dims,inds{:});
            imperical_weights(ind) = imperical_weights(ind) + 1;
        end

        imperical_weights = imperical_weights / sum(imperical_weights(:));

        %%%%%%%%%%%%%%%%%%%%%%%
        %Learn the parameters
        
        %Build the factor graph
        fg2 = FactorGraph();
        b2 = Bit(B,1);
        f = fg2.addFactor(rand(dims),b2);

        %Create the learner
        pl = PLLearner(fg2,{f.FactorTable},{b2});

        %%%%%%%%%%%%%%%%%
        % Compare gradient to numerical gradient
        pl.setData(samples);
        gradient = pl.calculateGradient();
        num_gradient = zeros(1,2^B);
        delta = 0.00001;
        for i = 1:length(num_gradient)
            num_gradient(i) = pl.calculateNumericalGradient(f.FactorTable,i,delta);
        end
        diff = num_gradient-gradient;
        l2 = norm(diff);
        
        if verbose
            fprintf('l2 of numerical gradient: %f\n',l2);
        end
        assertTrue(l2 < 1e-5);
        
        %Learn
        args.numSteps = 2000;
        args.scaleFactor = 0.05;
        if verbose
            disp('learning...');
        end
       
        start = pl.calculatePseudoLikelihood();
        pl.learn(samples,args);
    	final = pl.calculatePseudoLikelihood();
        assertTrue(final > start);
        %Compare the weights
        cdims = num2cell(dims);
        learned_weights = reshape(f.FactorTable.Weights,cdims{:});

        diff = learned_weights-imperical_weights;        
        l2 = norm(diff(:));
        
        if verbose
            fprintf('l2: %f\n',l2);
        end
        
        assertTrue(l2 < diffs(B-1));
    end
end
