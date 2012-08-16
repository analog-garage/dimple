%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

function plotBer(berRecordNames,confidenceLevel,legendNames)
    %Plots BERs and stuff. If confidenceLevel is -1, error bars are turned
    %off and the plots run normally without them.
    
    %%Turn error bars off if confidenceLevel is -1
    if confidenceLevel == -1
        errorbarson = 0;
        %set confidence level to something reasonable so that the rest of
        %the function doesn't coke.
        confidenceLevel = .99;
    else
        errorbarson = 1;
    end
    colors = ['r','g','b','k','m','c'];
    shapes = ['o','x','+','*','s','d','v','^','<','>','p','h'];
    
    %TODO: has to be better way
    hold off;
    %semilogy(0,0);
    %hold on;
    if nargin < 2
        confidenceLevel = .90;
    end
    
    z = 0;
    switch (confidenceLevel)
        case .8
            z = 1.282;
        case .9
            z = 1.645;
        case .95
            z = 1.96;
        case .98
            z = 2.326;
        case .99
            z = 2.576;
    end
    if (z == 0)
        error(['Invalid confidenceLevel: ' num2str(confidenceLevel)]);
    end
    
    %for i = 1:length(berRecordNames)
    %    eval(['load ' berRecordNames{i}]);
    %    berRecords(i) = berRecord;
    %end
    
    if nargin >= 3
        names = legendNames;
    end
    
    minBer=2;
    maxBer=0;
    minSnr = 1000;
    maxSnr = -1000;
    
    for i = 1:length(berRecordNames)
        %weird apostrophe fiesta: doing this wraps whatever string is in
        %berRecordNames(i) in apostrophes. Can't hurt, and allows for
        %spaces.
        eval(['load ''' berRecordNames{i} '''']);
        record = berRecord;
        
        %Get Name
        if nargin < 3
            names{i} = record.Description;
        end
        clear snrs;
        clear bers;
        clear numbits;
        %errs = zeros(length(record.SnrRecord));
        for j = 1:length(record.SnrRecord)
            %rec = record.SnrRecord(j);
            snrs(j) = record.SnrRecord(j).Snr;
            bers(j) = record.SnrRecord(j).Ber;
            numbits(j) = record.SnrRecord(j).NumBits;
            if (bers(j) == 0)
                bers(j) = 1/(2*numbits(j));
            end
            if (bers(j)) > maxBer
                maxBer = bers(j);
            end
            if (bers(j) < minBer)
                minBer = bers(j);
            end
            if snrs(j) > maxSnr
                maxSnr = snrs(j);
            end
            if snrs(j) < minSnr
                minSnr = snrs(j);
            end

        end
        %semilogy(snrs,bers,'o-');
        hs = z*sqrt(bers.*(1-bers)./numbits);
        color = colors(mod(i-1,length(colors))+1);
        shape = shapes(floor(i/length(colors))+1);
        
        if errorbarson == 1
            errorbar(snrs,bers,hs,hs,[color shape '-']);
        elseif errorbarson == 0
            plot(snrs,bers,[color shape '-']);
        else
            disp('I''m confused about error bars, plotBer.m line 88');
        end
        
        if i == 1
            hold on;
        end
        %errorbar(snrs,bers,hs,hs,[shape '-']);
        %errorBar(snrs,bers,errsl,errsu);
    end
    set(gca,'yscale','log');

    if maxSnr == minSnr
        maxSnr = minSnr+1;
    end
    s = warning('off', 'MATLAB:Axes:NegativeDataInLogAxis');
    axis([minSnr maxSnr minBer/10 1]);
    warning(s);
    xlim([0,5]);
    ylim([1e-8,1]);
    xlabel('E_b/N_0 (dB)');
    ylabel('BER');
    legend(names);
    grid on;
    hold off;
end
