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

classdef LyricBerTool < handle
    properties
        name;
        description;
        CodeRunnerClass;
        snrValues;
        maxNumErrs;
        maxNumBits;
        maxCodesPerIter;
        overwriteFile;
        names;
        descriptions;
        runformaxseconds;
        runformaxbits;

        cumnumbits;
    end

    methods (Static = true)
        function [d h m s] = secs2dhms(secs)
            d = floor(secs/86400);
            secs = secs-86400*d;
            h = floor(secs/3600);
            secs = secs-3600*h;
            m = floor(secs/60);
            s = secs-60*m;
        end
        function run(name,description,CodeRunnerClass,snrValues,maxNumErrs,maxNumBits,maxCodesPerIter,overwriteFile,names,descriptions,runformaxseconds,runformaxbits)
            berobj = LyricBerTool();
            if nargin < 8
                overwriteFile = 0;
            end
            if nargin < 9
                names = {};
            end
            if nargin < 10
                descriptions = {};
            end
            if nargin < 11
                %Set this to infinity so that the comparison in runberloop
                %will never fail if no argument is added (run like normal)
                runformaxseconds = Inf;
            end
            if nargin < 12
                %Set this to infinity so that the comparison in runberloop
                %will never fail if no argument is added (run like normal)
                runformaxbits = Inf;
            end
            berobj.name = name;
            berobj.description = description;
            berobj.CodeRunnerClass = CodeRunnerClass;
            berobj.snrValues = snrValues;
            berobj.maxNumErrs = maxNumErrs;
            berobj.maxNumBits = maxNumBits;
            berobj.maxCodesPerIter = maxCodesPerIter;
            berobj.overwriteFile = overwriteFile;
            berobj.names = names;
            berobj.descriptions = descriptions;
            berobj.runformaxseconds = runformaxseconds;
            berobj.runformaxbits = runformaxbits;
            
            berobj.cumnumbits = 0;
            %Start timing how long we've run
            tic;

            berobj.dorun();
        end
    end

    methods (Access='private')
        function obj = LyricBerTool()

        end
        function dorun(berobj)
            if ~exist('./BerData', 'dir')
                mkdir('./BerData');
            end
            berName = ['./BerData/' berobj.name '.mat'];
            berobj.names{end+1} = berName;
            berobj.descriptions{end+1} = berobj.description;

            berCodesName = [berName '_codes.mat'];
            tmpInfoName = [berName '_tmpInfo.mat'];

            if ~berobj.overwriteFile
                if (~isempty(dir(berName)))
                    error(['berRecord: ' berName ' already exists']);
                end
                if (~isempty(dir(berCodesName)))
                    error(['berRecord for codes: ' berName ' already exists']);
                end
            end

            %berRecord.ExeName =
            %berRecord.ClassName = className;
            berRecord.BerName = berName;
            berRecord.BerCodesName = berCodesName;
            berRecord.TmpInfoName = tmpInfoName;
            berRecord.MaxNumErrors = berobj.maxNumErrs;
            berRecord.MaxNumBits = berobj.maxNumBits;
            %berRecord.Threshold = threshold;
            berRecord.Description =berobj.description;
            berRecord.MaxCodesPerIter = berobj.maxCodesPerIter;
            berRecord.SnrValues = berobj.snrValues;
            berCodes.BerRecordName = berName;
            codeRunner = berobj.CodeRunnerClass;
            berobj.runsnrloop(berRecord,codeRunner,1);
        end
        function resume(berName,codeRunnerClass)
            load(berName);
            berCodesName = berRecord.BerCodesName;
            load(berCodesName); %gets berCodes
            tmpName = berRecord.TmpInfoName;

            codeRunner =berobj.CodeRunnerClass; %eval([berRecord.ClassName '()']);
            snrValIndex = length(berRecord.SnrRecord);

            if (~isempty(dir(tmpName)))
                %We were in the middle of an SNR point, so continue it
                load(tmpName);

                [berRecord,TmpCodeInfo] = LyricBerTool.runberloop(...
                    berRecord,TmpCodeInfo,codeRunner,names,descriptions);

                berCodes.SnrRecord(snrValIndex).BadCodes = TmpCodeInfo.BadCodes;
                berCodes.SnrRecord(snrValIndex).AllCodes = TmpCodeInfo.AllCodes;
                save([berCodesName],'berCodes');
                delete(tmpName);
            end


            LyricBerTool.runsnrloop(berRecord,codeRunner,snrValIndex+1,names,descriptions);
        end
        function runsnrloop(berobj,berRecord,codeRunner,startingIndex)
            if nargin < 4
                startingIndex = 1;
            end

            snrValues = berRecord.SnrValues;
            berName = berRecord.BerName;
            berCodesName = berRecord.BerCodesName;

            for snrValIndex = startingIndex:length(snrValues)
                %kill this loop if the total number of bits run exceeds our
                %limit or if the test has overrun our time limit
                if berobj.cumnumbits > berobj.runformaxbits || toc > berobj.runformaxseconds
                    break
                end
                TmpCodeInfo.BerName = berName;
                TmpCodeInfo.BerCodesName = berCodesName;
                TmpCodeInfo.BadCodes = struct([]);
                TmpCodeInfo.AllCodes = struct([]);
                snrVal = snrValues(snrValIndex);
                berRecord.SnrRecord(snrValIndex).Snr = snrVal;
                berRecord.SnrRecord(snrValIndex).NumBitErrors = 0;
                berRecord.SnrRecord(snrValIndex).NumBits = 0;

                [berRecord,TmpCodeInfo] = berobj.runberloop(berRecord,TmpCodeInfo,codeRunner);

                berCodes.SnrRecord(snrValIndex).BadCodes = TmpCodeInfo.BadCodes;
                berCodes.SnrRecord(snrValIndex).AllCodes = TmpCodeInfo.AllCodes;
                save([berCodesName],'berCodes');
                delete(berRecord.TmpInfoName);
            end
        end
        function [berRecord,TmpCodeInfo] = runberloop(berobj,berRecord,TmpCodeInfo,codeRunner)
            snrIndex = length(berRecord.SnrRecord);
            totNumErrors = berRecord.SnrRecord(snrIndex).NumBitErrors;
            totNumBits = berRecord.SnrRecord(snrIndex).NumBits;
            maxNumErrs = berRecord.MaxNumErrors;
            maxNumBits = berRecord.MaxNumBits;
            %thresh = berRecord.Threshold;
            maxCodesPerIter = berRecord.MaxCodesPerIter;
            snrVal = berRecord.SnrValues(snrIndex);
            berName = berRecord.BerName;

            codesPerIter = 1;

            berRecord.SnrRecord(snrIndex)

            N = 0;
            TotalBer = 0;
            BadBers = [];

            %TODO: also terminate if threshold reached.
            %Implemented: stop the loop once the cumnumbits limit is
            %reached and once max time is reached
            while totNumErrors < berobj.maxNumErrs && totNumBits < berobj.maxNumBits && berobj.cumnumbits < berobj.runformaxbits && toc < berobj.runformaxseconds

                %Collect bad codewords (txCodes, rxCode, outputs)
