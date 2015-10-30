function ret = hasDSPSystemToolbox(taskName)
    % Verify that DSP System Toolbox is installed and license is available.
    %
    % Also implicitly checks for Signal Processing Toolbox on which this depends
    ret = hasSignalProcessingToolbox(taskName) &&...
        hasToolbox(taskName, 'dsp', 'Signal_Blocks');
end