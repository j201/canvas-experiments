(ns canvas-experiments.utils
  (:require [monet.canvas :as monet]))

; s and l are in float notation, not percentange
(defn to-colour [{:keys [h s l a]}]
  (str (if a "hsla(" "hsl(")
       (or h 0)
       ", "
       (if s
         (int (* 100 s)) 
         100)
       "%, "
       (if l
         (int (* 100 l))
         100)
       "%"
       (if a
         (str ", " a)
         "")
       ")"))

(defn rand-colour 
  ([] (rand-colour {}))
  ([colour]
   (to-colour
     (merge {:h (rand-int 360)
             :s (rand)
             :l (rand)}
            colour))))

(defn ticks->s [x] (/ x 60))

(defn osc
  "A sinusoidal oscillator from 0 to 1"
  [start period]
  (let [start-phase (.asin js/Math (dec (* 2 start)))]
    (fn [x]
      (+ 0.5
         (* 0.5
            (.sin js/Math (+ start-phase
                             (* 2 Math/PI
                                (/ x period)))))))))

(defn log 
  ([thing]
   (.log js/console thing)
   thing)
  ([thing pred]
    (if (pred thing) (log thing) thing)))

(defn line-segment-intersection [[[x1 y1] [x2 y2]] [[x3 y3] [x4 y4]]]
  (let [t (/ (+ (* x4 (- y1 y3))
                (* x1 (- y3 y4))
                (* x3 (- y4 y1)))
             (+ (* x4 (- y1 y2))
                (* x3 (- y2 y1))
                (* (- x1 x2)
                   (- y3 y4))))]
    (if (or (= t js/Infinity)
            (< t 0)
            (> t 1))
      nil
      [(+ x1 (* t (- x2 x1)))
       (+ y1 (* t (- y2 y1)))])))

(defn distinct-rands "n different random elements from sq" [n sq]
  (if (<= n 0)
    []
    (let [x (rand-int (count sq))]
      (conj (distinct-rands (dec n)
                            (concat (take x sq)
                                    (drop (inc x) sq)))
            (nth sq x)))))

(defn polar-to-cart [theta r]
  [(* r (.cos js/Math theta))
   (* r (.sin js/Math theta))])

(defn rand-dir [r]
  (let [theta (* 2 (.-PI js/Math) (rand))]
    (polar-to-cart theta r)))

(defn transform [mtx]
  (apply map list mtx))

(defn polygon-area "shoelace formula" [polygon]
  (let [[xs ys] (transform polygon)]
    (* 0.5
       (.abs js/Math
             (+ (reduce + (map * (drop-last 1 xs) (drop 1 ys)))
                (* (last xs) (first ys))
                (- (reduce + (map * (drop-last 1 ys) (drop 1 xs))))
                (- (* (last ys) (first xs))))))))

(defn normalize-coord "convert to a radius of 1" [coord]
  (let [theta (.atan2 js/Math (second coord) (first coord))]
    (polar-to-cart theta 1)))

(defn point-in [[xp yp] [x1 y1] [x2 y2]]
  (and (and (>= xp x1)
            (<= xp x2))
       (and (>= yp y1)
            (<= yp y2))))

(defn background [colour w h]
  (monet/entity nil
                identity
                (fn [ctx]
                  (monet/fill-style ctx colour)
                  (monet/fill-rect ctx {:x 0 :y 0 :w w :h h}))))

(defn all-equal? [sq]
  (cond
    (empty? (rest sq)) true
    (not= (first sq) (second sq)) false
    :else (recur (rest sq))))
