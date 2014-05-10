(ns stch.glob
  "Contains a simple glob fn. Credit to
  https://github.com/jkk/clj-glob."
  (:require [clojure.string :as string]))

(defrecord GlobPattern [compiled-pattern])

(defn glob-pattern
  "Takes a glob pattern as a string and returns a GlobPattern."
  [pattern]
  (loop [[c :as stream] pattern, re "", curly-depth 0]
    (cond
     ; No more characters, return a GlobPattern
     (nil? c) (GlobPattern. (re-pattern re))
     ; Handle glob special characters
     (= c \\) (recur (nnext stream) (str re c c) curly-depth)
     (= c \*) (recur (next stream) (str re ".*") curly-depth)
     (= c \?) (recur (next stream) (str re ".{1}") curly-depth)
     (= c \{) (recur (next stream) (str re \() (inc curly-depth))
     (= c \}) (recur (next stream) (str re \)) (dec curly-depth))
     ; handle comma separator within curly brackets
     (and (= c \,) (> curly-depth 0))
     (recur (next stream) (str re \|) curly-depth)
     ; Escape regex special characters
     (#{\. \( \) \| \+ \^ \$ \@ \%} c)
     (recur (next stream) (str re \\ c) curly-depth)
     ; Not a special character
     :else (recur (next stream) (str re c) curly-depth))))

(defn glob
  "Attempt to match a string with the given glob
  pattern.  Returns the matched string on success,
  otherwise nil."
  [pattern s]
  (let [regex (-> (if (instance? GlobPattern pattern)
                    pattern
                    (glob-pattern (name pattern)))
                  :compiled-pattern)
        matches (re-matches regex s)]
    (if (string? matches)
      matches
      (first matches))))
