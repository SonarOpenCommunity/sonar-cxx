/*
 * Copyright (C) 2013 Günter Wirth ETAS GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02
 */

#pragma once
#ifndef VS2010_MACROS_h__
#define VS2010_MACROS_h__

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// @TODO: Add your preprocessor definitions here (Project\Properties\Configuration Properties\C/C++\Preprocessor).
//        Have also a look to the 'Inherited values' (Edit\Inherited values)!
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// 1) Is your project an application (none), a DLL(_DLL) or a static library (_LIB)?
#define _DLL 1
//#define _LIB 1

// 2) Is your project a Windows (_WINDOWS) or Console (_CONSOLE) application?
#define _WINDOWS 1
//#define _CONSOLE 1

// 3) Does your project use Unicode (UNICODE) or Multi-Byte (_MBCS) character set
#define UNICODE 1
#define _UNICODE 1
//#define _MBCS 1

// 4) Are you using the MFC?
//    Build Settings for an MFC DLL
//    - Regular, statically linked to MFC: _WINDLL, _USRDLL
//    - Regular, using the shared MFC DLL: _WINDLL, _USRDLL, _AFXDLL
//    - Extension DLL: _WINDLL, _AFXDLL, _AFXEXT
//#define _WINDLL 1
//#define _USRDLL 1
#define _AFXDLL 1
//#define _AFXEXT 1

// 5) Are you using the ATL?
//    - Dynamic Link to ATL: _ATL_DLL
//    - Static Link to ATL: _ATL_STATIC_REGISTRY
#define _ATL_DLL 1
//#define _ATL_STATIC_REGISTRY 1

// 6) Add your other preprocessor definitions here
// ...

// disable assert for production version
#define NDEBUG 1

// STRICT Type Checking: 
#define STRICT 1

// Defines the MFC version.
#if defined(_AFXDLL) || defined(_USRDLL)
#define _MFC_VER 0x0A00
#endif

// Defines the ATL version.
#if defined(_ATL_DLL) || defined(_ATL_STATIC_REGISTRY) || defined(_MFC_VER)
#define _ATL_VER 0x0A00
#endif


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// CXX plugin Predefined Macros V0.9
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//#define __DATE__ "??? ?? ????"
//#define __FILE__ "file"
//#define __LINE__ 1
//#define __TIME__ "??:??:??"
//#define __cplusplus 1
#undef __cplusplus
//#define __STDC__ 1
#undef __STDC__
//#define __STDC_HOSTED__ 1
#undef __STDC_HOSTED__


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ANSI-Compliant Predefined Macros
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// The compilation date of the current source file. The date is a string literal
// of the form Mmm dd yyyy. The month name Mmm is the same as for dates generated
// by the library function asctime declared in TIME.H.
//#define __DATE__ "??? ?? ????"

// The name of the current source file. __FILE__ expands to a string surrounded
// by double quotation marks. To ensure that the full path to the file is
// displayed, use /FC (Full Path of Source Code File in Diagnostics). 
//#define __FILE__ "file"

// The line number in the current source file. The line number is a decimal
// integer constant. It can be changed with a #line directive. 
//#define __LINE__ 1

// Indicates full conformance with the ANSI C standard. Defined as the integer
// constant 1 only if the /Za compiler option is given and you are not compiling
// C++ code; otherwise is undefined. 
//#define __STDC__ 1

// The most recent compilation time of the current source file. The time is a
// string literal of the form hh:mm:ss. 
//#define __TIME__ "??:??:??"

// The date and time of the last modification of the current source file,
// expressed as a string literal in the form Ddd Mmm Date hh:mm:ss yyyy, where
// Ddd is the abbreviated day of the week and Date is an integer from 1 to 31.
#define __TIMESTAMP__ __DATE__ __TIME__


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Microsoft-Specific Predefined Macros
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// Defined when /arch:AVX is specified.
//#define __AVX__ 

// Default char type is unsigned. Defined when /J is specified. 
//#define _CHAR_UNSIGNED 1

// Defines the version of the common language runtime used when the application
// was compiled. 
//#define __CLR_VER 

// Defined when you compile with /clr, /clr:pure, or /clr:safe.
//#define __cplusplus_cli 200406

// Defined when you use the /ZW option to compile.
//#define __cplusplus_winrt 201009

