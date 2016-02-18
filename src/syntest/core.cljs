(ns syntest.core
  (:require [cljsjs.syn] 
            [cljs.core.async :refer [<! >! chan timeout put! close!]]
            [cljs.test :refer [do-report]]
            [syntest.util :refer (el->selector)]
            [syntest.dom :as dom])
  (:require-macros [cljs.core.async.macros :refer [go]]))
 
(enable-console-print!)

(def test-timeout (atom 6000))

(def context (atom {:timeout 6000
                    :steps-timeout 0
                    :stop-current-test nil
                    :log []}))

(defn timeout-s []
  (/ (:timeout @context) 1000))

(defn report-error! [msg]
  (do-report {:type :fail,
              :message (str "Failed to" (name (:type msg)) " on " (:selector msg) " after " (:wait msg) "s")
              :expected (:selector msg)}))

(defn add-stop-fn! [stop-fn]
  (swap! context assoc :stop-current-test stop-fn))

(defn stop-current-test! []
  (let [stop-fn (:stop-current-test @context)]
    (swap! context dissoc :stop-current-test)
    (when stop-fn (stop-fn))))

(defn add-to-log! [type msg]
  (let [msg-with-wait (merge msg {:wait (timeout-s)})]    
    (when (= type :error)
      (report-error! msg-with-wait))
    (reset! context
            (assoc @context :log (conj (:log @context) [type msg-with-wait])))))

(defn syn-perform! [output-chan action el opts]
  (let [action-name (str "_" (name action))]
    (.syn js/window action-name el opts #(put! output-chan el))
    output-chan))

(defn perform!
  ([action el] (perform! action el {})) 
  ([action el opts]
   (let [output-chan (chan)
         selector (el->selector el)] 
     (go
       (loop [time 0]
         (if-let [element (dom/sel1 el)] 
           (do
             (add-to-log! :before {:type action
                                   :selector selector})
             (.setTimeout js/window
                          (fn []
                            (add-to-log! :action {:type action
                                                  :selector selector})
                            (syn-perform! output-chan action element opts))
                          (:steps-timeout @context)))
           (if (< time (:timeout @context))
             (do
               (<! (timeout 100))
               (recur (+ time 100)))
             (do
               (add-to-log! :error {:type action
                                   :selector selector})
               (stop-current-test!))))))
     output-chan)))

(defn match-one [predicate elements]
  (some true? (filter predicate elements)))

(defn match-count [predicate match-count elements]
  (= match-count (count (filter predicate elements))))

(defn wait!
  ([els] (wait! els (fn [] true)))
  ([els predicate] (wait! els predicate nil))
  ([els predicate match-count]
   (let [output-chan (chan)
         
         matcher (if (nil? match-count)
                   (partial match-one predicate)
                   (partial match-count predicate match-count))]
     (go
       (loop [time 0]
         (let [elements (dom/sel els)]
           (if-not (empty? elements) 
             (when (matcher elements)
               (>! output-chan true))
             (if (< time (:timeout @context))
               (do
                 (<! (timeout 100))
                 (recur (+ time 100)))
               (do
                 (add-to-log! :error {:type :wait
                                      :selector (el->selector elements false)})
                 (stop-current-test!)))))))
     output-chan)))

(def click! (partial perform! :click))
(def dblclick! (partial perform! :dblclick))
(def type! (partial perform! :type))
(def key! (partial perform! :key))
(def drag! (partial perform! :drag))
