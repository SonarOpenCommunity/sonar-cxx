#ifndef RFC_CLASS_H
#define RFC_CLASS_H

#include "AncestorClass.h"
#include "UsedClass.h"

class RfcClass : public AncestorClass
{
public:
	void publicMethod1();
	void publicMethod2();

private:
	UsedClass usedClass;

	void privateMethod1();
	void privateMethod2();
};

#endif