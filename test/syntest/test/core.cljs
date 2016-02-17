(ns syntest.test.core
  (:require [syntest.core :as s]
            [cljs.test :refer-macros [deftest is async]]
            [cljs.core.async :refer [<!]]
            [dommy.core :as dommy :refer-macros [sel sel1]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [syntest.core :refer [syn-run!]]))

(defn container! []
  (let [div (.createElement js/document "div")
        body (.-body js/document)
        cleanup #(.removeChild body div)]
    (.appendChild body div)
    [div cleanup]))

(defn on-click! [el]
  (set! (.-onclick el) #(dommy/set-text! (.-target %) "CLICKED")))

(defn was-clicked? [el]
  (= "CLICKED" (dommy/text el)))

(defn el! [tag container]
  (let [el (dommy/create-element tag)]
    (dommy/append! container el)
    el))

(deftest is-el? []
  (is (s/is-el? (.createElement js/document "div")))
  (is (not (s/is-el? "foo"))))


(deftest get-el []
  (let [[container cleanup] (container!)
        div (el! "div" container)]
    (dommy/add-class! div "foo")
    (is (= (s/get-el div) div))
    (is (= (s/get-el ".foo") div))
    (cleanup)))

(deftest clicking-el []
  (let [[container cleanup] (container!)
        div (el! "div" container)]
    (on-click! div)
    (syn-run!
     (<! (s/click! div))
     (is (was-clicked? div))
     (cleanup))))

(deftest clicking-el-by-selector []
  (let [[container cleanup] (container!)
        div (el! "div" container)]
    (dommy/add-class! div "foo")
    (on-click! div)
    (syn-run!
     (<! (s/click! ".foo"))
     (is (was-clicked? div))
     (cleanup))))

 
(deftest clicking-el-with-timeout [] 
  (let [[container cleanup] (container!)
        bootstrap (fn []
                    (let [div (el! "div" container)]
                      (on-click! div)
                      (dommy/add-class! div "bar")))]
    (.setTimeout js/window bootstrap 500)
    (syn-run!
      (<! (s/click! ".bar"))
      (is (was-clicked? (sel1 ".bar")))
      (cleanup))))



(deftest trying-to-click-non-existing-element-throws []
  (syn-run!
    (<! (s/click! ".non-existing-element"))
    (is false "This should never run")))
