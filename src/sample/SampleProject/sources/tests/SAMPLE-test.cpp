#include <QtTest/QtTest>
#include "SAMPLE-test.h"

SAMPLEtest::SAMPLEtest()
{
}

void SAMPLEtest::testCase1()
{
    QVERIFY2(2 == 1 + 1, "math Failure");
}
