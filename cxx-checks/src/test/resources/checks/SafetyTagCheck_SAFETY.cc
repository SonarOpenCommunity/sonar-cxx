/*[ Compilation unit ----------------------------------------------------------

Component	    : A Server Service
Name            : SafetyTagCheck.cc
Language        : C++
Creation Date   : 30-Dec-2013

Description     : Class has a safety tag
- Risk mitigation implementation shall be defined in separate file
- one RIM per file shall be implemented

Requirement Key :

Copyright (C) Company XYZ, 2013. All Rights Reserved.
-----------------------------------------------------------------------------*/

#include <string>

std::string theStr = "";

/*[ Function ------------------------------------------------------------------
Name            : SomeSetting
Description     : Stores the value
Return          : None
<Safetykey>MyRimName</Safetykey>
-----------------------------------------------------------------------------*/
void SomeSetting( std::string value ) {
    theStr = value;
}
