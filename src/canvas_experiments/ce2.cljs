(ns canvas-experiments.ce2
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(def circle-radius 30)
(def circle-padding 5)
(def circle-distance (+ (* 2 circle-radius) circle-padding))
(def circle-set-rows 5)
(def circle-set-total-width (* circle-set-rows circle-distance))
(def circle-set-speed 3)
(def circle-set-recolour-interval 100)
(def radius-osc (utils/osc 0.5 100))
(def radius-variance -10)

(defn add-cart-polar [x y angle r]
  [(+ x (* r (.cos js/Math angle)))
   (+ y (* r (.sin js/Math angle)))])

(defn circle-set-colour []
  (utils/rand-colour {:s 0.8 :l 0.5}))

(defn num-columns [vertical w h]
  (/ (if vertical h w)
     circle-distance))

(defn circle-set [start-x start-y speed vertical w h comp-op]
  [{:x 0
    :y 0
    :ticks 1
    :colour (circle-set-colour)}
   (fn [cs]
     {:x (mod (+ (:x cs) speed)
              circle-distance)
      :y (mod (+ (:y cs) speed)
              circle-distance)
      :ticks (inc (:ticks cs))
      :colour (if (zero? (mod (:ticks cs) circle-set-recolour-interval))
                (circle-set-colour)
                (:colour cs))})
   (fn [ctx cs]
     (set! (.-globalCompositeOperation ctx) comp-op)
     (monet/fill-style ctx (:colour cs))
     (doseq [col (range (inc (num-columns vertical w h)))
             row (range circle-set-rows)]
       (monet/circle ctx {:x (if vertical
                               (+ start-x (* row circle-distance))
                               (- (+ start-x (:x cs) (* col circle-distance))
                                  circle-distance))
                          :y (if vertical
                               (- (+ start-y (:y cs) (* col circle-distance))
                                  circle-distance)
                               (+ start-y (* row circle-distance)))
                          :r (+ circle-radius
                                (* radius-variance (radius-osc (:ticks cs))))}))
     (set! (.-globalCompositeOperation ctx) "source-over"))])


(defn entities [w h]
  {:cs1 (apply monet/entity (circle-set 0 (- (/ h 2) 200) circle-set-speed false w h "xor"))
   :cs2 (apply monet/entity (circle-set (- (/ w 2) 200) 0 circle-set-speed true w h "xor"))
   :cs3 (apply monet/entity (circle-set 0  (- (/ h 2) 100) (- circle-set-speed) false w h "xor"))
   :cs4 (apply monet/entity (circle-set (- (/ w 2) 100) 0 (- circle-set-speed) true w h "xor"))})

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
