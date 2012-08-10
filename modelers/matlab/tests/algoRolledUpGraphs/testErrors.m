
function testErrors

    b = BitStream();

    slice = b.getSlice(1);

    slice.getNext();
    slice.getNext();

    b.get(1);
    b.get(2);

    message = '';
    try
        b.get(3);
    catch err
        message = err.message;
    end

    assertFalse(isempty(strfind(message,'A variable has not yet been instantiated for the specified index: 2.0')));
end
