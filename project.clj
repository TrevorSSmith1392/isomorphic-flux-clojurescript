(defproject node-test "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2505"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [reagent "0.5.0-alpha"]
                 ]

  :node-dependencies [[source-map-support "0.2.8"]
                      [lodash "2.4.1"]
                      [express "4.10.6"]
                      [react "0.12.2"]
                      [fluxible-app "0.1.2"]
                      [fluxible-plugin-fetchr "0.2.1"]
                      [dot 1.0.3]
                      ]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
            [lein-npm "0.4.0"]]

  :source-paths ["src/node" "src/shared" "src/browser"]

  :cljsbuild {
    :builds [{:id "server"
              :source-paths ["src/node" "src/shared"]
              ;:notify-command  ["supervisor" "run.js -w run.js,out;"]
              :compiler {
                :output-to "out/server.js"
                :output-dir "out"
                :target :nodejs
                :optimizations :none
                :preamble  ["reagent/react.js"]
                :source-map true}}
             {:id "dev"
              :source-paths  ["src/browser" "src/shared"]
              :compiler  {
                :output-to "resources/public/js/main.js"
                :output-dir "resources/public/js/out"
                :optimizations :none
                :preamble  ["reagent/react.js"]
                :source-map true}}]})


