function ret = hasCommunicationToolbox(taskName)
    % Verifies that Communications Toolbox is installed and license available.
    %
    % Also implicitly checks for dependencies: DSP System Toolbox and
    % Signal Processing toolbox.
    ret = hasDSPSystemToolbox(taskName) && ...
        hasToolbox(taskName, 'comm', 'communication_toolbox');
end
