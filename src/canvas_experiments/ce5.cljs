(ns canvas-experiments.ce5
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(def body-length 120)
(def body-width 20)
(def row-length 50)
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
        (let [perpendicular ((case side :right - :left +)
                             (/ (.-PI js/Math) 2))
              origin (map + pos (map #(* % (/ body-width 2))
                                     (utils/polar-to-cart (+ theta perpendicular) 1)))]
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

(def max-speed 4)
(def max-row-angle (/ (.-PI js/Math) 4))
(def osc-divisor 20)

(defn speed-osc [ticks]
  (* max-speed
     (max 0 (.sin js/Math (/ ticks osc-divisor)))))

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
                 (when (utils/point-in next-pos [0 0] [w h])
                   {:ticks (inc ticks)
                    :pos next-pos})))
     :draw (fn [ctx {:keys [pos ticks]}]
             (draw-rower-body ctx pos dir style)
             (draw-rows ctx pos theta (row-osc ticks) style))}))

(defn rand-side-point [w h]
  (let [side (rand-int 3)]
    (case side
      0 [0 (* (rand h))]
      1 [(* (rand w)) 0]
      2 [w (* (rand h))]
      3 [(* (rand w)) h])))

(def no-of-rowers 5)
(def rower-colour {:s 0.5 :l 0.7})

(defn new-rower [w h]
  (rower (rand-side-point w h)
         (* (rand) 2 (.-PI js/Math))
         (utils/rand-colour rower-colour)
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

(defn entities [w h]
  {:rowers (rowers w h)})

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
