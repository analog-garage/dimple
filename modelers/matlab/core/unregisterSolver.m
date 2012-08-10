function unregisterSolver( name )
    registry = getSolverRegistry();
    registry.unregister(name);

end

