function isvalid = munitDelta(x,y,m_in,c_in,m_out,c_out)

%{
    x = inputs(1,:);
    y = inputs(2,:);
    m_in = inputs(3,:);
    c_in = inputs(4,:);
    m_out = inputs(5,:);
    c_out = inputs(6,:);
  %}  
    %Take x,y,min,cin,mout,cout    
    %return true if all conditions are met
    %function valid = evalCarry(x,y,m_in,c_in)
    %    valid = (x & y & m_in) | (x & y & c_in) | (m_in & c_in);
    %end
    %function valid = evalMultiple(x,y,m_in,c_in)
    %    valid = bitxor(x & y,bitxor(m_in,c_in));
    %end
    %isvalid = evalCarry(x,y,m_in,c_in) == c_out & ...
    %            evalMultiple(x,y,m_in,c_in) == m_out;    
    
    isvalid = (((x & y & m_in) | (x & y & c_in) | (m_in & c_in))  == c_out) & ...
               (bitxor(x & y,bitxor(m_in,c_in)) == m_out);
    
    %isvalid = sum(x .* y) ~= 0;
end
