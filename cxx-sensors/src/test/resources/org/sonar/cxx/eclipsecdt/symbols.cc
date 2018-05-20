#include "symbols.hh"
// this include must be resolved in one of specified include directories

// MotorController.cpp : Defines the exported functions for the DLL application.

int global1 = 0;

constexpr int factorial( int n )
{
  return n <= 1 ? 1 : ( n * factorial( n - 1 ) );
}

CMotorController::CMotorController() :
    speed( 0 ), direction( Forward ), limiter( false ), velocity( Decelerate )
{
}

void CMotorController::setSpeed( int speed )
{
  this->speed = speed;
}

int CMotorController::getSpeed()
{
  return speed;
}

void CMotorController::setDirection( Direction direction )
{
  this->direction = direction;
}

void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
{
  this->direction = direction;
  this->speed = speed;
}

Direction CMotorController::getDirection()
{
  return direction;
}

bool CMotorController::getLimiter()
{
  return limiter;
}

Velocity CMotorController::getVelocity()
{
  return Velocity();
}

void CMotorController::checkSpeed()
{
  global1 = 1;
  int test1 = global1;

  switch ( speed )
  {
  case 10:
    limiter = false;
    velocity = Accelerate;
    break;
  case 50:
  {
    limiter = true;
    velocity = Decelerate;
  }
    break;
  default:
    factorial( 4 );

  }
}

template< typename T >
T* factoryFunction()
{
  return new T();
}

void main()
{
  factoryFunction< CMotorController >()->setDirection( Direction::Forward );
}

