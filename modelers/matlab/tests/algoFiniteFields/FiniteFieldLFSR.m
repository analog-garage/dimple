

% A cute way of describing an LFSR-decoding factor graph

% Describe the LFSR's taps
degree=7;
primitive_poly=2^7 + 2^1 + 2^0;

% Generate some sample data
initial_fill=randi(2^degree-1);
data_length=100;
noise_level=.30;  % <-- the probability of flipping a bit
[true_LFSR_sequence    noisy_probs]=...
    make_noisy_LFSR_sequence(degree, primitive_poly, ...
                            initial_fill, data_length, noise_level);

% Make factor graph
fg = FactorGraph();

ff =FiniteFieldVariable(primitive_poly,data_length,1);
lrs=Bit(data_length,1);
for i=1:data_length-1
    if i<data_length
        fg.addFactor(@finiteFieldMult,2,ff(i),ff(i+1));
    end
    fg.addFactor(@finiteFieldProjection,ff(i),degree-1,lrs(i));
    lrs(i).Input=noisy_probs(i);
end
%fg.setScheduler(com.analog.lyric.dimple.schedulers.TreeOrFloodingScheduler());

% Run factor graph
%fg.Solver.setNumIterations(data_length);
tic;
fg.Solver.setNumIterations(100);
fg.solve();
toc;

fprintf('True    initial fill=0x%x\n',initial_fill);
fprintf('Guessed initial fill=0x%x\n',ff(1).Value);
