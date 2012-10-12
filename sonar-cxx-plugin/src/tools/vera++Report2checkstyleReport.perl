#! /usr/bin/env perl
#  An vera++ to checkstyle XML report generator for
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

##my $rapport = $ARGV[0];
##chomp $rapport;
##open (DATAFILE, "$rapport") || die("Can't open $rapport\n");

my ($file,$line,$rule,$comment);

print "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
print "<checkstyle version=\"5.0\">\n";

my $lastfile = "";
while (<STDIN>)
{
	chomp $_;
	## replace '(RULENumber)' with 'RULENumber:'
	$_ =~ s/(.*) \((.*)\) (.*)/\1\2:\3/g;

	($file,$line,$rule,$comment) = split(":", $_);

	my $severity = "error";

	$severity = "warning";
	#if ($rule =~ m/G.*/) { $severity = "ignore"; }
	#if ($rule =~ m/F.*/) { $severity = "info"; }
	#if ($rule =~ m/L.*/) { $severity = "warning"; }
	#if ($rule =~ m/T.*/) { $severity = "error"; }
	if ($file ne $lastfile)
	{
		if ($lastfile ne "")
		{
			print "\t</file>\n";
		}
		print "\t<file name=\"$file\">\n";
		$lastfile = $file;
	}
	print "\t\t<error line=\"$line\" severity=\"$severity\" message=\"$comment\" source=\"$rule\"/>\n";
}
if ($lastfile ne "")
{
	print "\t</file>\n";
}
print "</checkstyle>\n";

