#!/usr/bin/perl

open F,"active_nodes.txt";

foreach $node (<F>) {
	chomp $node;
    print "executing $ARGV[0] on node: $node\n";
	print `ssh ut\@$node \"$ARGV[0]\"`;
}
