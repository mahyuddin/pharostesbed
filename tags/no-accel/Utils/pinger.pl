#!/usr/bin/perl

open F,"active_nodes.txt";

foreach $node (<F>) {
	chomp $node;
    print "pinging node: $node... ";
    `ping -c 1 $node`;
    if ($? == 0) {
        print "up\n";
    } else { 
        print "down\n";
    }
}
