function versionNumber = getDimpleVersionNumber()
    numberize=[10^5 10^4 0 10^3 10^2 0 10 1];
    version = getDimpleVersion();
    blank = '00.00.00';
    versionNumber = dot(numberize, version - blank);
end
