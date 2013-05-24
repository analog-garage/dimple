function dimpleLPSolve( factorGraph )
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

    sfg = factorGraph.Solver;
    sfg.buildLPState();
    
    %
    % Build the MATLAB LP arguments     
    % 
    
    f = sfg.getObjectiveFunction();

    rows = sfg.getNumberOfConstraints();
    cols = numel(f);
    
    % The variable constraints, which are the first constraints, have 1 on the rhs and the rest have zero.
    beq = [ones(sfg.getNumberOfVariableConstraints(),1); zeros(sfg.getNumberOfMarginalConstraints(),1)];
    
    termIter = sfg.getMatlabSparseConstraints();
    Aeq = spalloc(rows, cols, termIter.size());
    while (termIter.advance())
        row = termIter.getRow();
        col = termIter.getVariable();
        val = termIter.getCoefficient();
        Aeq(row,col) = val; %#ok<SPRIX>
    end
    
    %
    % Do the LP solve
    %
    
    solution = linprog(-f, [], [], Aeq, beq, zeros(cols,1), ones(cols,1));
    
    %
    % Write the solution back
    %
    
    sfg.setSolution(solution);
    
end

