function registry = getSolverRegistry()
    global gsolverRegistry___;
    
    if isempty(gsolverRegistry___)
        gsolverRegistry___ = SolverRegistry();
    end
    
    registry = gsolverRegistry___;
end

