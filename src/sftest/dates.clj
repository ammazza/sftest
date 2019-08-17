(ns sftest.dates
  ;; (:require [sftest.dict :refer [dict]])
  (:gen-class))

;; Internally dates are represented as hashmaps.
;; {:year 2019 :month 6 :day 25 :hour 21 :minute 53 :second 35}

;; Given a sequence of dates, sort it in descending order.
;; This function isn't actually used anywhere.
(defn- sort-dates [ds]
  (sort-by
   (juxt :year :month :day :hour :minute :second)
   #(compare %2 %1)
   ds))

;; Number of leap years from epoch
;; TODO: implement!
(defn- is-leap-year [y]
  (if (zero? (rem y 400)) true
      (if (zero? (rem y 100)) false
          (if (zero? (rem y 4)) true))))

(defn leap-years-from-epoch [d]
  (filter is-leap-year (range (:year d))))

;; Given a date, return how many full days passed from
;; the beginning of the year.
(defn days-from-year-beginning [d]
  (let [monthdays (if (is-leap-year (:year d))
                    [31 29 31 30 31 30 31 31 30 31 30 31]
                    [31 28 31 30 31 30 31 31 30 31 30 31])]
    (+ (dec (:day d))
       (reduce + (take (dec (:month d)) monthdays)))))

;; Given a date, return how many full days have passed since
;; the epoch: 1 Jan 0000 at 00:00:00.
(defn- days-from-epoch [d]
  (+ (* 365 (:year d))
     (leap-years-from-epoch d)
     (days-from-year-beginning d)))

;; Epoch {:year 0 :month 1 :day 1 :hour 0 :minute 0 :second 0}
(defn- seconds-from-epoch [d]
  (+ (* 24 60 60 (days-from-epoch d))
     (* 60 60 (:hour d))
     (* 60 (:minute d))
     (:second d)))

(defn- date-difference-seconds [d1 d2]
  (let [d1 (seconds-from-epoch d1)
        d2 (seconds-from-epoch d2)]
    (Math/abs (- d1 d2))))

;; {:year 0 :month 1 :day 1 :hour 0 :minute 0 :second 0}

;; Use this for tests: https://www.mkyong.com/java/how-to-calculate-date-time-difference-in-java/

;; Leap years: https://kalender-365.de/leap-years.php


(defn date-regex []
  (let [s (str "(?<year>[1-9][0-9][0-9][0-9])-"
               "(?<month>[0-1][0-9])-"
               "(?<day>[0-3][0-9])T"
               "(?<hour>[0-2][0-9]):"
               "(?<minute>[0-5][0-9]):"
               "(?<second>[0-5][0-9])")]
    (java.util.regex.Pattern/compile s)))

(defn parse-date [s]
  (let [matcher (re-matcher (date-regex) s)]
    (if (.matches matcher)
      {:year (Integer. (.group matcher "year"))
       :month (Integer. (.group matcher "month"))
       :day (Integer. (.group matcher "day"))
       :hour (Integer. (.group matcher "hour"))
       :minute (Integer. (.group matcher "minute"))
       :second (Integer. (.group matcher "second"))}
      nil)))

;; (parse-date "2018-06-25T21:53:35")

(defn string-date-difference-seconds [s1 s2]
  (let [d1 (parse-date s1)
        d2 (parse-date s2)]
    (if (or (nil? d1) (nil? d2)) nil
        (date-difference-seconds d1 d2))))
  
(defn- diff-and-print [s1 s2]
  (let [d (string-date-difference-seconds s1 s2)]
    (if (nil? d)
      (println "Couldn't parse dates:" s1 "/" s2)
      (println s1 "-" s2 "=" d "sec"))))

;; OK, this read from stdin until C-d and keep the seq in memory.
(defn -main []
  (println
   (str
    "Date difference - Input dates one per line, in format\n"
    "'YYYY-MM-DDTHH:MM:SS'. Use CTRL-D to stop input.\n"
    "Dates will be takes two by two and their difference in\n"
    "seconds will be calculated.\n"))
  (let [lns (doall (line-seq (java.io.BufferedReader. *in*)))]
    (println)
    (dorun (map #(apply diff-and-print %) (partition 2 lns)))))

;; java -cp target/sftest-0.1.0-SNAPSHOT-standalone.jar sftest.dates




