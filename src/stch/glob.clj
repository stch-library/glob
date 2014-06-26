(ns stch.glob
  "Contains a simple glob matching fn. Credit to
  https://github.com/jkk/clj-glob."
  (:require [clojure.string :as string]))

(defrecord CompiledPattern [regex])

(defn compiled-pattern? [x]
  (instance? CompiledPattern x))

(defn compile-pattern
  "Takes a glob pattern as a string and returns a GlobPattern."
  [pattern]
  (loop [[c :as stream] pattern, re "", curly-depth 0]
    (cond
     ; No more characters, return a CompiledPattern
     (nil? c) (CompiledPattern. (re-pattern re))
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

(defn match-glob
  "Attempt to match a string with the given glob
  pattern.  Returns the matched string on success,
  otherwise nil."
  [pattern s]
  (let [regex (-> (if (compiled-pattern? pattern)
                    pattern
                    (compile-pattern pattern))
                  :regex)
        matches (re-matches regex s)]
    (if (string? matches)
      matches
      (first matches))))

(defn- split-on-slash [s]
  (->> (string/split s #"/")
       (remove string/blank?)))

(defn- absolute? [s]
  (= (first s) \/))

(defmacro and-not [& forms]
  `(and ~@(map (fn [form] `(not ~form)) forms)))

(defn match-globf
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
                  :let [regex (:regex (compile-pattern pattern))
                        matches (re-matches regex path)]
                  :while matches]
              path)]
        (when (= (count path-matches)
                 (count pattern-parts))
          (str (when abs-path? "/")
               (string/join "/" path-matches)))))))
