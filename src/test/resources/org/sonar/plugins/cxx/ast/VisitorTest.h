#ifndef VISITOR_TEST_H
#define VISITOR_TEST_H

struct MyStruct;	//forward declaration

class FirstClass
{
public:
	int getMember()	{ return member1; }
	void setMember(int value) { member1 = value; }

	virtual int calculate()	= 0;

protected:
	int 		member1;
	float 		member2;
	MyStruct 	member3;
};

class SecondClass
{
public:
	double doSomething(int a, double b, float c, MyStruct d);	

protected:
	double member4;
};

class ThirdClass : public FirstClass, public global::SecondClass
{
public:
	ThirdClass();	
	virtual ~ThirdClass() {}
	virtual int calculate();
};

#endif