%                 disp('FecBpskRunner cycle');
%                 tic
                [numErrors,numBits,badCodeInfo,allCodeInfo] = run(codeRunner,snrVal,codesPerIter);
%                 toc
                
                %Increment the total by this run in prep for killing things
                %once we hit the limit.
                berobj.cumnumbits = berobj.cumnumbits + numBits;

                N = N + codesPerIter;

                totNumErrors = totNumErrors + numErrors;
                totNumBits = totNumBits + numBits;

                berRecord.SnrRecord(snrIndex).NumBitErrors = totNumErrors;
                berRecord.SnrRecord(snrIndex).NumBits = totNumBits;
                ber = totNumErrors/totNumBits;
                berRecord.SnrRecord(snrIndex).Ber = totNumErrors/totNumBits;

                fprintf('totNumErrors = %d\n',totNumErrors)
                fprintf('totNumBits = %d\n',totNumBits)
                fprintf('ber = %f\n',ber)
                fprintf('Cumulative num bits = %d\n',berobj.cumnumbits)
                [d h m s] = LyricBerTool.secs2dhms(toc);
                fprintf('Total time run = %d:%d:%d:%f\n',d,h,m,s)

                save([berName],'berRecord');
                for badCodeIndex = 1:length(badCodeInfo)
                    tmpCodeInfoIndex = length(TmpCodeInfo.BadCodes)+1;
                    if tmpCodeInfoIndex == 1
                        TmpCodeInfo.BadCodes = badCodeInfo(badCodeIndex);
                    else
                        TmpCodeInfo.BadCodes(tmpCodeInfoIndex) = badCodeInfo(badCodeIndex);
                    end
                end
                for allCodeIndex = 1:length(allCodeInfo)
                    tmpCodeInfoIndex = length(TmpCodeInfo.AllCodes)+1;
                    if tmpCodeInfoIndex == 1
                        TmpCodeInfo.AllCodes = allCodeInfo(allCodeIndex);
                    else
                        TmpCodeInfo.AllCodes(tmpCodeInfoIndex) = allCodeInfo(allCodeIndex);
                    end
                end

                berRecord.SnrRecord(snrIndex).NumCodes = N;
                berRecord.SnrRecord(snrIndex).NumBadCodes = length(BadBers);

                save([berRecord.TmpInfoName],'TmpCodeInfo');

                plotBer(berobj.names,.99,berobj.descriptions);
                pause(.1);

                codesPerIter = codesPerIter * 2;
                if (codesPerIter > berobj.maxCodesPerIter)
                    codesPerIter = berobj.maxCodesPerIter;
                end

                %Calculate sample mean
                sampleMean = TotalBer/N;

                if (sampleMean ~= 0 && N > 1)

                    %Calculate sample variance
                    numZeroBer = N - length(BadBers);
                    sampleVar = (numZeroBer*sampleMean^2 + ...
                        sum((BadBers - sampleMean).^2)) ...
                        / (N-1);

                    %Calculate ratio
                    ratio = sqrt(sampleVar)/sampleMean;

                    %If it is less than threshold terminate
                    if (ratio < thresh)
                        break;
                    end

                    ratio
                    thresh
                end
            end
        end
    end
end
