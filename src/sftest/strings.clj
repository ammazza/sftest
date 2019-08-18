;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Structures and functions to compute string distance and find similar strings.

(ns sftest.strings
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; String distance metrics. Any string distance functions must take two string
;; and return an integer, that is smaller when the strings are more similar. If
;; the distance cannot be applied to the two strings (e.g. they must be of same
;; length) then return nil.

;; Hamming distance of two strings: counts characters that differ.
(defn hamming [x y]
  (if (not= (count x) (count y)) nil
      (count (filter true? (map not= x y)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions to search sequences for similar strings.

;; Given a sequence of strings, return the  closest to the first.  The result is
;; a hashmap with the two strings and the distance - {:s1 "sss" :s2 "sss" :dist nnn}
(defn closest-to-first
  [xs   ;; The sequence of elements (strings)
   dfn  ;; A distance binary function - smaller is more similar, nil for invalid
   bfn] ;; Break function - boolean: true the result if good enough
  (let [refx (first xs)] ;; The first element (reference) to be compared against the others.
    (reduce
     ;; The reducing function takes parms of different types. The first is a hashmap
     ;; (result of the  previous iteration), the second is a  element of the
     ;; sequence to reduce (the next element to be processed).
     (fn [res x]
       ;; Function dfn returns the distance or nil if input is invalid,
       ;; like trying to compute Hamming of strings of different length.
       (let [d (dfn refx x)]
         ;; (println refx " vs " x)
         ;; If the result is nil, exit the reduction loop, otherwise
         ;; compare the result with the previous and if needed update.
         (if (nil? d) (reduced res)
             (let [res (if (< d (:dist res)) {:s1 refx :s2 x :dist d} res)]
               ;; Check current result with the break function and possibly terminate search.
               (if (bfn (:dist res)) (reduced res) res)))))
     ;; We initialise  the reduction  result with an 'empty result'. Empty
     ;; string and distance set to +inf (worse than any result).
     {:s1 "" :s2 "" :dist ##Inf}
     ;; We don't compare the first element against itself, would be a fake optimal solution.
     (rest xs))))

;; Given a  sequence of strings,  return the two  strings that are  closest. The
;; result is a hashmap with the two strings and the distance.
(defn find-closest-strings
  [xs   ;; The sequence of elements (strings)
   dfn  ;; A distance binary function - smaller is more similar
   bfn  ;; Break function - boolean: if true the result if good enough
   pfn] ;; Preprocessing function that returns a new xs
  (let [xs (pfn xs)
        xs (map (partial nthnext xs) (range (count xs)))]
    (reduce
     (fn [r1 x]
       (let [r2 (closest-to-first x dfn bfn)
             r (if (< (:dist r1) (:dist r2)) r1 r2)]
         (if (bfn (:dist r)) (reduced r) r)))
     (closest-to-first (first xs) dfn bfn)
     (rest xs))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main function.

(defn -main []
  (println
   (str
    "Closest strings - Input string one per line and use CTRL-D to finish.\n"
    "The two closest strings will be found, using the Hamming distance.\n"
    "The comparison is case-insensitive and will stop as soon as it finds\n"
    "the longest strings that differ by 1 character only.\n"))
  (let [lns (doall (line-seq (java.io.BufferedReader. *in*)))
        ;; The behaviour and performance of find-closest-strings is strongly influenced
        ;; by the functions passed as parameter. The ones used in this application are a
        ;; performace-accuracy trade-off. Strings are sorted by decreasing length and a
        ;; distance is used (Hamming) that works only on strings of same length. This allows
        ;; to reduce the number of comparisons. Also, a threshold function is passed: when
        ;; two strings that differ by one character only are found, the search terminates.
        res (find-closest-strings lns
                                  hamming
                                  (fn [d] (< d 2))
                                  ;; Sorting by descending length, lets us find first longer
                                  ;; words (hence more meaningful ones) that are similar.
                                  (fn [xs] (sort-by count #(compare %2 %1) (map clojure.string/lower-case xs))))]
    (println)
    (println (:s1 res) "-" (:s2 res) "->" (:dist res) "chars")))
