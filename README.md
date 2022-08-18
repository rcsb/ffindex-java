# Overview

FFindex is a very simple index/database for huge amounts of small files. The files are stored concatenated in one big 
data file, seperated by '\0'. A second file contains a plain text index, giving name, offset and length of the small 
files.

# Performance

Performance was measured on a collection of 190k BinaryCIF files (stored as `.bcif.gz`).

## Read Times (JMH)
| Approach | read 1,000 random files [ms] |
| --- | --- |
| File System | 48 |
| FFindex-java | 30 |

## Write Times (JHM)
| Approach | bundle 190,000 files [s] |
| --- | --- |
| tar | 57 |
| tar + gzip | 166 |
| tar + pigz (multi-threaded) | 62 |
| FFindex-java (single-threaded) | 80 |
| FFindex-java (multi-threaded) | 12 |

## File Sizes (`du -sh`)
| Approach | size [GB] |
| --- | --- |
| plain directory | 3.8 |
| tar | 3.6 |
| tar + gzip | 3.5 |
| FFindex-java | 3.4 GB (data), 6.0 MB (index) |

Last one seems fishy, but maybe it's the absence of metadata.

# Details & Limitations
No guarantees are made that files produced by this project are interoperable with the original FFindex files or 
implementations. This implementation is motivated by FFindex and produces identical files in simple cases. However, the
original FFindex anticipates index entries to be sorted (to perform binary search on it), this implementation writes 
entries in their insertion order and implements access by a map.

The maximum size of individual files to store is ~2 GB. The maximum number of files is capped by the size of Java arrays
as well.

# Copyright

FFindex was written by Andreas Hauser <Andreas.Hauser@LMU.de>.

FFindex is registered trademark of the Ludwig-Maximilians-Universität, Munich (LMU).
FFindex is provided under the [Create Commons license "Attribution-ShareAlike 4.0"](http://creativecommons.org/licenses/by-sa/4.0/).

The reference implementation can be found at: https://github.com/ahcm/ffindex

Check out the ffindex command-line tool for useful functionality that isn't part of this project.