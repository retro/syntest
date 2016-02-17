(ns syntest.core
  (:require [cljsjs.syn]
            [dommy.core :as dommy :refer-macros [sel1]]
            [cljs.core.async :refer [<! >! chan timeout put! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))
 
(enable-console-print!)

(def timeout-delay (atom 6000))

(defn is-el? [el]
  (<= 1 (.-nodeType el) 13))

(defn get-el [el]
  (if (is-el? el) el (sel1 el)))

(defn syn-perform! [action el opts]
  (let [action-name (str "_" (name action))
        output-chan (chan)]
    (.syn js/window action-name el opts #(put! output-chan true))
    output-chan))


(defn perform!
  ([action el] (perform! action el {}))
  ([action el opts]
   (let [output-chan (chan)] 
     (go
       (loop [time 0]
         (let [element (get-el el)]
           (if element
             (put! output-chan (<! (syn-perform! action element opts)))
             (if (< time @timeout-delay)
               (do
                 (<! (timeout 100))
                 (recur (+ time 100)))
               (do
                 (.error js/console (str "Couldn't find element " el " in " (/ @timeout-delay 1000) " seconds"))
                 (close! output-chan)))))))
     output-chan)))

(def click! (partial perform! :click))

(defn dblclick! [el options])

(defn type! [el text])

(defn key! [el key])

(defn drag! [el opts-or-target])


