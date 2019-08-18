(ns sftest.dates
  (:gen-class))

;; Internally dates are represented as hashmaps.
;; {:year 2019 :month 6 :day 25 :hour 21 :minute 53 :second 35}
;;
;; To operate on them, dates are converted to second elapsed
;; since the epoch: 1st Jan of year 0 at midnight.
;; {:year 0 :month 1 :day 1 :hour 0 :minute 0 :second 0}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Given a sequence of dates, sort it in descending order.
;; This function isn't actually used anywhere.
(defn- sort-dates [ds]
  (sort-by
   (juxt :year :month :day :hour :minute :second)
   #(compare %2 %1)
   ds))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Leap year management

;; True if year y, passed as integer, is leap. False otherwise.
(defn- is-leap-year [y]
  (if (zero? (rem y 400)) true
      (if (zero? (rem y 100)) false
          (if (zero? (rem y 4)) true))))

;; Given date d, count leap years since the epoch. The date's year
;; itself is not counted in. The epoch is a leap year.
(defn- leap-years-from-epoch [d]
  (count (filter is-leap-year (range (:year d)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Conversion from date to time since epoch

;; Given a date, return how many full days passed from
;; the beginning of the year.
(defn- days-from-year-beginning [d]
  (let [monthdays (if (is-leap-year (:year d))
                    [31 29 31 30 31 30 31 31 30 31 30 31]
                    [31 28 31 30 31 30 31 31 30 31 30 31])]
    (+ (dec (:day d))
       (reduce + (take (dec (:month d)) monthdays)))))

;; Given a date, return how many full days have passed since
;; the epoch.
(defn- days-from-epoch [d]
  (+ (* 365 (:year d))
     (leap-years-from-epoch d)
     (days-from-year-beginning d)))

;; Given a date, return how many seconds have passed since
;; the epoch.
(defn- seconds-from-epoch [d]
  (+ (* 24 60 60 (days-from-epoch d))
     (* 60 60 (:hour d))
     (* 60 (:minute d))
     (:second d)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Compute the difference of two dates. The positive difference
;; is returned, independently of the dates' order.

(defn- date-difference-seconds [d1 d2]
  (let [d1 (seconds-from-epoch d1)
        d2 (seconds-from-epoch d2)]
    (Math/abs (- d1 d2))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Date parsing from text - Format: 'YYYY-MM-DDTHH:MM:SS'

;; Regex to validate a date and extract its components.
(defn- date-regex []
  (let [s (str "(?<year>[1-9][0-9][0-9][0-9])-"
               "(?<month>[0-1][0-9])-"
               "(?<day>[0-3][0-9])T"
               "(?<hour>[0-2][0-9]):"
               "(?<minute>[0-5][0-9]):"
               "(?<second>[0-5][0-9])")]
    (java.util.regex.Pattern/compile s)))

;; Use the regex above to parse a string to a date.
;; If the string cannot be parsed return nil.
(defn- parse-date [s]
  (let [matcher (re-matcher (date-regex) s)]
    (if (.matches matcher)
      {:year (Integer. (.group matcher "year"))
       :month (Integer. (.group matcher "month"))
       :day (Integer. (.group matcher "day"))
       :hour (Integer. (.group matcher "hour"))
       :minute (Integer. (.group matcher "minute"))
       :second (Integer. (.group matcher "second"))}
      nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main method + utility functions. 

;; Compute date difference in seconds starting from dates
;; expressed as strings. Return nil if any of the dates could
;; not be parsed.
(defn- string-date-difference-seconds [s1 s2]
  (let [d1 (parse-date s1)
        d2 (parse-date s2)]
    (if (or (nil? d1) (nil? d2)) nil
        (date-difference-seconds d1 d2))))

;; Compute date difference in seconds starting from dates
;; and print out the difference or an error message.
(defn- diff-and-print [s1 s2]
  (let [d (string-date-difference-seconds s1 s2)]
    (if (nil? d)
      (println "Couldn't parse dates:" s1 "/" s2)
      (println s1 "-" s2 "=" d "sec"))))

(defn -main []
  (println
   (str
    "Date difference - Input dates one per line, in format\n"
    "'YYYY-MM-DDTHH:MM:SS'. Use CTRL-D to stop input.\n"
    "Dates will be takes two by two and their difference in\n"
    "seconds will be calculated.\n"))
  ;; Read lines until CTRL-D, load the whole sequence in memory.
  (let [lns (doall (line-seq (java.io.BufferedReader. *in*)))]
    (println)
    ;; Take input lines 2-by-2 and try to calculate their difference.
    (dorun (map #(apply diff-and-print %) (partition 2 lns)))))
