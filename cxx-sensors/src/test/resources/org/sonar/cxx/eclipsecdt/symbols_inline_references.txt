DECLARATION:
3: enum Direction
        !!!!!!!!!
REFERENCES:
20:   Direction direction;
      ^^^^^^^^^
33:   void setDirection( Direction direction );
                         ^^^^^^^^^
34:   Direction getDirection();
      ^^^^^^^^^
59: void CMotorController::setDirection( Direction direction )
                                         ^^^^^^^^^
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
                                                 ^^^^^^^^^
70: Direction CMotorController::getDirection()
    ^^^^^^^^^
116:   factoryFunction< CMotorController >()->setDirection( Direction::Forward );
                                                            ^^^^^^^^^


DECLARATION:
5:   Forward, Reverse
     !!!!!!!
REFERENCES:
45:     speed( 0 ), direction( Forward ), limiter( false ), velocity( Decelerate )
                               ^^^^^^^
116:   factoryFunction< CMotorController >()->setDirection( Direction::Forward );
                                                                       ^^^^^^^


DECLARATION:
5:   Forward, Reverse
              !!!!!!!
REFERENCES:


DECLARATION:
8: enum Velocity
        !!!!!!!!
REFERENCES:
22:   Velocity velocity;
      ^^^^^^^^
31:   Velocity getVelocity();
      ^^^^^^^^
80: Velocity CMotorController::getVelocity()
    ^^^^^^^^


DECLARATION:
10:   Accelerate, Decelerate
      !!!!!!!!!!
REFERENCES:
94:     velocity = Accelerate;
                   ^^^^^^^^^^


DECLARATION:
10:   Accelerate, Decelerate
                  !!!!!!!!!!
REFERENCES:
45:     speed( 0 ), direction( Forward ), limiter( false ), velocity( Decelerate )
                                                                      ^^^^^^^^^^
99:     velocity = Decelerate;
                   ^^^^^^^^^^


DECLARATION:
13: static int global1 = 0;
               !!!!!!!
REFERENCES:
87:   global1 = 1;
      ^^^^^^^
88:   int test1 = global1;
                  ^^^^^^^


DECLARATION:
16: class CMotorController
          !!!!!!!!!!!!!!!!
REFERENCES:
44: CMotorController::CMotorController() :
    ^^^^^^^^^^^^^^^^
49: void CMotorController::setSpeed( int speed )
         ^^^^^^^^^^^^^^^^
54: int CMotorController::getSpeed()
        ^^^^^^^^^^^^^^^^
59: void CMotorController::setDirection( Direction direction )
         ^^^^^^^^^^^^^^^^
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
         ^^^^^^^^^^^^^^^^
70: Direction CMotorController::getDirection()
              ^^^^^^^^^^^^^^^^
75: bool CMotorController::getLimiter()
         ^^^^^^^^^^^^^^^^
80: Velocity CMotorController::getVelocity()
             ^^^^^^^^^^^^^^^^
85: void CMotorController::checkSpeed()
         ^^^^^^^^^^^^^^^^
116:   factoryFunction< CMotorController >()->setDirection( Direction::Forward );
                        ^^^^^^^^^^^^^^^^


DECLARATION:
19:   int speed;
          !!!!!
REFERENCES:
45:     speed( 0 ), direction( Forward ), limiter( false ), velocity( Decelerate )
        ^^^^^
51:   this->speed = speed;
            ^^^^^
56:   return speed;
             ^^^^^
67:   this->speed = speed;
            ^^^^^
90:   switch ( speed )
               ^^^^^


DECLARATION:
20:   Direction direction;
                !!!!!!!!!
REFERENCES:
45:     speed( 0 ), direction( Forward ), limiter( false ), velocity( Decelerate )
                    ^^^^^^^^^
61:   this->direction = direction;
            ^^^^^^^^^
66:   this->direction = direction;
            ^^^^^^^^^
72:   return direction;
             ^^^^^^^^^


DECLARATION:
21:   bool limiter;
           !!!!!!!
REFERENCES:
45:     speed( 0 ), direction( Forward ), limiter( false ), velocity( Decelerate )
                                          ^^^^^^^
77:   return limiter;
             ^^^^^^^
93:     limiter = false;
        ^^^^^^^
98:     limiter = true;
        ^^^^^^^


DECLARATION:
22:   Velocity velocity;
               !!!!!!!!
REFERENCES:
45:     speed( 0 ), direction( Forward ), limiter( false ), velocity( Decelerate )
                                                            ^^^^^^^^
94:     velocity = Accelerate;
        ^^^^^^^^
99:     velocity = Decelerate;
        ^^^^^^^^


DECLARATION:
25:   CMotorController( void );
      !!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
