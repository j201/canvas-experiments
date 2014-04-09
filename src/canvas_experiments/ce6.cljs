(ns canvas-experiments.ce6
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(def ripple-width 50)
(def ripple-separation 80)
(def ripple-colour {:s 0.5 :l 0.8})
(def ripples 8)
(def ripple-speed 2)
(def min-size 200)
(def max-size 800)
(def size-diff (- max-size min-size))
(def ripplers 12)

(defn s-alpha [s max-s]
  (max 0 (- 1 (/ s max-s))))

(defn draw-ripple [ctx x y s colour]
  (let [r (/ s 2)
        gradient (doto (.createRadialGradient ctx
                                              (+ x r) (+ y r) (max 0 (- r ripple-width))
                                              (+ x r) (+ y r) r)
                   (.addColorStop "0" "transparent")
                   (.addColorStop "0.5" colour)
                   (.addColorStop "1" "transparent"))]
    (set! (.-fillStyle ctx) gradient)
    (monet/fill-rect ctx {:x x, :y y, :w s, :h s})))

(defn rippler-init [w h]
  {:x (rand-int w)
   :y (rand-int h)
   :ss '(0)
   :max-s (+ min-size (rand-int size-diff))
   :colour (assoc ripple-colour :h (rand-int 360))})

(defn rippler [w h]
  (monet/entity (rippler-init w h)
                (fn [{:keys [ss max-s] :as state}]
                  (if (every? #(>= % max-s) ss)
                    (rippler-init w h)
                    (assoc state :ss
                           (let [updated-ss (map #(+ ripple-speed %) ss)]
                             (if (and (< (count updated-ss) ripples)
                                      (>= (first updated-ss) ripple-separation))
                               (cons 0 updated-ss)
                               updated-ss)))))
                (fn [ctx {:keys [x y ss max-s colour]}]
                  (doseq [s ss]
                    (let [alpha (s-alpha s max-s)
                          r (/ s 2)]
                      (when (> alpha 0)
                        (draw-ripple ctx (- x r) (- y r) s (utils/to-colour (assoc colour :a alpha)))))))))

(defn entities [w h]
  (into {}
        (map-indexed #(vector %1 %2)
                     (take ripplers
                           (repeatedly #(rippler w h))))))

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
