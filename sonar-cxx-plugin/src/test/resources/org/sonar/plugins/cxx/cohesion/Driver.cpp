/**
* example from : http://www.sonarsource.org/clean-up-design-at-class-level-with-sonar/
* LCOM4 = 3
*/

class Driver
{
public:
	void stop() { car.stop(); }
	void drive() { car.drive(); }
	void goTo() { drive(); }
	
	void getAngry() { brain.getAngry(); }
	
	void drinkCoffee() { /* do some other stuff */ }
	
private:
	Car car;
	Brain brain;
};