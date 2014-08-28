function ret = hasToolbox(taskName, verName, licenseName)
    ret = false;
    if (isempty(ver(verName)))
        dtrace(true, ['WARNING: ', taskName, ' was skipped because ', licenseName, ' not installed']);
    else
        ret = license('checkout', licenseName);
        if ~ret
            dtrace(true, ['WARNING: ', taskName, ' skipped because ', licenseName, ' license could not be obtained']);
        end
    end
end
