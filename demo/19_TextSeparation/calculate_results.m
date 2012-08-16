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

function error_rate = calculate_results(t1,t2,t3,plaintext,verbosity,overlap_length,hint_rate)

    text = {t1,t2,t3};

    if verbosity > 0
        for t = 1:3
            fprintf('Text: %d\n',t);
            for i=0:floor(overlap_length/60)
                j=60*i + 1:60*i+60;
                j2=60*i + 60;
                j2=min(j2,overlap_length);

                a=plaintext(j:j2,t).Value';
                b=text{t}(j:j2);
                diff=(a~=b);
                diff_pretty=a.*diff;
                diff_pretty(diff==0)='.';
                disp(text{t}(j:j2));
                disp(char(diff_pretty));

                fprintf('\n');
            end
        end
    end
    
    num_errors=sum(plaintext(:,1).Value' ~= t1);
    
    error_rate=num_errors/length(t1);
    
    if verbosity>0
        fprintf('Character error rate = %f   (bytes=%d, hint rate=%d)\n',...
            error_rate, overlap_length, hint_rate);
    end

end
