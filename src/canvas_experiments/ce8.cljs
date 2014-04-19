(ns canvas-experiments.ce8
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(def delta (/ (.-PI js/Math) 2))
(def tick-factor 0.004)
(def radius 20)
(def l-param-min 2)
(def l-param-max 7)

(defn liss-pos [a b A B ticks]
  [(* A 0.5 (+ 1 (.sin js/Math (+ delta (* a ticks tick-factor)))))
   (* B 0.5 (+ 1 (.sin js/Math (* b ticks tick-factor))))])

(defn bw-flip [colour]
  (if (= colour "black") "white" "black"))

(defn get-colour [mc]
  (or (:colour (monet/get-entity mc :colour))
      "black"))

(defn next-liss-dot [a b A B ticks]
  {:ticks (inc ticks)
   :pos (map + (repeat radius) (liss-pos a b A B ticks))})

(defn draw-liss-dot [mc ctx {:keys [pos]}]
  (let [colour (get-colour mc)]
    (set! (.-shadowColor ctx) colour)
    (set! (.-shadowBlur ctx) 30)
    (monet/fill-style ctx colour)
    (monet/circle ctx {:x (first pos)
                       :y (second pos)
                       :r radius})))

(defn liss-dot [mc a b w h]
  (let [[A B] (map - [w h] (repeat (* 2 radius)))]
    (monet/entity (next-liss-dot a b A B 0)
                  #(next-liss-dot a b A B (:ticks %))
                  (partial draw-liss-dot mc))))

(defn get-dots [mc]
  (for [m (range l-param-min (inc l-param-max))
        n (range l-param-min (inc l-param-max))]
    (monet/get-entity mc (keyword (str "ld" m n)))))

(defn colour-switcher [mc]
  (monet/entity {:colour "white"
                 :ticks-since-changed 0}
                (fn [state]
                  (let [dots (get-dots mc)]
                    (if (and (> (:ticks-since-changed state) 100) ; hackish condition, but meh
                             (utils/log (every? #(< % 10)
                                     (map (fn [d1 d2]
                                            (apply max
                                                   (map - (:pos d1) (:pos d2))))
                                          (rest dots) (drop-last 1 dots)))
                                        true?))
                      {:colour (bw-flip (:colour state))
                       :ticks-since-changed 0}
                      (update-in state [:ticks-since-changed] inc))))
                identity))

(defn bg [mc w h]
  (monet/entity nil
                identity
                (fn [ctx]
                  (monet/fill-style ctx (bw-flip (get-colour mc)))
                  (monet/fill-rect ctx {:x 0 :y 0 :w w :h h}))))

(defn show [mcanvas w h]
  (monet/add-entity mcanvas :colour (colour-switcher mcanvas))
  (monet/add-entity mcanvas :bg (bg mcanvas w h))
  (doseq [m (range l-param-min (inc l-param-max))
          n (range l-param-min (inc l-param-max))]
    (monet/add-entity mcanvas
                      (keyword (str "ld" m n))
                      (liss-dot mcanvas m n w h))))
