function valid = f(output, varargin)
    valid = sum(cell2mat(varargin)) == output;
end
