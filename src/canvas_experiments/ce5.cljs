(ns canvas-experiments.ce5
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(def body-length 120)
(def body-width 20)
(def row-length 20)
(def rows-per-side 16)
(def row-separation (/ body-length rows-per-side))

(defn draw-rower-body [ctx pos dir style]
  (-> ctx
      (monet/stroke-style style)
      (monet/stroke-width body-width)
      (monet/stroke-cap "round")
      (monet/begin-path))
  (apply monet/move-to ctx pos) 
  (apply monet/line-to ctx (map + pos
                                (map #(* body-length %) dir)))
  (monet/stroke ctx))

(defn draw-rows [ctx pos theta offset-angle style]
  (let [dir (utils/polar-to-cart theta 1)]
    (do
      (-> ctx
          (monet/stroke-style style)
          (monet/stroke-width 1))
      (doseq [side [:right :left]]
        (let [perpendicular (+ theta ((case side :right - :left +)
                                      (/ (.-PI js/Math) 2)))
              origin (map + pos (map #(* % (/ body-width 2))
                                     (utils/polar-to-cart perpendicular 1)))]
          (doseq [row-index (range (inc rows-per-side))]
            (let [line-start (map + origin
                                  (map #(* % (* row-index row-separation))
                                       dir))]
              (monet/begin-path ctx)
              (apply monet/move-to ctx line-start)
              (apply monet/line-to ctx (map + line-start
                                            (utils/polar-to-cart (+ perpendicular ((case side :right + :left -)
                                                                                   offset-angle))
                                                                 row-length)))
              (monet/close-path ctx)
              (monet/stroke ctx))))))))

(def min-speed 2)
(def max-speed 8)
(def speed-diff (- max-speed min-speed))
(def max-row-angle (/ (.-PI js/Math) 4))
(def osc-divisor 20)

(defn speed-osc [ticks]
  (+ min-speed
     (* speed-diff
        (max 0 (.sin js/Math (/ ticks osc-divisor))))))

(defn row-osc [ticks]
  (* max-row-angle
     (.cos js/Math (/ ticks osc-divisor))))

(defn rower [pos theta style w h]
  (let [dir (utils/polar-to-cart theta 1)]
    {:state {:ticks 0
             :pos pos}
     :update (fn [{:keys [ticks pos]}]
               (let [next-pos (map + pos
                                   (map #(* % (speed-osc ticks))
                                        dir))]
                 (when (utils/point-in next-pos (repeat 2 (- body-length))
                                       [(+ body-length w) (+ body-length h)])
                   {:ticks (inc ticks)
                    :pos next-pos})))
     :draw (fn [ctx {:keys [pos ticks]}]
             (draw-rower-body ctx pos dir style)
             (draw-rows ctx pos theta (row-osc ticks) style))}))

(defn rand-side-point [x y w h]
  (let [side (rand-int 3)]
    (case side
      0 [x (* (rand h))]
      1 [(* (rand w)) y]
      2 [w (* (rand h))]
      3 [(* (rand w)) h])))

(def no-of-rowers 10)
(def rower-colour {:h 0 :s 0 :l 1 :a 0.8})

(defn new-rower [w h]
  (rower (rand-side-point (- body-length)
                          (- body-length)
                          (+ body-length w)
                          (+ body-length h))
         (* (rand) 2 (.-PI js/Math))
         (utils/to-colour rower-colour)
         w h))

(defn rowers [w h]
  (monet/entity (take no-of-rowers (repeatedly #(new-rower w h)))
                (fn [rowers]
                  (let [updated (filter #(not (nil? (:state %)))
                                        (map #(assoc % :state ((:update %) (:state %)))
                                             rowers))]
                    (concat updated
                            (take (- no-of-rowers (count updated))
                                  (repeatedly #(new-rower w h))))))
                (fn [ctx rowers]
                  (doseq [rower rowers]
                    ((:draw rower) ctx (:state rower))))))

(def bg-osc-period 1200)
(def bg-osc (utils/osc 0 bg-osc-period))
(def bg-colour {:s 0.8 :l 0.2})
(def bg-min-hue 180)
(def bg-max-hue 240)
(def bg-hue-diff (- bg-max-hue bg-min-hue))

(defn bg [w h]
  (monet/entity 0
                inc
                (fn [ctx ticks]
                  (monet/fill-style ctx (utils/to-colour (assoc bg-colour :h (+ bg-min-hue
                                                                                (* bg-hue-diff (bg-osc ticks))))))
                  (monet/fill-rect ctx {:x 0, :y 0, :w w, :h h}))))

(defn entities [w h]
  {:bg (bg w h)
   :rowers (rowers w h)})

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
