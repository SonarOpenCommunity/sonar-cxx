#ifndef SAMPLETEST_H
#define SAMPLETEST_H
#include <QObject>

class SAMPLEtest : public QObject
{
    Q_OBJECT

public:
    SAMPLEtest();

private Q_SLOTS:
    void testCase1();
};

#endif // SAMPLETEST_H
