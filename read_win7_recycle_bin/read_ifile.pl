#!/usr/bin/env perl
use 5.018;
use autodie;

my $fname = shift;
open my $fh, '<:raw', $fname;

my $unixdiff = 11644473600;

my $bytes_read = read $fh, my $bytes, 544;
die 'not enough bytes' unless $bytes_read == 544;

my ($hdr, $ofs, $ts, $name) = unpack 'Q Q Q a520', $bytes;
my ($day, $month, $year) = (localtime($ts/10000000 - $unixdiff))[3,4,5];
$year = $year + 1900;
$month = $month + 1;

say "$year-$month-$day\t$fname\t$name";

close $fh;
