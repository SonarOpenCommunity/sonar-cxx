#include "stdafx.h"
#include "VC14.h"


struct ClientIdentifier {
	long unique_id;
	std::string client_name;

	ClientIdentifier() : unique_id(-1), client_name("") {};
};

void ClientContextRegistry::onChanged(const ClientIdentifier & aIdentifier_in)
{
	std::cout << aIdentifier_in.unique_id << std::endl;
}

bool PropertyEdit::CheckForCharactersInString(const std::string& str_in)
{
	int aLen = str_in.length;
	for (int i = 0; i<aLen; i++)
	{
		int aCharVal = str_in[i];
		if (aCharVal < 45 || aCharVal > 57)
			return false;
	}

	return true;
}




