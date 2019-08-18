# sftest

Two Clojure example applications that:
- find the two most similar strings in a sequence
- compute the difference in seconds between two dates

## Prerequisites

To compile and run the code you will need two things:
- a recent Java Virtual Machine
- the [Leiningen](https://leiningen.org/) build tool for Clojure.

Leiningen installation consists  of a single script. Once put  on your execution
path, try the command `lein repl`. It will download all its dependencies and, if
successful, start a Clojure REPL (that you can quit with `CTRL-D`).

## Compiling

From the repository  root run `lein uberjar`. This will  create a standalone jar
file containing both applications.

## Usage

Both applications when started show a  brief help message and then start waiting
for  input.   You  can  type  input  values one  per  line  (i.e.   followed  by
enter).  When you're  done type  `CTRL-D` on  a new  line. The  application will
process the input and print out the results.

Input lines  can be put in  a file and  piped into the applications.  Two runner
scripts  are  provided  (directory  `bin`),   to  run  each  application  either
interactively or with  an input file. Two example input  files are also provided
(directory `resources`).

### String comparison usage

To run the string comparison application with the example input file use:

`./bin/words.sh ./resources/words.txt`

The output should be:

`billion - million -> 1 chars`

These are the closest  words found, that differ by 1 character.  See below for a
detailed explanation.

### Date difference usage

To run the date difference application with the example input file use:

`./bin/dates.sh ./resources/dates.txt`

The output should be:

``` text
2018-06-25T21:53:35 - 2018-06-25T21:53:35 = 0 sec
2018-06-25T21:53:35 - 2018-06-25T21:53:37 = 2 sec
2018-06-25T21:53:37 - 2018-06-25T21:53:35 = 2 sec
2018-06-25T21:53:35 - 2019-06-25T21:53:35 = 31536000 sec
2018-06-25T21:53:35 - 2019-07-26T22:54:36 = 34218061 sec
2000-01-01T10:00:00 - 2001-01-01T10:00:00 = 31622400 sec
Couldn't parse dates: 2018-06-25T21:53:35 / 2018-Jun-25T21:53:35
```

These are  pairs of successive  dates, with their  difference in seconds,  or an
error  message  if the  dates  couldn't  be parsed.  See  below  for a  detailed
explanation.

## Internals

This section explains some notable design and implementation choices.  Other
details are included in the comments to the soure code, in files
[`dates.clj`](src/sftest/dates.clj) and
[`strings.clj`](src/sftest/strings.clj).

### String comparison explanation

The core of this application  is the function `find-closest-strings`. It accepts
a sequence  of strings and  a string distance function  and returns one  pair of
strings that are the closest according to the function.

The distance function takes 2 strings  and returns an integer (distance) that is
smaller for more similar strings or nil if the distance couldn't be computed for
those  strings.   Currently  the  only  distance  implemented  is  the  _Hamming
distance_, that is  the number of character  that differ in strings  of the same
length.

The basic  search for similar strings  compares each strings with  all the other
ones in the sequence (order of comparison doesn't matter, each pair of string is
compared once only). The generic approach has complexity `O(N * log N)`, but the
search function accommodates for strategies that potentially reduce complexity.

In fact the full signature of function `find-closest-strings` is as follows:

``` clojure
;; Given a  sequence of strings,  return the two  strings that are  closest. The
;; result is a hashmap with the two strings and the distance.
(defn find-closest-strings
  [xs   ;; The sequence of elements (strings)
   dfn  ;; A distance binary function - smaller is more similar
   bfn  ;; Break function - boolean: if true the result if good enough
   pfn] ;; Preprocessing function that returns a new xs
...)
```

Firstly a preprocessing function can be passed, to transform the input sequence.
For  instance, the  application sorts  the words  in descending  order by  their
length.   This combined  with the  fact that  the Hamming  distance can  only be
computed  on  string  of  equal   length,  potentially  reduces  the  number  of
comparisons. In fact the inner search  loop will terminate early if the distance
cannot be computed on a pair.

Additionally a  _break function_  can be  passed, that  makes the  search return
based on a  distance value. This way  a _good enough_ threshold can  be set: the
first pair of string that have that  distance or less are returned as result and
the search terminates.

#### Other string distances

The  Hamming  distance was  chosen  because  it's  simple  to compute  and  well
represent  the  human  concept  of  string  similarity.  Also,  because  of  its
applicability to strings of same length  only, allows to reduce the search space
as explained above.

Another  fast distance  for  same  length strings,  the  cosine similarity,  was
rejected  as  less   _human_  because  it  may  select   anagrams,  but  further
investigations would be necessary.

More  generic distances  like the  Levenstein algorithm  or the  _Longest Common
Subsequence_  could be  easily added  to  the application,  although they  would
require  searching  all  pairs  of   strings,  making  the  search  time  always
worst-case.

### Date difference explanation

The date  difference application parses  dates from strings and  represents them
internally as hashmaps  e.g. `{:year 2019 :month  6 :day 25 :hour  21 :minute 53
:second 35}`.

To operate on them,  dates are converted to second elapsed  since the epoch, set
to 1st January of  the year 0 at midnight. When  computing date differences leap
years are also considered.

The application  accepts dates as  strings in the  format `YYYY-MM-DDTHH:MM:SS`.
The date parsing  functions utilitses a regular epxression to  validate the date
and extract its components. This is the only validation and it doesn't cover all
cases, so  while it doesn't  accept a  day with number  `41` it does  accept day
`39`.

## License

Copyright © 2019 Filippo Ammazzalorso

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.
