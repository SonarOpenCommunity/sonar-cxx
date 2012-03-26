#include <QtTest/QtTest>
#include "SAMPLE-test.h"

SAMPLEtest::SAMPLEtest()
{
}

void SAMPLEtest::testCase1()
{
    new double(); //upps! we leak a double...
    QVERIFY2(2 == 1 + 1, "math Failure");
}
