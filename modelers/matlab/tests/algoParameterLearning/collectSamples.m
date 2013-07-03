function samples = collectSamples(fg,burnIns,numSamples,scansPerSamples)

    BURN_INS = burnIns;
    NUM_SAMPLES = numSamples;
    SCANS_PER_SAMPLE = scansPerSamples;

    fg.Solver = 'gibbs';
    fg.Solver.setBurnInScans(BURN_INS);
    fg.Solver.setNumSamples(NUM_SAMPLES);
    fg.Solver.setScansPerSample(SCANS_PER_SAMPLE);
    fg.Solver.saveAllSamples();
    fg.Solver.setSeed(1);
    fg.Solver.saveAllScores();
    fg.solve();

    numvars = length(fg.Variables);
    samples = zeros(NUM_SAMPLES,numvars);

    for i = 1:numvars
        samples(:,i) = cell2mat(cell(fg.Variables{i}.Solver.getAllSamples()));
    end
    
end