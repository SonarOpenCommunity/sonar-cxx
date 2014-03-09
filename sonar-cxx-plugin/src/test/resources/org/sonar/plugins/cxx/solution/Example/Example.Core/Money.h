#pragma once
#include <string>
class MoneyBag;

class Money
{
private:
    int fAmount;
    std::string fCurrency;
public:
    Money();

    // copy ctor
    Money( const Money& obj ) { 
        fAmount = obj.fAmount;
        fCurrency = obj.fCurrency;
    };

    // ctor with money and currency
    Money( int amount, std::string currency );

    /// <summary>Adds a money to this money.</summary>
    Money Add( Money m );

    // Assignment operator 
    Money &Money::operator=(Money &ptRHS) {
        fAmount = ptRHS.fAmount;
        fCurrency = ptRHS.fCurrency;
        return *this;  // Assignment operator returns left side.
    }

    // += operator
    Money & Money::operator+=(const Money &rhs) {
        if (fCurrency == rhs.fCurrency)
        {
          fAmount += rhs.fAmount;
          return *this;      
        }
    };
    
    // + operator
    const Money Money::operator+(const Money &other) const {
        Money result = *this;     // Make a copy of myself.  Same as MyClass result(*this);
        result += other;            // Use += to add other to the copy.
        return result;              // All done!
    }

    bool Money::operator==(const Money &other) const {
      // Compare the values, and return a bool result.
        if ((fAmount == other.fAmount) && (fCurrency == other.fCurrency)) 
            return true;
        return false;
    }
    /// <summary>Adds a simple Money to this money. This is a helper method for
    /// implementing double dispatch.</summary>
    MoneyBag AddMoney( Money m );

    /// <summary>Adds a MoneyBag to this money. This is a helper method for
    /// implementing double dispatch.</summary>
    MoneyBag AddMoneyBag( MoneyBag s );

        /// <summary>Multiplies a money by the given factor.</summary>
    virtual Money Multiply( int factor );

    /// <summary>Negates this money.</summary>
    Money Negate();

    /// <summary>Subtracts a money from this money.</summary>
    Money Subtract( Money m );

    int Amount() const;
 
    std::string Currency() const;
    
    virtual bool IsZero(void);

    virtual std::string ToString();

    virtual ~Money();
};

