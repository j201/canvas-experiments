(ns canvas-experiments.ce9
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(def bar-width 10)
(def bar-padding 3)
(def bar-space (+ bar-width bar-padding))
(def colours (map utils/to-colour
                  (map (fn [hue]
                         {:h (mod hue 360)
                          :s 1
                          :l 0.5
                          :a 0})
                       (range 0 360 90))))
(def height-fn-period 300)
(def wave-point-separation 60)

(defn draw-bar [ctx colour x y centre-y]
  (set! (.-fillStyle ctx) (doto (.createLinearGradient ctx 0 centre-y 0 (+ y centre-y))
                            (.addColorStop "0" colour)
                            (.addColorStop "1" "white")))
  (monet/rect ctx {:x x
                   :y centre-y
                   :w bar-width
                   :h y}))

(defn waveform [w colour wave-fn height-fn]
  (monet/entity 0 inc
                (fn [ctx ticks]
                  (let [bar-ys (->> (range 0 w bar-space)
                                    (map #(- ticks %))
                                    (map wave-fn))
                        height (height-fn ticks)]
                    (doseq [[n y] (map-indexed vector bar-ys)]
                      (draw-bar ctx colour (- w (* bar-space n)) y height))))))

(defn rand-ish [seed] ; follows a sin distribution, expects high seeds (usu. above 1), from -1 to 1
  (.sin js/Math (* 1000 seed seed)))

(defn surrounding-wave-points [x]
  [(+ x (- wave-point-separation
           (mod x wave-point-separation)))
   (- x (mod x wave-point-separation))])

(defn interpolate [x1 y1 x2 y2 x3] ; x3 should be between x1 and x2
  (let [frac (/ (- x3 x1)
                (- x2 x1))]
    (+ (* (- 1 frac) y1)
       (* frac y2))))

(defn wave-fn [max-val]
  (memoize (fn [ticks]
             (let [[x1 x2] (surrounding-wave-points ticks)
                   [y1 y2] (map #(* max-val (rand-ish %)) [x1 x2])]
               (interpolate x1 y1 x2 y2 ticks)))))

(defn height-fn [h start]
  (let [osc (utils/osc start height-fn-period)
        max-val (/ h 4)
        centre (/ h 2)]
    (fn [ticks]
      (+ centre (* max-val (dec (* 2 (osc ticks))))))))

(defn show [mcanvas w h]
  (doseq [[n colour] (map-indexed vector colours)]
    (monet/add-entity mcanvas n
                      (waveform w
                                colour
                                (wave-fn (/ h 4))
                                (height-fn h (/ n (count colours)))))))
