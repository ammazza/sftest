(ns sftest.strings-test
  (:require [clojure.test :refer :all]
            [sftest.dict :refer [dict]]
            [sftest.strings :refer :all]))

;; Passing empty string sequence
(deftest empty-seq
  (testing "Empty sequence"
    (is (= {:s1 "", :s2 "", :dist ##Inf} 
         (find-closest-strings
          []
          hamming
          (fn [d] (< d 2))
          (fn [xs] (sort-by count (map clojure.string/lower-case xs))))))))

;; Passing single string sequence
(deftest singleton-seq
  (testing "Singleton sequence"
    (is (= {:s1 "", :s2 "", :dist ##Inf} 
         (find-closest-strings
          ["foo"]
          hamming
          (fn [d] (< d 2))
          (fn [xs] (sort-by count (map clojure.string/lower-case xs))))))))

;; Minimal sequence
(deftest minimal-seq
  (testing "Minimal sequence"
    (is (= {:s1 "foo", :s2 "bar", :dist 3} 
         (find-closest-strings
          ["foo" "bar"]
          hamming
          (fn [d] (< d 2))
          (fn [xs] (sort-by count (map clojure.string/lower-case xs))))))))

;; Non-string sequence, without threshold
(deftest number-seq
  (testing "Number sequence"
    (is (= {:s1 6, :s2 7, :dist 1}
           (find-closest-strings
            [1 2 3 4 5 6 7]
            (fn [x y] (Math/abs (- x y)))
            (fn [d] false)
            identity)))))

;; Hamming ascending length sequence
(deftest hamming-asc-seq
  (testing "Hamming ascending length sequence"
    (is (= {:s1 "a", :s2 "i", :dist 1}
         (find-closest-strings
          (dict)
          hamming
          (fn [d] (< d 2))
          (fn [xs] (sort-by count (map clojure.string/lower-case xs))))))))

;; Hamming descending length sequence
(deftest hamming-desc-seq
  (testing "Hamming descending length sequence"
    (is (= {:s1 "billion", :s2 "million", :dist 1}
         (find-closest-strings
          (dict)
          hamming
          (fn [d] (< d 2))
          (fn [xs] (sort-by count #(compare %2 %1) (map clojure.string/lower-case xs))))))))