// Expands to an integer starting with 0 and incrementing by 1 every time it is
// used in a source file or included headers of the source file.
#define __COUNTER__ __LINE__

// Defined for C++ programs only.
#define __cplusplus 199711L

// Defined for code compiled with /GR (Enable Run-Time Type Information).
#define _CPPRTTI 1

// Defined for code compiled by using one of the /EH (Exception Handling Model) flags.
#define _CPPUNWIND 1

// Defined when you compile with /LDd, /MDd, and /MTd.
//#define _DEBUG 1

// Valid only in a function. Defines the undecorated name of the enclosing function as a string.
#define __FUNCTION__ "function"
#define __FUNCTIONW__ L"function"

// Valid only in a function. Defines the decorated name of the enclosing function as a string.
#define __FUNCDNAME__ __FUNCTION__

// Valid only in a function. Defines the signature of the enclosing function as a string.
// __FUNCSIG__ is not expanded if you use the /EP or /P compiler option.
// On a 64-bit operating system, the calling convention is __cdecl by default.
#define __FUNCSIG__ __FUNCTION__

// Reports the maximum size (in bits) for an integral type.
#define _INTEGRAL_MAX_BITS 64

// Defined for DEC ALPHA platforms (no longer supported).
//#define _M_ALPHA 

// Defined for x64 processors.
//#define _M_AMD64 

// Defined for a compilation that uses any form of /clr (/clr:oldSyntax, /clr:safe, for example).
//#define _M_CEE 

// Defined for a compilation that uses /clr:pure.
//#define _M_CEE_PURE 

// Defined for a compilation that uses /clr:safe.
//#define _M_CEE_SAFE 

// Defined for x86 processors. See the Values for _M_IX86 table below for more
// information. This is not defined for x64 processors.
#define _M_IX86 600

// Defined for Itanium Processor Family 64-bit processors.
//#define _M_IA64 

// Expands to a value indicating which /arch compiler option was used.
//#define _M_ARM_FP 

// Expands to a value indicating which /arch compiler option was used.
#define _M_IX86_FP 0

// Defined for Power Macintosh platforms (no longer supported).
//#define _M_MPPC 

// Defined for MIPS platforms (no longer supported).
//#define _M_MRX000 

// Defined for PowerPC platforms (no longer supported).
//#define _M_PPC 

// Defined for x64 processors.
//#define _M_X64 

// Defined to be 1 when /clr is specified.
//#define _MANAGED 1

// Evaluates to the revision number component of the compiler's version number.
// The revision number is the fourth component of the period-delimited version number.
// For example, if the version number of the Visual C++ compiler is 15.00.20706.01,
// the _MSC_BUILD macro evaluates to 1.
#define _MSC_BUILD 1

// This macro is defined when you compile with the /Ze compiler option (the default).
// Its value, when defined, is 1.
#define _MSC_EXTENSIONS 1

// Evaluates to the major, minor, and build number components of the compiler's version number.
#define _MSC_FULL_VER 160040219

// Evaluates to the major and minor number components of the compiler's version number. 
#define _MSC_VER 1600

// Defined when one of the /RTC compiler options is specified.
#define __MSVC_RUNTIME_CHECKS 1

// Defined when /MD or /MDd (Multithreaded DLL) or /MT or /MTd (Multithreaded) is specified.
#define _MT 1

// Defined when /Zc:wchar_t is used.
#define _NATIVE_WCHAR_T_DEFINED 1

// Defined when compiling with /openmp, returns an integer representing
// the date of the OpenMP specification implemented by Visual C++.
//#define _OPENMP 200203

// Defined when /Zl is used; see /Zl (Omit Default Library Name) for more information.
//#define _VC_NODEFAULTLIB 

// Defined when /Zc:wchar_t is used or if wchar_t is defined in a system header file
// included in your project.
#define _WCHAR_T_DEFINED 1

// Determines the minimum platform SDK required to build your application.
#define WINVER 0x0600
#define _WIN32_WINNT 0x0600
#define NTDDI_VERSION 0x06000000

// Defined for applications for Win32 and Win64. Always defined.
#define WIN32 1
#define _WIN32 1
#define __WIN32 1

// Defined for applications for Win64.
//#define _WIN64 

// Defined when specifying /Wp64.
//#define _Wp64 

