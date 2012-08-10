function f = MinSumHelper(x,l,r,u,d)
    f = max(max(abs(x-l),abs(x-r)),max(abs(x-u),abs(x-d)))/4;
end
