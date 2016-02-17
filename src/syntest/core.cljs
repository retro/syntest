(ns syntest.core
  (:require [cljsjs.syn]
            [dommy.core :as dommy :refer-macros [sel1 sel]]
            [clojure.string :refer [lower-case join split]]
            [cljs.core.async :refer [<! >! chan timeout put! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))
 
(enable-console-print!)

(def test-timeout (atom 6000))

(def context (atom {:timeout 6000
                    :steps-timeout 0
                    :stop-current-test nil
                    :log []}))

(defn add-stop-fn! [stop-fn]
  (swap! context assoc :stop-current-test stop-fn))

(defn stop-current-test! []
  (let [stop-fn (:stop-current-test @context)]
    (swap! context dissoc :stop-current-test)
    (when stop-fn (stop-fn))))

(defn add-to-log! [type msg]
  (reset! context
          (assoc @context :log (conj (:log @context) [type msg]))))

(defn is-el? [el]
  (<= 1 (.-nodeType el) 13))

(defn get-el [el]
  (if (is-el? el) el (sel1 el)))

(defn base-selector [node-name classes]
  (if (empty? classes)
    node-name
    (str node-name "." (join "." (split classes #" ") ))))

(defn el-id [el]
  (let [id (dommy/attr el "id")]
    (if-not (empty? id)
      (str "#" id)
      nil)))

(defn el->selector [el]
  (if (string? el)
    el
    (let [id (el-id el) 
          node-name (lower-case (.-nodeName el))
          classes (dommy/class el)]
      (if id 
        id
        (let [selector
              (loop [parent (dommy/parent el)
                     selector (list (base-selector node-name classes))]
                (if (and parent (not= (lower-case (.-nodeName parent)) "html"))
                  (if-let [parent-id (el-id parent)]
                    (conj selector parent-id)
                    (recur (dommy/parent parent) selector))
                  selector))
              others (sel selector)]
          (str (join " " selector) "[" (.indexOf others el) "]"))))))

(defn syn-perform! [output-chan action el opts]
  (let [action-name (str "_" (name action))]
    (.syn js/window action-name el opts #(put! output-chan true))
    output-chan))

(defn perform!
  ([action el] (perform! action el {}))
  ([action el opts]
   (let [output-chan (chan)
         selector (el->selector el)] 
     (go
       (loop [time 0]
         (if-let [element (get-el el)]
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
                                   :selector selector
                                   :wait (/ (:timeout @context) 1000)})
               (stop-current-test!)
               )))))
     output-chan)))

(def click! (partial perform! :click))
(def dblclick! (partial perform! :dblclick))
(def type! (partial perform! :type))
(def key! (partial perform! :key))
(def drag! (partial perform! :drag))
