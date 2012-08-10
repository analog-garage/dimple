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
