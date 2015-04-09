/*
 * Copyright (C) 2015 Arnold Metselaar
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

// Macro definitions to help parse g++ programs with sonar-cxx

// Definions for predefined macros may be obtained with:
// echo | cpp -x c++ -dM -

// Directories searched by default for include files may be obtained with:
// echo | cpp -v -x c++ - | grep -e "^ "

#pragma once
#ifndef _GXX_MACROS_H_
#define _GXX_MACROS_H_

// GCC extension keywords and alternate forms
#define _Complex
#define __FUNCTION__ "function"
#define __PRETTY_FUNCTION__ "pretty_function"
#define __alignof __alignof__
#define __alignof__(x) sizeof(char)
#define __asm asm
#define __asm__ asm
#define __attribute__(...)
#define __attribute __attribute__
#define __complex__ _Complex
#define __const const
#define __const__ const
#define constexpr const
#define __declspec(...)
#define __decltype decltype
#define __extension__
#define __func__ "func"
#define __imag__
#define __inline inline
#define __inline__ inline
#define __null 0
#define __real__
#define __restrict
#define __restrict__
#define __signed signed
#define __signed__ signed
#define __thread
#define typeof decltype
#define __typeof decltype
#define __typeof__ decltype
#define __volatile volatile
#define __volatile__ volatile

#endif
