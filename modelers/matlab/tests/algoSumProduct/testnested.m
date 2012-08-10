%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testnested()
    %disp('++testnested')
    %if strcmp(class(CSolver), class(getSolver())
        b = Bit(2,1);
        ng = FactorGraph(b);
        ng.addFactor(@xorDelta,b);

        b2 = Bit(2,1);
        g = FactorGraph();
        g.addFactor(ng,b2);
        b2.Input = [.9 .5]';
        g.Solver.setNumIterations(2);
        g.solve();
        assertElementsAlmostEqual(b2.Belief,[.9 .9]');
        
        
        %Do the same thing but with CSL
        b = Bit(2,1);
        ng = FactorGraph(b(1),b(2));
        ng.addFactor(@xorDelta,b);

        b2 = Bit(2,1);
        g = FactorGraph();
        g.addFactor(ng,b2);
        b2.Input = [.9 .5]';
        g.Solver.setNumIterations(2);
        g.solve();
        assertElementsAlmostEqual(b2.Belief,[.9 .9]');

        %disp('--testnested')
    %else
        %disp('--testnested skipped')
    %end
end

