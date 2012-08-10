function cell_a = appendCell(cell_a, value)
    if length(cell_a) == 0
        cell_a = {value};
    else
        cell_a{length(cell_a) + 1} = value;
    end
end

