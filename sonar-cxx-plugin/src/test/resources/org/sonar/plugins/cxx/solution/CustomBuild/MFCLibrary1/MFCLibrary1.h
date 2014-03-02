// MFCLibrary1.h : main header file for the MFCLibrary1 DLL
//

#pragma once

#ifndef __AFXWIN_H__
	#error "include 'stdafx.h' before including this file for PCH"
#endif

#include "resource.h"		// main symbols


// CMFCLibrary1App
// See MFCLibrary1.cpp for the implementation of this class
//

class CMFCLibrary1App : public CWinApp
{
public:
	CMFCLibrary1App();

// Overrides
public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};
