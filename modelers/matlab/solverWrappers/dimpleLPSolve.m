function dimpleLPSolve( factorGraph )
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here
    
    
    sfg = factorGraph.Solver; 
    tic;
    sfg.buildLPState();
    disp(['Time spent building LP model from factor graph model:' num2str(toc) 'sec ']);
    solver=char(sfg.getMatlabLPSolver());
    if isequal(solver,'')
        solver='matlab';
    end
    
    
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
    
    
    disp(['Time spent transfering model to solver:' num2str(toc) 'sec ']);

    %
    % Do the LP solve
    %
    tic;
    switch solver
        case 'matlab'

            solution = linprog(-f, [], [], Aeq, beq, zeros(cols,1), ones(cols,1));

        case 'glpk'
            
            solution = glpk (-f, Aeq,beq,zeros(cols,1),ones(cols,1), repmat('S',rows,1),repmat('C',cols,1));
            
        case 'glpkIP'
            solution = glpk (-f, Aeq,beq,zeros(cols,1),ones(cols,1), repmat('S',rows,1),repmat('B',cols,1));
            
        case 'gurobi'
            model = [];
            model.A=Aeq;
            model.rhs=beq;
            model.sense = '=';
            model.obj=-f;
            model.lb=zeros(size(Aeq,2),1);
            model.ub=ones(size(Aeq,2),1);
            model.vtype='C';
            result = gurobi(model);
            solution=result.x;
            
            
        case 'gurobiIP'
            
            model = [];
            model.A=Aeq;
            model.rhs=beq;
            model.sense = '=';
            model.obj=-f;
            model.lb=zeros(size(Aeq,2),1);
            model.ub=ones(size(Aeq,2),1);
            model.vtype='B';
            result = gurobi(model);
            solution=result.x;
    end
    
    disp(['Time spent solving problem:' num2str(toc) 'sec ']);

    
    %
    % Write the solution back
    %
    
    sfg.setSolution(solution);
    
end

