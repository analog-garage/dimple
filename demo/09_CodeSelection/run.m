% Do Not Modify. For use with automated testing.
global Dimple_DEMO_RESULT;

% Check for required toolbox.
if isempty(which('gf'))
    disp('Communications toolbox is required.');
    Dimple_DEMO_RESULT = -1;
    return;
end

code_selection

