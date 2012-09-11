#!/usr/bin/perl

open F,"active_nodes.txt";

foreach $node (<F>) {
	chomp $node;
	print "uploading $ARGV[0] to $node:$ARGV[1]\n";
	`scp $ARGV[0] ut\@$node:$ARGV[1]`;
}
