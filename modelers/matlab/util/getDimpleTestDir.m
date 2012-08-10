function retval = getDimpleTestDir()
    x = getenv('DimpleTESTDIR');
    if isempty(x)
        retval = {};
    else
        x = regexp(x,';','split');
        retval = x;
    end
end
