(ns chessground.drag
  "Make pieces draggable, and squares droppable"
  (:require [jayq.core :as jq :refer [$]]
            [chessground.common :as common :refer [pp push!]]
            [chessground.chess :as chess]))

(def dragging-class "dragging")
(def drag-over-class "drag-over")

(defn on-start [event chans]
  (-> event .-target .-classList (.add dragging-class))
  (push! (:move-start chans) (-> event .-target .-parentNode (.getAttribute "data-key"))))

(defn on-move [event]
  (let [target (.-target event)
        x (+ (or (.-x target) 0) (.-dx event))
        y (+ (or (.-y target) 0) (.-dy event))
        transform (str "translate(" x "px, " y "px)")]
    (set! (.-x target) x)
    (set! (.-y target) y)
    (aset (.-style target) common/transform-prop transform)))

(defn make-draggable [el chans]
  (let [obj (js/interact el)]
    (jq/data ($ el) :interact obj)
    (.draggable obj (clj->js {:onstart #(on-start % chans)
                              :onmove on-move}))))

(defn piece-on [el]
  (.set (jq/data ($ el) :interact) (js-obj "draggable" true)))

(defn piece-off [el]
  (.set (jq/data ($ el) :interact) (js-obj "draggable" false)))

(defn square [el chans]
  (let [obj (js/interact el)]
    (.dropzone obj true)
    (.on obj "dragenter"
         (clj->js (fn[event] (-> event .-target .-classList (.add drag-over-class)))))
    (.on obj "dragleave"
         (clj->js (fn[event] (-> event .-target .-classList (.remove drag-over-class)))))
    (.on obj "drop"
         (clj->js (fn[event]
                    (let [piece (.-relatedTarget event)
                          orig (.-parentNode piece)
                          dest (.-target event)]
                      (push! (:move-piece chans) (map common/square-key [orig dest]))
                      (.remove (.-classList piece) dragging-class)
                      (.remove (.-classList dest) drag-over-class)))))))

(defn unfuck [piece-el]
  (set! (.-x piece-el) 0)
  (set! (.-y piece-el) 0)
  (aset (.-style piece-el) common/transform-prop ""))
