// see https://github.com/SonarOpenCommunity/sonar-cxx/wiki/Error-Recovery

int i = 0;

void func1()
{
    i = 1 //;
}

void func2()
{
    i = 2;
}
