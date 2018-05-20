// MotorController.h

enum Direction
{
  Forward, Reverse
};

enum Velocity
{
  Accelerate, Decelerate
};

extern int global1 = 0;

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
