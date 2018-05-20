3: enum Direction
        !!!!!!!!!
5:   Forward, Reverse
     !!!!!!!
5:   Forward, Reverse
              !!!!!!!
8: enum Velocity
        !!!!!!!!
10:   Accelerate, Decelerate
      !!!!!!!!!!
10:   Accelerate, Decelerate
                  !!!!!!!!!!
13: static int global1 = 0;
               !!!!!!!
16: class CMotorController
          !!!!!!!!!!!!!!!!
19:   int speed;
          !!!!!
20:   Direction direction;
                !!!!!!!!!
21:   bool limiter;
           !!!!!!!
22:   Velocity velocity;
               !!!!!!!!
25:   CMotorController( void );
      !!!!!!!!!!!!!!!!
27:   void setSpeed( int speed );
           !!!!!!!!
27:   void setSpeed( int speed );
                         !!!!!
28:   int getSpeed();
          !!!!!!!!
29:   void checkSpeed();
           !!!!!!!!!!
30:   bool getLimiter();
           !!!!!!!!!!
31:   Velocity getVelocity();
               !!!!!!!!!!!
33:   void setDirection( Direction direction );
           !!!!!!!!!!!!
33:   void setDirection( Direction direction );
                                   !!!!!!!!!
34:   Direction getDirection();
                !!!!!!!!!!!!
39: constexpr int factorial( int n )
                  !!!!!!!!!
39: constexpr int factorial( int n )
                                 !
44: CMotorController::CMotorController() :
    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
44: CMotorController::CMotorController() :
                      !!!!!!!!!!!!!!!!
49: void CMotorController::setSpeed( int speed )
         !!!!!!!!!!!!!!!!!!!!!!!!!!
49: void CMotorController::setSpeed( int speed )
                           !!!!!!!!
49: void CMotorController::setSpeed( int speed )
                                         !!!!!
54: int CMotorController::getSpeed()
        !!!!!!!!!!!!!!!!!!!!!!!!!!
54: int CMotorController::getSpeed()
                          !!!!!!!!
59: void CMotorController::setDirection( Direction direction )
         !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
59: void CMotorController::setDirection( Direction direction )
                           !!!!!!!!!!!!
59: void CMotorController::setDirection( Direction direction )
                                                   !!!!!!!!!
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
         !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
                           !!!!!!!!!!!!!!!!!!!!
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
                                                           !!!!!!!!!
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
                                                                          !!!!!
70: Direction CMotorController::getDirection()
              !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
70: Direction CMotorController::getDirection()
                                !!!!!!!!!!!!
75: bool CMotorController::getLimiter()
         !!!!!!!!!!!!!!!!!!!!!!!!!!!!
75: bool CMotorController::getLimiter()
                           !!!!!!!!!!
80: Velocity CMotorController::getVelocity()
             !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
80: Velocity CMotorController::getVelocity()
                               !!!!!!!!!!!
85: void CMotorController::checkSpeed()
         !!!!!!!!!!!!!!!!!!!!!!!!!!!!
85: void CMotorController::checkSpeed()
                           !!!!!!!!!!
88:   int test1 = global1;
          !!!!!
108: template< typename T >
                        !
109: T* factoryFunction()
        !!!!!!!!!!!!!!!
114: void main()
          !!!!
