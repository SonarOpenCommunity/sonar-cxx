#include "extern/ExternHeader.h"
#include "ClassTest.h"

/**
*	Documentation comment
**/
void MyClass::setValue(int value) {
	publicMember = value;
}

int MyClass::getValue() {
	return publicMember;
}

int MyClass::sanityCheck() {
	//do some stuff
	return 0;
}

void MyClass::init() {
	publicMember = 0;
	protectedMember = 0;
	privateMember = 0.0f;
}

bool MyClass::isValid() {
	return publicMember == protectedMember;
}
