#include "../include/RfcClass.h"

void RfcClass::publicMethod1() {
	ancestorMethod1();
}

void RfcClass::publicMethod2() {
	ancestorMethod2();
}

void RfcClass::privateMethod1() {
	usedClass.usedMethod1();
	usedClass.usedMethod2();
}

void RfcClass::privateMethod2() {

}
