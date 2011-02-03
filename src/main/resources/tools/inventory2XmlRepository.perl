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
#  __________________________________________________________________________

use strict;

my ($key,$priority,$description);
my $category = "Reliability";
print "<rules>\n";
while (<STDIN>)
{
	chomp $_;

#	if ($_ =~ m/^\(.*,.*\).*$/) { 
#       $_ =~ s/^\((.*), ([A-Z a-z]+)\) (.*)$/\1|||\2|||\3/;
        ($key,$priority,$description) = split(/\|\|\|/, $_);
		if ($priority =~ m/.*style.*/) { $priority = "INFO"; }
		elsif ($priority =~ m/.*warning.*/) { $priority = "MINOR"; }
		elsif ($priority =~ m/.*error.*/) { $priority = "MAJOR"; }
		elsif ($priority =~ m/.*critical.*/) { $priority = "CRITICAL"; }
		else { $priority = "MINOR"; }
		print "\t<rule>\n";
		print "\t\t<key>$key</key>\n";
		print "\t\t<priority>$priority</priority>\n";
		print "\t\t<name>$key</name>\n";
		print "\t\t<configKey>$key</configKey>\n";
		print "\t\t<category name=\"$category\"/>\n";
		print "\t\t<description>$description</description>\n";
		print "\t</rule>\n";
 #       }
 }
print "</rules>\n";
