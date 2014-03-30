(ns canvas-experiments.core
  (:require [monet.canvas :as monet]
            [canvas-experiments.experiments :refer [experiments]]))

(def canvas (.getElementById js/document "canvas"))

(defn resize-canvas []
  (do
    (set! (.-width canvas) js/innerWidth)
    (set! (.-height canvas) js/innerHeight)))

(when (= (.-hash js/location) "")
  (set! (.-hash js/location) "0"))

(def mcanvas (atom nil))

(defn show-experiment []
  (reset! mcanvas (monet/init canvas "2d"))
  ((nth experiments (int (.-hash js/location)))
   @mcanvas (.-width canvas) (.-height canvas)))

(defn init []
  (resize-canvas)
  (show-experiment))

(js/addEventListener "resize"
                     (fn []
                       (monet/stop @mcanvas)
                       (init)))

(init)
