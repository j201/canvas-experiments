(ns canvas-experiments.ce4
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(defn along-line-segment [t [[x1 y1] [x2 y2]]]
  [(+ x1 (* t (- x2 x1)))
   (+ y1 (* t (- y2 y1)))])

;; A shape is a list of points

(defn rand-divide [shape]
  (let [sides (sort (utils/distinct-rands 2 (range (count shape))))
        sides-line (for [side sides]
                     (along-line-segment (rand)
                                         [(nth shape side)
                                          (nth shape (mod (inc side)
                                                          (count shape)))]))]
    [(concat (reverse sides-line)
             (map #(nth shape %)
                  (apply range (map inc sides))))
     (concat sides-line
             (map #(nth shape %)
                  (map #(mod % (count shape))
                       (range (inc (second sides))
                              (+ 1 (count shape) (first sides))))))]))

(def min-fragment-size 20000)
(def fill-colour {:s 0.8 :l 0.6})
(def stroke-colour {:s 0.8 :l 0.2})
(def fragment-speed 6)
(def split-period 5)
(def hue-increment 0.2)

(defn next-hue [hue]
  (mod (+ hue hue-increment)
       360))

(defn move-fragment [fragment w h]
  (let [dir (map #(* fragment-speed %)
                 (utils/normalize-coord (map - (first fragment) [(/ w 2) (/ h 2)])))]
    (map #(map + dir %)
         fragment)))

(defn entities [w h]
  {:rect 
   (monet/entity {:fragments [[[(* w 0.25) (* h 0.25)]
                               [(* w 0.75) (* h 0.25)]
                               [(* w 0.75) (* h 0.75)]
                               [(* w 0.25) (* h 0.75)]]]
                  :previous-state nil
                  :time-to-split 0
                  :hue 0
                  :rewinding false}
                 (fn [{:keys [fragments previous-state time-to-split hue rewinding] :as state}]
                   (if (and rewinding previous-state)
                     (assoc previous-state :rewinding true :hue (next-hue hue))
                     (if (zero? time-to-split)
                       (let [{finished true
                              to-split false} (group-by #(< (utils/polygon-area %)
                                                            min-fragment-size)
                                                        fragments)]
                         {:fragments (map #(move-fragment % w h)
                                          (concat (mapcat rand-divide to-split)
                                                  finished))
                          :previous-state state
                          :time-to-split split-period
                          :hue (next-hue hue)
                          :rewinding (zero? (count to-split))})
                       {:fragments (map #(move-fragment % w h) fragments)
                        :previous-state state
                        :time-to-split (dec time-to-split)
                        :hue (next-hue hue)
                        :rewinding false})))
                 (fn [ctx {:keys [fragments hue]}]
                   (monet/stroke-style ctx (utils/to-colour (assoc stroke-colour :h hue)))
                   (monet/fill-style ctx (utils/to-colour (assoc fill-colour :h hue)))
                   (monet/stroke-width ctx 5)
                   (doseq [fragment fragments]
                     (monet/begin-path ctx)
                     (apply monet/move-to ctx (first fragment))
                     (doseq [point (rest fragment)]
                       (apply monet/line-to ctx point))
                     (apply monet/line-to ctx (first fragment))
                     (monet/close-path ctx)
                     (monet/fill ctx)
                     (monet/stroke ctx))))})

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
