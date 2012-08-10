function result = wrapProxyObject(proxyObject)
    if proxyObject.isGraph()

        result = FactorGraph('igraph',proxyObject);
    elseif proxyObject.isFactor()
        if proxyObject.isDiscrete()
            result = DiscreteFactor(proxyObject);
        else
            result = Factor(proxyObject);
        end

    elseif proxyObject.isVariable()
        if proxyObject.isDiscrete()
            domain = cell(proxyObject.getDomain().getElements());
            indices = 0;
            result = Discrete(domain,'existing',proxyObject,indices);
        elseif proxyObject.isJoint()
            domain = RealJointDomain(proxyObject.getDomain().getNumVars());
            indices = 0;
            result = RealJoint(domain,'existing',proxyObject,indices);
        else
            domain = RealDomain(proxyObject.getDomain().getLowerBound(),proxyObject.getDomain().getUpperBound);
            indices = 0;
            result = Real(domain,'existing',proxyObject,indices);
        end

    else
        error('not supported');
    end
end
