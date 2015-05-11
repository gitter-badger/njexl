#!/usr/local/bin/python

import sys
print 'I am sample Python Script'

if len(sys.argv ) < 2 :
    print 'Usage : at least one arg required!'
    sys.exit(2)

for arg in sys.argv:
    print arg

if sys.argv[1] != 0:
    print 'I would exit with non zero status!'
    sys.exit( int(sys.argv[1]) )
sys.exit(0)
