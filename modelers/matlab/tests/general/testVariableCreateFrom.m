function testVariableCreateFrom()

a = Bit;
b = VariableBase.createFrom(a);
assert(isa(b, 'Bit'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.Elements, b.Domain.Elements);

a = Bit(3,4);
b = VariableBase.createFrom(a);
assert(isa(b, 'Bit'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.Elements, b.Domain.Elements);


a = FiniteFieldVariable(7);
b = VariableBase.createFrom(a);
assert(isa(b, 'FiniteFieldVariable'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.Elements, b.Domain.Elements);
assertEqual(a.Domain.PrimitivePolynomial, b.Domain.PrimitivePolynomial);

a = FiniteFieldVariable(7, 3, 4);
b = VariableBase.createFrom(a);
assert(isa(b, 'FiniteFieldVariable'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.Elements, b.Domain.Elements);
assertEqual(a.Domain.PrimitivePolynomial, b.Domain.PrimitivePolynomial);


a = Discrete(2:11);
b = VariableBase.createFrom(a);
assert(isa(b, 'Discrete'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.Elements, b.Domain.Elements);

a = Discrete(2:11, 3, 4);
b = VariableBase.createFrom(a);
assert(isa(b, 'Discrete'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.Elements, b.Domain.Elements);


a = Real();
b = VariableBase.createFrom(a);
assert(isa(b, 'Real'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.LB, b.Domain.LB);
assertEqual(a.Domain.UB, b.Domain.UB);

a = Real([-1 2]);
b = VariableBase.createFrom(a);
assert(isa(b, 'Real'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.LB, b.Domain.LB);
assertEqual(a.Domain.UB, b.Domain.UB);

a = Real([-1 2], 3, 4);
b = VariableBase.createFrom(a);
assert(isa(b, 'Real'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.LB, b.Domain.LB);
assertEqual(a.Domain.UB, b.Domain.UB);


a = Complex();
b = VariableBase.createFrom(a);
assert(isa(b, 'Complex'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.NumElements, b.Domain.NumElements);
assertEqual(a.Domain.RealDomains{1}.LB, b.Domain.RealDomains{1}.LB);
assertEqual(a.Domain.RealDomains{1}.UB, b.Domain.RealDomains{1}.UB);
assertEqual(a.Domain.RealDomains{2}.LB, b.Domain.RealDomains{2}.LB);
assertEqual(a.Domain.RealDomains{2}.UB, b.Domain.RealDomains{2}.UB);

a = Complex(ComplexDomain(RealDomain(-1,2), RealDomain(-2,3)));
b = VariableBase.createFrom(a);
assert(isa(b, 'Complex'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.NumElements, b.Domain.NumElements);
assertEqual(a.Domain.RealDomains{1}.LB, b.Domain.RealDomains{1}.LB);
assertEqual(a.Domain.RealDomains{1}.UB, b.Domain.RealDomains{1}.UB);
assertEqual(a.Domain.RealDomains{2}.LB, b.Domain.RealDomains{2}.LB);
assertEqual(a.Domain.RealDomains{2}.UB, b.Domain.RealDomains{2}.UB);

a = Complex(ComplexDomain(RealDomain(-1,2), RealDomain(-2,3)), 3, 4);
b = VariableBase.createFrom(a);
assert(isa(b, 'Complex'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.NumElements, b.Domain.NumElements);
assertEqual(a.Domain.RealDomains{1}.LB, b.Domain.RealDomains{1}.LB);
assertEqual(a.Domain.RealDomains{1}.UB, b.Domain.RealDomains{1}.UB);
assertEqual(a.Domain.RealDomains{2}.LB, b.Domain.RealDomains{2}.LB);
assertEqual(a.Domain.RealDomains{2}.UB, b.Domain.RealDomains{2}.UB);


a = RealJoint(3);
b = VariableBase.createFrom(a);
assert(isa(b, 'RealJoint'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.NumElements, b.Domain.NumElements);
assertEqual(a.Domain.RealDomains{1}.LB, b.Domain.RealDomains{1}.LB);
assertEqual(a.Domain.RealDomains{1}.UB, b.Domain.RealDomains{1}.UB);
assertEqual(a.Domain.RealDomains{2}.LB, b.Domain.RealDomains{2}.LB);
assertEqual(a.Domain.RealDomains{2}.UB, b.Domain.RealDomains{2}.UB);
assertEqual(a.Domain.RealDomains{3}.LB, b.Domain.RealDomains{3}.LB);
assertEqual(a.Domain.RealDomains{3}.UB, b.Domain.RealDomains{3}.UB);

a = RealJoint(RealJointDomain(RealDomain(-1,2), RealDomain(-2,3), RealDomain()));
b = VariableBase.createFrom(a);
assert(isa(b, 'RealJoint'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.NumElements, b.Domain.NumElements);
assertEqual(a.Domain.RealDomains{1}.LB, b.Domain.RealDomains{1}.LB);
assertEqual(a.Domain.RealDomains{1}.UB, b.Domain.RealDomains{1}.UB);
assertEqual(a.Domain.RealDomains{2}.LB, b.Domain.RealDomains{2}.LB);
assertEqual(a.Domain.RealDomains{2}.UB, b.Domain.RealDomains{2}.UB);
assertEqual(a.Domain.RealDomains{3}.LB, b.Domain.RealDomains{3}.LB);
assertEqual(a.Domain.RealDomains{3}.UB, b.Domain.RealDomains{3}.UB);

a = RealJoint(RealJointDomain(RealDomain(-1,2), RealDomain(-2,3), RealDomain()), 3, 4, 5);
b = VariableBase.createFrom(a);
assert(isa(b, 'RealJoint'));
assertEqual(size(a), size(b));
assertEqual(a.Domain.NumElements, b.Domain.NumElements);
assertEqual(a.Domain.RealDomains{1}.LB, b.Domain.RealDomains{1}.LB);
assertEqual(a.Domain.RealDomains{1}.UB, b.Domain.RealDomains{1}.UB);
assertEqual(a.Domain.RealDomains{2}.LB, b.Domain.RealDomains{2}.LB);
assertEqual(a.Domain.RealDomains{2}.UB, b.Domain.RealDomains{2}.UB);
assertEqual(a.Domain.RealDomains{3}.LB, b.Domain.RealDomains{3}.LB);
assertEqual(a.Domain.RealDomains{3}.UB, b.Domain.RealDomains{3}.UB);

end

