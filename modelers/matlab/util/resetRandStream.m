function [rstream] = resetRandStream(inString)
    rstream=RandStream(inString);
    version_array = matlabVersion();
    % setDefaultStream deprecated in favor of setGlobalStream
    globalStreamExists = (version_array(1) >= 2011);
    if (globalStreamExists == 1)
        RandStream.setGlobalStream(rstream);
    else
        RandStream.setDefaultStream(rstream);
    end
    reset(rstream);

