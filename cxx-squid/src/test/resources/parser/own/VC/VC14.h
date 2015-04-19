#pragma once
#include <string>

class PropertyEdit {
		PropertyEdit() {};
		~PropertyEdit() {};

		bool CheckForCharactersInString(const std::string & str_in);
};


class ClientContextRegistry {
	ClientContextRegistry() {};
	~ClientContextRegistry() {};

	void onChanged(const ClientIdentifier& aIdentifier_in);


};



