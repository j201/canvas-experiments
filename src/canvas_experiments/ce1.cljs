(ns canvas-experiments.ce1
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(def freq 0.2)
(def circle-speed-factor 5)

; period of 1
(defn osc [x]
  (+ 0.7 (* -0.3 (.cos js/Math (* x 2 Math/PI)))))

(defn update-circles [ticks w h circles]
  (let [dr (* circle-speed-factor (osc (* freq (utils/ticks->s ticks))))]
    (vec (drop-while #(> (:r %)
                         (max w h))
                     (map #(assoc % :r (+ dr (:r %)))
                          circles)))))

(defn entities [w h]
  {:circles (monet/entity {:ticks 0
                           :r-separation 20
                           :r-since-last 0
                           :max-sat 0
                           :circles []}
                          (fn [{:keys [ticks r-separation r-since-last max-sat circles] :as state}]
                            (let [pos (osc (* freq (utils/ticks->s ticks)))]
                              {:ticks (inc ticks)
                               :r-separation (- 20 (* 15 pos))
                               :r-since-last (if (> r-since-last
                                                    r-separation)
                                               0
                                               (inc r-since-last))
                               :max-sat pos
                               :circles (update-circles ticks w h
                                                        (if (> r-since-last
                                                               r-separation)
                                                          (conj circles {:r 0
                                                                         :colour (utils/to-colour {:h (mod (+ (rand-int 60)
                                                                                                              (* 360
                                                                                                                 (/ (utils/ticks->s ticks)
                                                                                                                    200)))
                                                                                                           360)
                                                                                                   :s max-sat
                                                                                                   :l max-sat})})
                                                          circles))}))
                          (fn [ctx {circles :circles}]
                            (.log js/console circles)
                            (doseq [{:keys [r colour]} circles]
                              (-> ctx
                                  (monet/fill-style colour)
                                  (monet/circle {:x (/ w 2)
                                                 :y (/ h 2)
                                                 :r r})))))})

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
