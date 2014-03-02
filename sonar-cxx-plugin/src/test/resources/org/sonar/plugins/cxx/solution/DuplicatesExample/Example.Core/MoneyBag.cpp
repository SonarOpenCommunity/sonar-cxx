#include "stdafx.h"
#include "MoneyBag.h"

#include <algorithm>



MoneyBag::MoneyBag()
{
    std::vector<Money> fMonies {};
}


MoneyBag::~MoneyBag()
{
}

MoneyBag::MoneyBag( std::vector<MoneyBag> bag )
{
    for (unsigned int i = 0; i < bag.size() ; i++)
    {
        if (!bag[i].IsZero())
            AppendMoney( bag[i] );
    }
}

MoneyBag::MoneyBag( Money m1, Money m2 )
{
    AppendMoney( m1 );
    AppendMoney( m2 );
}

MoneyBag::MoneyBag( Money m, MoneyBag bag )
{
    AppendMoney( m );
    AppendBag( bag );
}

MoneyBag::MoneyBag( MoneyBag m1, MoneyBag m2 )
{
    AppendBag( m1 );
    AppendBag( m2 );
}

Money MoneyBag::Add( Money m )
{
    return m.AddMoneyBag( *this );
}

Money MoneyBag::AddMoney( Money m )
{
    return (MoneyBag( m, *this ));
}

Money MoneyBag::AddMoneyBag( const MoneyBag s )
{
    return ( MoneyBag( s, *this ));
}

void MoneyBag::AppendBag( const MoneyBag aBag )
{
    for( auto m : aBag.fMonies )
        AppendMoney( m );
}

void MoneyBag::AppendMoney( const Money aMoney )
{
    std::vector<Money>::iterator m_Iter; 
    bool found = false;
    for (; m_Iter != fMonies.end(); m_Iter++) {
        if ((*m_Iter).Currency() == aMoney.Currency())
        {
            (*m_Iter).AddMoney(aMoney);
            found = true;
        }
    }
    if (!found) {
         fMonies.push_back( aMoney );
    }
}

bool MoneyBag::Contains( const Money aMoney )
{
    std::vector<Money>::iterator m_Iter = FindMoney( aMoney.Currency() );
    return (*m_Iter).Amount() == aMoney.Amount();
}

std::vector<Money>::iterator MoneyBag::FindMoney( std::string currency )
{
    std::vector<Money>::iterator m_Iter;
    for (; m_Iter != fMonies.end(); m_Iter++) {
        if ((*m_Iter).Currency() == currency )
            return m_Iter;
    }
    // not found returns begin()
    return fMonies.begin();
}