27:   void setSpeed( int speed );
           !!!!!!!!
REFERENCES:


DECLARATION:
27:   void setSpeed( int speed );
                         !!!!!
REFERENCES:
51:   this->speed = speed;
                    ^^^^^


DECLARATION:
28:   int getSpeed();
          !!!!!!!!
REFERENCES:


DECLARATION:
29:   void checkSpeed();
           !!!!!!!!!!
REFERENCES:


DECLARATION:
30:   bool getLimiter();
           !!!!!!!!!!
REFERENCES:


DECLARATION:
31:   Velocity getVelocity();
               !!!!!!!!!!!
REFERENCES:


DECLARATION:
33:   void setDirection( Direction direction );
           !!!!!!!!!!!!
REFERENCES:
116:   factoryFunction< CMotorController >()->setDirection( Direction::Forward );
                                              ^^^^^^^^^^^^


DECLARATION:
33:   void setDirection( Direction direction );
                                   !!!!!!!!!
REFERENCES:
61:   this->direction = direction;
                        ^^^^^^^^^


DECLARATION:
34:   Direction getDirection();
                !!!!!!!!!!!!
REFERENCES:


DECLARATION:
39: constexpr int factorial( int n )
                  !!!!!!!!!
REFERENCES:
41:   return n <= 1 ? 1 : ( n * factorial( n - 1 ) );
                                ^^^^^^^^^
103:     factorial( 4 );
         ^^^^^^^^^


DECLARATION:
39: constexpr int factorial( int n )
                                 !
REFERENCES:
41:   return n <= 1 ? 1 : ( n * factorial( n - 1 ) );
             ^
41:   return n <= 1 ? 1 : ( n * factorial( n - 1 ) );
                            ^
41:   return n <= 1 ? 1 : ( n * factorial( n - 1 ) );
                                           ^


DECLARATION:
44: CMotorController::CMotorController() :
    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
44: CMotorController::CMotorController() :
                      !!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
49: void CMotorController::setSpeed( int speed )
         !!!!!!!!!!!!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
49: void CMotorController::setSpeed( int speed )
                           !!!!!!!!
REFERENCES:


DECLARATION:
49: void CMotorController::setSpeed( int speed )
                                         !!!!!
REFERENCES:
51:   this->speed = speed;
                    ^^^^^


DECLARATION:
54: int CMotorController::getSpeed()
        !!!!!!!!!!!!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
54: int CMotorController::getSpeed()
                          !!!!!!!!
REFERENCES:


DECLARATION:
59: void CMotorController::setDirection( Direction direction )
         !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
REFERENCES:
116:   factoryFunction< CMotorController >()->setDirection( Direction::Forward );
                                              ^^^^^^^^^^^^


DECLARATION:
59: void CMotorController::setDirection( Direction direction )
                           !!!!!!!!!!!!
REFERENCES:
116:   factoryFunction< CMotorController >()->setDirection( Direction::Forward );
                                              ^^^^^^^^^^^^


DECLARATION:
59: void CMotorController::setDirection( Direction direction )
                                                   !!!!!!!!!
REFERENCES:
61:   this->direction = direction;
                        ^^^^^^^^^


DECLARATION:
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
         !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
                           !!!!!!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
                                                           !!!!!!!!!
REFERENCES:
66:   this->direction = direction;
                        ^^^^^^^^^


DECLARATION:
64: void CMotorController::setDirectionAndSpeed( Direction direction, int speed )
                                                                          !!!!!
REFERENCES:
67:   this->speed = speed;
                    ^^^^^


DECLARATION:
70: Direction CMotorController::getDirection()
              !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
70: Direction CMotorController::getDirection()
                                !!!!!!!!!!!!
REFERENCES:


DECLARATION:
75: bool CMotorController::getLimiter()
         !!!!!!!!!!!!!!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
75: bool CMotorController::getLimiter()
                           !!!!!!!!!!
REFERENCES:


DECLARATION:
80: Velocity CMotorController::getVelocity()
             !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
80: Velocity CMotorController::getVelocity()
                               !!!!!!!!!!!
REFERENCES:


DECLARATION:
85: void CMotorController::checkSpeed()
         !!!!!!!!!!!!!!!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
85: void CMotorController::checkSpeed()
                           !!!!!!!!!!
REFERENCES:


DECLARATION:
88:   int test1 = global1;
          !!!!!
REFERENCES:


DECLARATION:
108: template< typename T >
                        !
REFERENCES:
109: T* factoryFunction()
     ^
111:   return new T();
                  ^


DECLARATION:
109: T* factoryFunction()
        !!!!!!!!!!!!!!!
REFERENCES:


DECLARATION:
114: void main()
          !!!!
REFERENCES:


