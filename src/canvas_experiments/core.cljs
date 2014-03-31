(ns canvas-experiments.core
  (:require [monet.canvas :as monet]
            [canvas-experiments.experiments :refer [experiments]]))

(def canvas (.getElementById js/document "canvas"))

(defn resize-canvas []
  (do
    (set! (.-width canvas) js/innerWidth)
    (set! (.-height canvas) js/innerHeight)))

(defn hash-num []
  (int (subs (.-hash js/location) 1)))

(when (= (.-hash js/location) "")
  (set! (.-hash js/location) "0"))

(def mcanvas (atom nil))

(defn show-experiment []
  (reset! mcanvas (monet/init canvas "2d"))
  ((nth experiments (hash-num))
   @mcanvas (.-width canvas) (.-height canvas)))

(defn reset []
  (when @mcanvas
    (monet/stop @mcanvas))
  (resize-canvas)
  (show-experiment))

(defn next-experiment []
  (set! (.-hash js/location) (mod (inc (hash-num))
                                 (count experiments)))
  (reset))
(defn prev-experiment []
  (set! (.-hash js/location) (mod (dec (hash-num))
                                 (count experiments)))
  (reset))

(js/addEventListener "resize" reset)

(js/addEventListener "keydown"
                     (fn [e]
                       (let [handler (case (.-keyCode e)
                                       39 next-experiment
                                       37 prev-experiment
                                       nil)]
                         (when handler
                             (handler)))))

(.addEventListener (.getElementById js/document "left-arrow") "click" prev-experiment)
(.addEventListener (.getElementById js/document "right-arrow") "click" next-experiment)

(reset)
