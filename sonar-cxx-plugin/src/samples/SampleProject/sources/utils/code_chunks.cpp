#include <stdio.h>
#include <stdlib.h>
#include <iostream>
using namespace std;

//this
//is
//a
//comment

/*
  this is
  a
  multiline
  comment
 */

void foo()
{
    char buffer[512]; //rats violation
    gets(buffer); //rats violation

    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;
    cout << "word" << endl;

    if(true){
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
        cout << "word" << endl;
    }

    // if(true){
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    //     cout << "word" << endl;
    // }

    const char* e = getenv("PATH");
    std::cout << *e << std::endl;
    if ( !e ) { std::cout << "environment variable PATH was not set" << std::endl; }

    int* ptr = nullptr;
    *ptr = 1;
}


