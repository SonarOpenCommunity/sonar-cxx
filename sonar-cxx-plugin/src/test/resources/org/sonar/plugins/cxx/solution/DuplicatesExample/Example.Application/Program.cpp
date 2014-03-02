// ConsoleApplication1.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <Money.h>
#include <MoneyBag.h>

#include <iostream>
#include <string>

int _tmain(int argc, _TCHAR* argv[])
{

    MoneyBag bag = MoneyBag();
    Money moneyA = Money( 10, "EUR" );
    Money moneyB = Money( 20, "USD" );
    Money moneyC = Money( 15, "EUR" );
    Money moneyD = Money( 25, "JPY" );
    Money money = bag.AddMoney( moneyA );
    money = bag.AddMoney( moneyB );
    money = bag.AddMoney( moneyC );
    money = bag.AddMoney( moneyD );

    std::cout << "My Portfolio : " + bag.to_string() << std::endl;

	return 0;
}

