// MotorController.h

enum Direction
{
  Forward, Reverse
};

enum Velocity
{
  Accelerate, Decelerate
};

static int global1 = 0;

// This class is exported from the MotorController.dll
class CMotorController
{
private:
  int speed;
  Direction direction;
  bool limiter;
  Velocity velocity;

public:
  CMotorController( void );

  void setSpeed( int speed );
  int getSpeed();
  void checkSpeed();
  bool getLimiter();
  Velocity getVelocity();

  void setDirection( Direction direction );
  Direction getDirection();
};

// MotorController.cpp : Defines the exported functions for the DLL application.

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

