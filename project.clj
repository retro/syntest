(defproject syntest "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.854"]
                 [cljsjs/jquery "3.2.1-0"]
                 [funcool/promesa "1.9.0"]
                 [org.clojure/core.async "0.3.443"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :plugins [[lein-cljsbuild "1.1.4"]]

  :clean-targets ^{:protect false} ["resources/public/js"
                                    "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies []

    :plugins      [[lein-figwheel "0.5.13-SNAPSHOT"]
                   [lein-doo "0.1.7"]]
    }}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "syntest.core/reload"}
     :compiler     {:main                 syntest.core
                    :optimizations        :none
                    :output-to            "resources/public/js/app.js"
                    :output-dir           "resources/public/js/dev"
                    :asset-path           "js/dev"
                    :install-deps true
                    :npm-deps {:syn "0.10.0"
                               :karma "^0.13.16"
                               :karma-chrome-launcher "^0.2.2"
                               :karma-cljs-test "^0.1.0"}
                    :source-map-timestamp true}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            syntest.core
                    :optimizations   :advanced
                    :output-to       "resources/public/js/app.js"
                    :output-dir      "resources/public/js/min"
                    :elide-asserts   true
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:output-to     "resources/public/js/test.js"
                    :output-dir    "resources/public/js/test"
                    :main          syntest.runner
                    :optimizations :none
                    :install-deps true
                    :npm-deps {:syn "0.10.0"
                               :karma "^0.13.16"
                               :karma-chrome-launcher "^0.2.2"
                               :karma-cljs-test "^0.1.0"}}}
    ]})
