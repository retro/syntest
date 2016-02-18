(ns syntest.dom
  (:require [cljsjs.jquery]
            [jayq.core :as jayq]))

(.noConflict (.-jQuery js/window))

(defn sel [selector]
  (jayq/$ selector))

(defn sel1 [selector]
  (first (sel selector)))

(defn id [selector]
  (jayq/attr (sel selector) :id))

(defn class [selector]
  (jayq/attr (sel selector) :class))

(defn parent [selector]
  (first (jayq/parent (sel selector))))
