%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2011, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

classdef LdpcRunner
    properties
        Ldpc;
        X;
    end
    methods
       function obj = LdpcRunner(numIterations,useNested)
           if nargin < 1
               numIterations = 20;
           end
           if nargin < 2
               useNested = 1;
           end
           [obj.Ldpc,obj.X] = createLdpc(load('matrixout.txt'),useNested,numIterations);
           obj.Ldpc.Scheduler = com.analog.lyric.dimple.schedulers.TreeOrFloodingScheduler();
       end
       function [numErrors,numBits,badCodeInfo,allCodeInfo] = run(obj,snrVal,codesPerIter)
           randStates = cell(codesPerIter,1);
           randnStates = cell(codesPerIter,1);
           numErrors = 0;
           numBits = 0;
           badCodeInfo = [];
           allCodeInfo = [];
           badCodeInfoIndex = 1;
           
           vars = obj.X;
           ldpc = obj.Ldpc;
           
           for i = 1:codesPerIter
               randStates{i} = rng();
               randnStates{i} = rng();
               
              %Generate msg
              msg = randi([0 1], 704,1);
              code = FecEncoding.encode(msg);
              [sigma,adjSnr] = FecEncoding.snr2sigma(snrVal);
              
               %Modulate
               modulatedsig = FecEncoding.modulateCodeword(code);
               receivedsig = awgn(modulatedsig, adjSnr, 0); % Signal power = 0 dBW
               input = (FecEncoding.demod(receivedsig,sigma,'format',0));

               vars.Input = input;
               %setBitInput(vars,input);
               %tic
               solve(ldpc);
               %toc
               
               guesses = vars.Belief > .5;
               
               numMsgErrors = sum((guesses(1:704) > .5) ~= msg);
               numBits = numBits + 704;
               numErrors = numErrors+numMsgErrors;
               
               if (numMsgErrors > 0)
                   badCodeInfo(badCodeInfoIndex).NumErrors = numMsgErrors;
                   badCodeInfo(badCodeInfoIndex).NumBits = 704;
               end
               
           end
       end
    end
    
    
end
