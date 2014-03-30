(defproject canvas-experiments "0.1.0-SNAPSHOT"
  :description "Canvas art"
  :license {:name "MIT Licence"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2014"]
                 [rm-hull/monet "0.1.10"]]
  :plugins [[lein-cljsbuild "1.0.2"]]
  :cljsbuild { 
    :builds {
      :main {
        :source-paths ["src"]
        :compiler {:output-to "public/cljs.js"
                   :optimizations :simple
                   :externs ["src/externs.js"]
                   :pretty-print true}}}})
