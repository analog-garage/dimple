function ret = hasToolbox(taskName, verName, licenseName)
    %hasToolbox returns true if toolbox is installed and license is available
    %
    %       hasToolbox(taskName, verName, licenseName)
    %
    % Verifies that toolbox with product name 'verName' (as given to
    % the ver() command) is installed, and that the specified license
    % name can be obtained. If false and dtrace() is enabled, then this
    % will log a message using the specified 'taskName'.
    %
    % For use in tests to selectively enable test cases conditioned on the
    % availability of installed products and/or floating license.
    %
    % See also ver, license
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
