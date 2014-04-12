#!/usr/bin/perl

my $INFILE = $ARGV[0];
my $OUTFILE = $ARGV[1];

if (@ARGV != 2)
{
	print "ERROR - invalid arguments\n";
	exit 0;
}
open(READFILE, "<", $INFILE) or die "Cannot open '$INFILE': $!";
open(OUTFILE, ">", $OUTFILE) or die "OMG $!";

print OUTFILE "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
print OUTFILE "<results>\n";
my $line="";
while($line = <READFILE>) {
	chomp $line;

	my @elems = split(":", $line);
	if (@elems > 2) {
		# break string in char and start packing the data
		my $rest = "";
		my $id = "";
		my $file = "";
		my $linenum = "";
		my $msg = "";
		my $ruleid = "";
		my $severity = "style";
	
		@array = split(//, $line);
		my $it=0;
		#print "array @array linr $line\n";
		foreach $char (@array) {		
			if ( $char eq ":" ) {
				$it=$it+1;
			} else {
				if ($it == 0) {
					$file="$file$char";
				}
				if ($it == 1) {
					$linenum="$linenum$char";
				}
				if ($it > 1) {
					$rest="$rest$char";
				}					
			}		
		}
		
		@array = split(//, $rest);		
		#find rule id
		my $rulenotfound == 0;
		my $index = @array - 1;
		my $cntclend = 0;
		my $cntclstart = 0;
		my $msgindexend = 0;
		my $msgindexstart = 0;
		while( $rulenotfound == 0 and $index > 0) {		
			if ( $array[$index] eq "]" ) {
				$cntclend=$cntclend+1;
			}			
			if ( $array[$index] eq "[" ) {
				$cntclstart=$cntclstart+1;
			}						
			if ( $cntclstart == 2 ) {
				$msgindexstart=$index;
				$rulenotfound=1;				
			}			
			if ( $cntclend == 2 and $msgindexend == 0) {
				$msgindexend=$index;
			}
			$index=$index-1;
		}
		
		$msg  = substr $rest, 0, $msgindexstart;
		$ruleid  = substr $rest, $msgindexstart+1, $msgindexend-$msgindexstart-1;
		
		$msg =~ s/^\s+//; #remove leading spaces
		$msg =~ s/\s+$//; #remove trailing spaces
		
		$msg =~ s/&/&amp;/g;
		$msg =~ s/</&lt;/g;
		$msg =~ s/>/&gt;/g;
		$msg =~ s/"/&quot;/g;		
		$msg =~ s/'/&apos;/g;		
		
		my $line = "\t\t<error file=\"$file\" line=\"$linenum\" id=\"$ruleid\"  msg=\"$msg\"/>\n";
		

		print OUTFILE $line;		
	}
}
print OUTFILE "</results>\n";

close(READFILE);
close(LOGFILE);