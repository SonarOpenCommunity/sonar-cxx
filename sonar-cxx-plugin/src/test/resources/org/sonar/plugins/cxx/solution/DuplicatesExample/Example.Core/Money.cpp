#include "stdafx.h"
#include "Money.h"


Money::Money()
{
}

Money::Money( int amount, std::string currency )
{
    fAmount = amount;
    fCurrency = currency;
}

Money::~Money()
{
}

Money Money::AddMoney( Money m )
{
    if (m.Currency() == fCurrency)
        fAmount += m.Amount();
    return *this;

}

int Money::Amount() const
{
    return fAmount; 
}

std::string Money::Currency() const
{
    return fCurrency; 
}

bool Money::IsZero()
{
    return fAmount == 0; 
}

/// <summary>Adds a money to this money. Forwards the request to
/// the AddMoney helper.</summary>
Money Money::Add( Money m )
{
    return m.AddMoney( m );
}

Money Money::Multiply( int factor )
{
    // We compute the new amount
    return  Money( fAmount * factor, fCurrency );
}

Money Money::Negate()
{
    // A new negative money is generated
    return Money( -fAmount, fCurrency );
}

Money Money::Subtract( Money m )
{
    return Money( fAmount -= m.fAmount, fCurrency );
}

std::string Money::ToString()
{
    // This is a simple comment for test
    std::string buffer("[" + fAmount + fCurrency + "]");
    // We build the string representation
    return buffer;
}
