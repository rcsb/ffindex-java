# Overview

FFindex is a very simple index/database for huge amounts of small files. The files are stored concatenated in one big 
data file, seperated by '\0'. A second file contains a plain text index, giving name, offset and length of the small 
files.

# Details & Limitations
No guarantees are made that files produced by this project are interoperable with the original FFindex files or 
implementations. This implementation is motivated by FFindex and produces identical files in simple cases. However, the
original FFindex anticipates index entries to be sorted (to perform binary search on it), this implementation writes 
entries in their insertion order and implements access by a map.

The maximum size of individual files to store is ~2 GB.

# Copyright

FFindex was written by Andreas Hauser <Andreas.Hauser@LMU.de>.

FFindex is registered trademark of the Ludwig-Maximilians-Universität, Munich (LMU).
FFindex is provided under the [Create Commons license "Attribution-ShareAlike 4.0"](http://creativecommons.org/licenses/by-sa/4.0/).

The reference implementation can be found at: https://github.com/ahcm/ffindex

Check out the ffindex command-line tool for useful functionality that isn't part of this project.