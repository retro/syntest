(ns syntest.predicates
  (:require [cljsjs.jquery]
            [jayq.core :as jayq]))

(defn predicate-applier [predicate]
  (fn [el]
    (predicate (jayq/$ el))))

(defn height [n]
  (predicate-applier #(= n (.height %))))

(defn inner-height [n]
  (predicate-applier #(= n (.innerHeight %))))

(defn outer-height [n]
  (predicate-applier #(= n (.outerHeight %))))

(defn width [n]
  (predicate-applier #(= n (.width %))))

(defn outer-width [n]
  (predicate-applier #(= n (.outerWidth %))))

(defn inner-width [n]
  (predicate-applier #(= n (.innerWidth %))))

(defn scroll-left [n]
  (predicate-applier #(= n (.scrollLeft %))))

(defn scroll-top [n]
  (predicate-applier #(= n (.scrollTop %))))

(defn exists []
  (predicate-applier #(> 0 (count %))))

(defn missing []
  (predicate-applier #(= 0 (count %))))

(defn visible []
  (predicate-applier #(.is % ":visible")))

(defn invisible []
  (predicate-applier #(.is % ":hidden")))

(defn coordinates [calc-fn {:keys [top left]}]
  (cond
   (nil? top) (predicate-applier #(= left (.-left (calc-fn %))))
   (nil? left) (predicate-applier #(= top (.-top (calc-fn))))
   :else (predicate-applier (fn [el]
                              (let [offset (calc-fn)]
                                (and (= left (.-left offset))
                                     (= top (.-top offset))))))))

(def offset (partial coordinates #(.offset %)))
(def position (partial coordinates #(.position %)))
