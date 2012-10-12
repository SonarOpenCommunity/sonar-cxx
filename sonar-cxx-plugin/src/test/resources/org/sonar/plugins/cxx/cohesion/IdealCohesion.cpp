//LCOM4 = 1.0
class IdealCohesion 
{
public:
	void 	setMember(int value);
	int		getMember();
	
	float	calculateSum();
	
private:
	int 	member_int;
	float 	member_float;

};

void IdealCohesion::setMember(int value) {
	member_int = value;
}

int	IdealCohesion::getMember() {
	return member_int;
}

float IdealCohesion::calculateSum() {
	return member_int + member_float;
}