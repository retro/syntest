(ns syntest.test.core
  (:require [syntest.core :as s]
            [cljs.test :refer-macros [deftest is async]]
            [cljs.core.async :refer [<! timeout]]
            [syntest.dom :as dom]
            [cljsjs.jquery]
            [jayq.core :as jayq])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [syntest.core :refer [syn-run! defsyntest]]))

(defn format-log-item [[type msg]]
  (case type
    :action (str "ACTION: " (name (:type msg)) " on " (:selector msg))
    :before (str "BEFORE: " (name (:type msg)) " on " (:selector msg))
    :error (str "FAILED: " (name (:type msg)) " on " (:selector msg) " after " (:wait msg) "s")))

(add-watch s/context :syntest-context 
           (fn [key atom old-state new-state]
             (let [old (:log old-state)
                   new (drop (count old) (:log new-state))]
               (doseq [log-item new]
                      (.log js/console (format-log-item log-item))))))

(defn container! []
  (let [div (.createElement js/document "div")
        body (.-body js/document)
        cleanup #(.removeChild body div)]
    (.appendChild body div)
    [div cleanup]))

(defn on-click! [el]
  (set! (.-onclick el) #(jayq/text (jayq/$ el) "CLICKED")))

(defn was-clicked? [el]
  (= "CLICKED" (jayq/text (jayq/$ el))))

(defn el! [tag container]
  (let [el (.createElement js/document tag)]
    (jayq/append (jayq/$ container) el) 
    el))

(defsyntest clicking-el []
  (let [[container cleanup] (container!)
        div (el! "div" container)]
    (on-click! div)
    (<! (s/click! div))
    (is (was-clicked? div))
    (cleanup)))

(defsyntest clicking-el-by-selector []
  (let [[container cleanup] (container!)
        div (el! "div" container)]
    (jayq/add-class (jayq/$ div) "foo")
    (on-click! div)
    (<! (s/click! ".foo"))
    (is (was-clicked? div))
    (cleanup)))

(defsyntest clicking-el-with-timeout []
  (let [[container cleanup] (container!)
        bootstrap (fn []
                    (let [div (el! "div" container)]
                      (on-click! div)
                      (jayq/add-class (jayq/$ div) "bar")))]
    (.setTimeout js/window bootstrap 1000)
    (<! (s/click! ".bar"))
    (is (was-clicked? (first (jayq/$ ".bar"))))
    (cleanup)))

(defsyntest trying-to-click-non-existing-element []
  (<! (s/click! ".non-existing-element"))
  (is false "This should never run"))

(defsyntest typing []
  (let [[container cleanup] (container!)
        bootstrap #(el! "input" container)]
    (.setTimeout js/window bootstrap 2000)
    (<! (s/type! "input" "testing"))
    (is (= (jayq/val (jayq/$ "input")) "testing"))
    (cleanup)))

(defsyntest action-sends-element-on-chan []
  (let [[container cleanup] (container!)
        div (el! "div" container)
        chan-div (<! (s/click! div))]
    (= div chan-div)
    (cleanup)))
