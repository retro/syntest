(ns syntest.util
  (:require [syntest.dom :as dom]
            [clojure.string :refer [lower-case join split]]))


(defn base-selector [node-name classes]
  (if (empty? classes)
    node-name
    (str node-name "." (join "." (split classes #" ")))))

(defn el-id [el]
  (let [id (dom/id el)]
    (if-not (empty? id)
      (str "#" id)
      nil)))

(defn el->selector
  ([el] (el->selector el true))
  ([el calculate-index?]
   (if (string? el)
     el
     (let [id (dom/id el) 
           node-name (lower-case (.-nodeName el))
           classes (dom/class el)]
       (if id 
         id
         (let [selectors
               (loop [parent (dom/parent el)
                      selector (list (base-selector node-name classes))]
                 (if (and parent (not= (lower-case (.-nodeName parent)) "html"))
                   (if-let [parent-id (el-id parent)]
                     (conj selector parent-id)
                     (recur (dom/parent parent) selector))
                   selector))
               selector (join " " selectors)]
           (if calculate-index?
             (str selector "[" (.index (dom/sel selector) el) "]")
             selector)))))))
