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
                    (glob-pattern pattern))
                  :compiled-pattern)
        matches (re-matches regex s)]
    (if (string? matches)
      matches
      (first matches))))

(defn- split-on-slash [s]
  (->> (string/split s #"/")
       (remove string/blank?)))

(defn- absolute? [s]
  (= (first s) \/))

(defn negate [form]
  `(not ~form))

(defmacro and-not [& forms]
  `(and ~@(map negate forms)))

(defn globf
  "Attempt to match the file path with the given glob pattern.
  Returns the matched portion on success, otherwise nil.
  Pattern and path must be strings."
  [pattern path]
  (let [abs-path? (absolute? path)
        abs-pattern? (absolute? pattern)]
    (when (or (and abs-path? abs-pattern?)
              (and-not abs-path? abs-pattern?))
      (let [pattern-parts (split-on-slash pattern)
            path-parts (split-on-slash path)
            comb (->> (interleave pattern-parts path-parts)
                      (partition 2))
            path-matches
            (for [[pattern path] comb
                  :let [regex (-> (glob-pattern pattern)
                                  :compiled-pattern)
                        matches (re-matches regex path)]
                  :while matches]
              path)]
        (when (= (count path-matches)
                 (count pattern-parts))
          (str (when abs-path? "/")
               (string/join "/" path-matches)))))))
