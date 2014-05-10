(defproject stch-library/glob "0.1.0"
  :description "Glob pattern matching."
  :url "https://github.com/stch-library/glob"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:dependencies [[speclj "3.0.2"]]}}
  :plugins [[speclj "3.0.2"]]
  :test-paths ["spec"])
