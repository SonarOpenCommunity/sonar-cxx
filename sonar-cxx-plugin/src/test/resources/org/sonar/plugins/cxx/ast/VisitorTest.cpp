#include "extern/ExternHeader.h"
#include "VisitorTest.h"

double SecondClass::doSomething(int a, double b, float c) {
	return member4;
}

ThirdClass::ThirdClass() {
	member1 = 0;
	member2 = 1;
	member3.i = 2;
	member3.f = 3;
	member4 = 4;
}

int ThirdClass::calculate() {
	return member1 + member2;
}