// makers for documenting the semantics of APIs (sal.h)
#define _USE_DECLSPECS_FOR_SAL 0
//#define SAL_NO_ATTRIBUTE_DECLARATIONS


//
// Microsoft-Specific Modifiers V0.4
//

// based addressing
//
#define _based(type)
#define __based(type)

// function calling conventions 
//
#define cdecl
#define _cdecl
#define __cdecl
#define _clrcall
#define __clrcall
#define _stdcall
#define __stdcall
#define _fastcall
#define __fastcall
#define __thiscall
#define __vectorcall

// obsolete calling conventions
//
#define pascal
#define _pascal
#define __pascal
#define __fortran
#define _syscall
#define __syscall
#define _oldcall
#define __oldcall
#define __loadds

// inline
//
#define _inline inline
#define __inline inline
#define _forceinline inline
#define __forceinline inline

// extended storage-class attributes 
//
#define _declspec(...)
#define __declspec(...)

// similar to __declspec(restrict), but for use on variable
//
#define __restrict

// similar to __declspec(export)
//
#define __export

// __w64 keyword
//
#define _w64
#define __w64

// specifies that a pointer to a type or other data is not aligned
//
#define _unaligned
#define __unaligned

// use the __sptr or __uptr modifier on a 32-bit pointer declaration to specify how the compiler converts a 32-bit pointer to a 64-bit pointer
// 
#define __sptr
#define __uptr

// types
//
#define _int8 char 
#define __int8 char 
#define _int16 short 
#define __int16 short 
#define _int32 int 
#define __int32 int 
#define _int64 long long 
#define __int64 long long 
#define __ptr32
#define __ptr64
#define __wchar_t wchar_t
#define __handle

// is defined in xmmintrin.h
//#define __m64 ...
//#define __m128 ...

// is defined in emmintrin.h
//#define __m128d ...
//#define __m128i ...

// are applicable for 16 bit programs and are ignored by default in compilations using the Win32 memory model
//
#define far
#define _far
#define __far
#define near
#define _near
#define __near
#define __huge
#define __cs
#define __ss

// assembly language instruction
//
#define _asm asm
#define __asm asm

// alignment requirement of the type
//
#define __alignof(type) 1

// try/except statement
//
#define __try try
#define __except(...) catch(int e)
#define __finally catch(...) {}
#define __leave

// conditionally include code depending on whether the specified symbol exists
//
#define __if_exists(v)
#define __if_not_exists(v)

// other keywords
//
#define __assume(a)
#define __uuidof(X) IID_IUnknown
#define __noop (void(0))
#define __pragma(a)
#define __super
#define __debug
#define __emit__(...)

// compiler support for type traits (C++ Component Extensions)
//
#define __has_assign(type) false
#define __has_copy(type) false
#define __has_finalizer(type) false
#define __has_nothrow_assign(type) false
#define __has_nothrow_constructor(type) false
#define __has_nothrow_copy(type) false
#define __has_trivial_assign(type) false
#define __has_trivial_constructor(type) false
#define __has_trivial_copy(type) false
#define __has_trivial_destructor(type) false
#define __has_user_destructor(type) false
#define __has_virtual_destructor(type) false
#define __is_abstract(type) false
#define __is_base_of(base, derived) false
#define __is_class(type) false
#define __is_convertible_to(from, to) false
#define __is_delegate(type) false
#define __is_empty(type) false
#define __is_enum(type) false
#define __is_interface_class(type) false
#define __is_pod(type) false
#define __is_polymorphic(type) false
#define __is_ref_array(type) false
#define __is_ref_class(type) false
#define __is_sealed(type) false
#define __is_simple_value_class(type) false
#define __is_union(type) false
#define __is_value_class(type) false

// inheritance keywords
//
#define _single_inheritance 
#define __single_inheritance 
#define _multiple_inheritance 
#define __multiple_inheritance 
#define _virtual_inheritance 
#define __virtual_inheritance 

// Managed Extensions for C++ Programming.
//
#define __abstract
#define __box(ident) &(ident)
#define __delegate
#define __event
#define __gc
#define __identifier(keyword) identifier_##keyword
#define __interface class
#define __nogc
#define __pin
#define __property
#define __sealed
#define __try_cast dynamic_cast
#define __typeof(type) System::type::GetType() 
#define __value
#define __raise
#define __hook(...)
#define __unhook(...)

#endif // VS2010_MACROS_h__
