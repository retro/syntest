(ns syntest.core-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [syntest.core :as s]
            [cljs.core.async :refer [<!]]
            [syntest.test :refer-macros [synasync]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn make-container! []
  (let [div (.createElement js/document "div")
        body (.-body js/document)
        cleanup #(.removeChild body div)]
    (.appendChild body div)
    [div cleanup]))

(deftest find-element
  (let [[div cleanup] (make-container!)]
    (set! (.-innerHTML div) "<article></article>")
    (synasync [el]
     (s/existing? "article")
     (s/existing? el)
     (cleanup))))

(deftest find-element-with-delay
  (let [[div cleanup] (make-container!)]
    (js/setTimeout #(set! (.-innerHTML div) "<article><article>") 1000)
    (synasync [el]
     (s/existing? "article")
     (cleanup))))

