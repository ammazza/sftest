(ns sftest.dates-test
  (:require [clojure.test :refer :all]
            [sftest.dates :refer :all]))

(deftest test-same-date
  (testing "test same-date"
    (is (= 0
           (string-date-difference-seconds "2018-06-25T21:53:35" "2018-06-25T21:53:35")))))

(deftest test-2-secs
  (testing "test 2 secs"
    (is (= 2
           (string-date-difference-seconds "2018-06-25T21:53:35" "2018-06-25T21:53:37")))))

(deftest test-other-way
  (testing "test other way"
    (is (= (string-date-difference-seconds "2018-06-25T21:53:35" "2018-06-25T21:53:37")
           (string-date-difference-seconds "2018-06-25T21:53:37" "2018-06-25T21:53:35")))))

(deftest test-1-year
  (testing "test 1 year"
    (is (= 31536000
           (string-date-difference-seconds "2018-06-25T21:53:35" "2019-06-25T21:53:35")))))

(deftest test-all-1-bigger
  (testing "test all 1 bigger"
    (is (= 34218061
           (string-date-difference-seconds "2018-06-25T21:53:35" "2019-07-26T22:54:36")))))

(deftest test-leap-year
  (testing "test leap year"
    (is (= 31622400
           (string-date-difference-seconds "2000-01-01T10:00:00" "2001-01-01T10:00:00")))))

(deftest test-cannot-parse
  (testing "test cannot parse"
    (is (nil?
         (string-date-difference-seconds "2018-06-25T21:53:35" "2018-Jun-25T21:53:35")))))

