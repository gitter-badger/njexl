#!/usr/bin/perl -w 
print "I am sample perl script!\n" ;
$num_args = $#ARGV + 1;
if ($num_args < 1) {
    print "Usage: sample.pl arg1 arg2 arg3\n";
    exit 2;
}
foreach my $arg (@ARGV){
    print "$arg\n"
}

if ( $ARGV[0] != 0 ){
    print "I would exit with non zero status!\n" ;
    exit int($ARGV[0]);
}
exit 0;