#pragma once
#include "Money.h"
#include <vector>

// MoneyBag handles currencies separately

class MoneyBag :
    public Money
{
private:
    std::vector<Money> fMonies;
    std::vector<Money>::iterator FindMoney( std::string currency );
    bool Contains( Money aMoney );

public:
    MoneyBag();
    ~MoneyBag();    
    MoneyBag( std::vector<MoneyBag> bag );
    MoneyBag( Money m1, Money m2 );
    MoneyBag( Money m, MoneyBag bag );
    MoneyBag( MoneyBag m1, MoneyBag m2 );
    Money Add( Money m );
    Money AddMoney( Money m );
    Money AddMoneyBag( MoneyBag s );
    void AppendBag( MoneyBag aBag );
    void AppendMoney( Money aMoney );
    Money MoneyBag::Multiply( int factor ) override;

    std::string MoneyBag::to_string()
    {
        std::string buffer { "{" };
        std::vector<Money>::iterator m_Iter;
        for (; m_Iter != fMonies.end(); m_Iter++) {
            buffer.append( (*m_Iter).ToString());
        }
        buffer.append( "}" );
        return buffer;
    }

};

