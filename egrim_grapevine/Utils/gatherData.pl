#!/usr/bin/perl

open N,"active_nodes.txt";

foreach $node (<N>) {
    chomp $node;
    print "getting data from $node...\n";
    $results = `scp ut\@$node:M15/$ARGV[0]* .`;
    print "$results";
}

close N;
