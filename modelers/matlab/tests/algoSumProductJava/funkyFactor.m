function output = funkyFactor(x,y,z)
    if x == y && y == z
        if x == 0
            output = 1;
        else
            output = 2;
        end
    else
        output = 0;
    end
end

