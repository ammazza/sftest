(ns sftest.strings)

;; Example dataset for development
(defn strs [] ["functional" "dysfunctional" "function" "ufo" "filippo"])

;; Silly but fast  string similary for development -  absolute difference in
;; number of characters.
(defn closest-length [x y] (Math/abs (- (count x) (count y))))

;; Given a sequence of strings strs, return the closest to the first.
;; The result is a hashmap with the two strings and the distance.
;; TODO: check length of xs >1
;; TODO: consider returning a maybe is length <2
;; TODO: have additional function to asses if min was reached and terminate with reduced.
(defn closest-to-first
  ""
  [xs   ;; The sequence of elements (strings)
   dfn] ;; A comparison binary function - smaller is more similar
  (reduce
   ;; The reducing function takes parms of different types. The first is a hashmap
   ;; (result of the  previous iteration), the second is a  element of the
   ;; sequence to reduce (the next element to be processed).
   (fn [res x]
     (let [d (dfn (:s1 res) x)]
       (if (< d (:dist res)) (assoc res :s2 x :dist d) res)))
   ;; We initialise  the reduction  result with  the distance  between the
   ;; first two elements in the sequence.
   {:s1 (first xs) :s2 (second xs) :dist (dfn (first xs) (second xs))}
   (rest xs)))

;; Function closest to first can be  easily used to process only subsequences of
;; the main sequence -  e.g.  all words with the same length  if we're using the
;; Hamming distance. The  function processing the main sequence  should make use
;; of this (e.g. sort by length and pass a termination function that checks for
;; difference in length).

;; This will take the main sequence, apply any preprocessing if needed, and
;; the run closest to first for all subsequences.
(defn find-closest-strings "" [] nil)
