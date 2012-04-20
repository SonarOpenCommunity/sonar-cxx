#include <QtTest/QtTest>
#include "SAMPLE-test.h"

int main(int argc, char *argv[])
{
    QApplication app(argc, argv);

    SAMPLEtest object;
    QTest::qExec(&object, argc, argv);
}
