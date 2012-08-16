%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

classdef FecEncoding
    methods ( Static = true)
        function msgs = genmsgs(nummsgs,bugworkaround)
            if nargin < 2
                bugworkaround  = 1;
            end
            
            msgs = randint(704,nummsgs);

            if bugworkaround
                bin1 = 8;
                msgs((1+44*(bin1-1)):(44*bin1),:) = repmat(randint(1,nummsgs),44,1);
                bin2 = 12;
                msgs((1+44*(bin2-1)):(44*bin2),:) = repmat(randint(1,nummsgs),44,1);           
            end
            
        end
        function [sigma, adjSNR] = snr2sigma(EbNo,codeRate)
            M = 2;  % or 2
            k = log2(M);
            if (nargin < 2)
                codeRate = 2/3;
                %codeRate = 1;
            end
            % Adjust SNR for coded bits and multi-bit symbols.
            adjSNR = EbNo - 10*log10(1/codeRate) + 10*log10(k);
            sigma = sqrt(1/(10^(+adjSNR/10))/2);
        end
        function voltages = prob2volts(probs,minVoltage,maxVoltage)
            if (nargin < 3)
                maxVoltage = .4;
            end
            if (nargin < 2)
                minVoltage = -.4;
            end
            voltages = (probs)*(maxVoltage-minVoltage) + minVoltage;
        end
        function prob = llr2prob(llr)
            %Convert LLRs to probabilities.
            powers = 2 .^ llr;
            inversePowers = 1 ./ powers;
            plusOne = inversePowers +1;
            p0s = 1 ./ plusOne;
            prob = 1 - p0s;
        end
        function modulated = modulateCodeword(code)
            modulated = -code*2+1;
            modulated(1) = modulated(1) + 1e-32*sqrt(-1);
        end
        
        function y = genMatFile2Mat(fileName)
            x = dlmread(fileName);
            y = zeros(size(x,1),max(x(:,2)));
            
            for i = 1:size(y,1)
                if x(i,1) > 0
                   numNonZeros = x(i,1); 
                   y(i,1+x(i,2:(numNonZeros+1))) = 1;
                end
            end
            
        end
        
        function [y,G] = encode(x,G)
            %if no filename, provide default
            if nargin < 2
                load G;
            end
            
            y = mod(G*x,2);
        end
        
        
        function [p1s, msg_orig,code,llr,receivedsig] = EbNo2LdpcInputP(EbNo,msg_orig)

            % number of bits per iteration
            bitsPerIter = 704;

            [sigma,adjSNR] = FecEncoding.snr2sigma(EbNo);

            % Generate binary random numbers
            if (nargin < 2)
                msg_orig = randint(bitsPerIter, 1);
            end

            %encode    
            code = FecEncoding.encode(msg_orig,'gen80216e.txt');

            %Modulate
            modulatedsig = FecEncoding.modulateCodeword(code);

            %Add Noise
            rand('state', sum(100*clock));
            receivedsig = real(modulatedsig) + sigma*randn(size(modulatedsig));
%             receivedsig = awgn(modulatedsig, adjSNR, 0); % Signal power = 0 dBW

            %Demodulate
            [p1s,llr] = FecEncoding.demodCodeword(receivedsig,sigma);

        end
        function [voltages, msg_orig,code] = EbNo2LdpcInput(EbNo,msg_orig)
            % number of bits per iteration
            bitsPerIter = 704;

            % Generate binary random numbers
            if (nargin < 2)
                msg_orig = randint(bitsPerIter, 1);
            end

            [p1s,msg_orig,code] = FecEncoding.EbNo2LdpcInputP(EbNo,msg_orig);

            %Convert to voltages
            voltages = FecEncoding.prob2volts(p1s);

        end
        
        function [demodulated,llr] = demodCodeword(code,sigma)                        
            
            modObj = modem.pskmod('M',2,'InputType','Bit');


            % Construct a BPSK demodulator object to compute
            % log-likelihood ratios
            demodObj = modem.pskdemod(modObj,'DecisionType','approximate llr', ...
             'NoiseVariance',sigma^2);

            % Compute log-likelihood ratios (AWGN channel)
            llr = demodulate(demodObj, code);

            %Convert LLRs to probabilities.
            demodulated = FecEncoding.llr2prob(llr);
        end    
        
        function output = demod(input,sigma,formatStr,dollr)
            input = real(input);
            if nargin < 3
                dollr = 1;
            else
               if ~isequal(formatStr,'format')
                   error('expected ''format''');
               end
               if nargin < 4
                   error('Need to specify fourth argument');
               end
            end
            
            if dollr
                output = input*2/sigma^2;
            else
                output = 1-1./(1+exp(-2*input/(sigma^2)));
            end
        end
        
        function [demapSnr,gain] = noiseSnr2demapSnrBpsk(noiseSnr)
            global noiseSnrs;
            global demapSnrs;
            global gains;
            
            if (isempty(noiseSnrs))                
                [noiseSnrs, demapSnrs,gains] = GetSnrAndGainBpsk();
                [noiseSnrs, indices] = sort(noiseSnrs);
                demapSnrs = demapSnrs(indices);
                gains = gains(indices);
            end
            
            found = 0;
            
            if  isempty(noiseSnrs) || noiseSnr < noiseSnrs(1)
               demapSnr = noiseSnr;
               gain = 1;
            elseif noiseSnr > noiseSnrs(end)
                demapSnr = demapSnrs(end);
                gain = gains(end);
            else            
                for i = 1:length(noiseSnrs)
                    if noiseSnr <= noiseSnrs(i)
                        gain = gains(i);
                        demapSnr = demapSnrs(i);
                        break;
                    end
                end
            end
        end
    end 
end
