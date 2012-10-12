#include <QApplication>

int main(int argc, char *argv[])
{
    new int(); //memleak

    QApplication app(argc, argv);
    return app.exec();
}
