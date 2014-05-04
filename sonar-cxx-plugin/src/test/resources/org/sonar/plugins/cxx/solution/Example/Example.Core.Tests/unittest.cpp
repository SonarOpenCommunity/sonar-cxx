#include "stdafx.h"
#include "CppUnitTest.h"

#include "../Example.Core/Money.h"
#include "../Example.Core/MoneyBag.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace TestMoney
{		
    TEST_CLASS( TestMoney )
	{

        Money f12CHF;
        Money f14CHF;
        Money f7USD;
        Money f21USD;

        MoneyBag fMB1;
        MoneyBag fMB2;

	public:
		
        TEST_METHOD( BagMultiply )
		{
            // Run a function under test here.
            Assert::AreEqual( 1, 1 );
            // Assert::AreEqual( expectedValue, actualValue, L"message", LINE_INFO() );
		}

        TEST_METHOD( BagNegate )
        {
            // TODO: Your test code here
        }

        TEST_METHOD( BagSubtract )
        {
            // TODO: Your test code here
        }

        TEST_METHOD( BagSumAdd )
        {
            // TODO: Your test code here
        }

        TEST_METHOD( IsZero )
        {
            // TODO: Your test code here
        }

        TEST_METHOD( MixedSimpleAdd )
        {
            // TODO: Your test code here
        }

        TEST_METHOD( MoneyBagEquals )
        {
            // TODO: Your test code here
        }

        TEST_METHOD( MoneyEquals )
        {
            // TODO: Your test code here
        }
	};
}