#! /usr/bin/env perl
#  An XML repository generator for C++ check rules 
# Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits réservés.
# Author(s) : Franck Bonin, Neticoa SAS France.
#
#  This Software is free software; you can redistribute it and/or
#  modify it under the terms of the GNU Lesser General Public
#  License as published by the Free Software Foundation; either
#  version 3 of the License, or (at your option) any later version.
# 
#  Sonar is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  Lesser General Public License for more details.
# 
#  You should have received a copy of the GNU Lesser General Public
#  License along with This Software; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
#  _________________________________________________________________________

use strict;

print "<profile>\n";
print "\t<name>Default C++ Profile</name>\n";
print "\t<language>c++</language>\n";
print "\t<rules>\n";
while (<STDIN>)
{
	chomp $_;

	if ($_ =~ m/.*<key>.*/) { 
        	$_ =~ s/.*<key>(.*)<.*/\1/g;
		print "\t\t<rule>\n";
		print "\t\t\t<repositoryKey>c++</repositoryKey>\n";
		print "\t\t\t<key>$_</key>\n";
		print "\t\t</rule>\n";
        }
 }

print "\t</rules>\n";
print "</profile>\n";
