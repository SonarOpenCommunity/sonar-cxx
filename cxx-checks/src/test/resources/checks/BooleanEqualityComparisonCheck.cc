// BooleanEqualityComparisonCheck
// java plugin check V1.5  "S1125"

bool foo(bool par) {
	return par;
}

int main(void)
{

	auto var = false;
	var == false;       // Non-Compliant
	var == true;        // Non-Compliant
	var != false;       // Non-Compliant
	var != true;        // Non-Compliant
	false == var;       // Non-Compliant
	true == var;        // Non-Compliant
	false != var;       // Non-Compliant
	true != var;        // Non-Compliant

	var == foo(true);   // Compliant
}


