function registerSolver(name,constructor)
    %retrieve the solver registry
    registry = getSolverRegistry();    
    registry.register(name,constructor);
end
