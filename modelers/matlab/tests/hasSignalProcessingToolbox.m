function ret = hasSignalProcessingToolbox(taskName)
    % Verify that Signal Processing Toolbox is installed and license obtained
    ret = hasToolbox(taskName, 'signal', 'Signal_Toolbox');
end