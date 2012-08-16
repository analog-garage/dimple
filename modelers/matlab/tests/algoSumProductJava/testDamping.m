function testDamping()


    %Test damping from variable

    for i = 1:2

        fg = FactorGraph();

        if i == 1
            %dampingVal = 0;
            dampingVal = .4;
            fg.Solver.setDamping(dampingVal);
        end

        a = Bit();
        b = Bit();
        f = fg.addFactor(@xorDelta,a,b);

        a.Input = .8;

        if i == 2
            dampingVal = .7;
            fg.Solver.setDamping(dampingVal);
        end

        fg.solve();

        msg2xor = dampingVal*.5 + (1-dampingVal)*.8;
        msgFromXor = dampingVal*.5 + (1-dampingVal)*msg2xor;

        assertEqual(b.Belief,msgFromXor);

        fg.solve();
    end


end

