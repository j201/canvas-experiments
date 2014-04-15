(ns canvas-experiments.ce7
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(def time-between 40)
(def speed-frac 0.05)
(def min-pos 10)
(def max-pos 25)
(def pos-diff (- max-pos min-pos))
(def line-colour {:s 0.8})
(def stroke-width 5)
(def max-lines 7)

(defn update-lines [lines]
  (if (< (:distance lines) 1)
    (assoc lines :distance (+ speed-frac (:distance lines)))
    lines))

(defn rand-lines-pos [max-sum]
  (loop [result '()
         sum 0]
    (if (< sum max-sum)
      (let [new-sum (+ sum min-pos (rand-int pos-diff))]
        (recur (cons new-sum result) new-sum))
      result)))

(def sides #{:top :bottom :left :right})

(defn new-lines [w h]
  (let [side (rand-nth (seq sides))]
    {:colour (utils/rand-colour line-colour)
     :distance 0
     :side side
     :positions (rand-lines-pos (if (#{:top :bottom} side) w h))}))

(defn pos-from-side [side distance pos w h]
  (case side
    :top [pos (* distance h)]
    :bottom [pos (* (- 1 distance) h)]
    :left [(* distance w) pos]
    :right [(* (- 1 distance) w) pos]))

(defn line-sets [w h]
  (monet/entity {:lines '()
                 :time-to-next 0}
                (fn [{:keys [lines time-to-next]}]
                  (if (zero? time-to-next)
                    {:lines (take max-lines
                                  (cons (new-lines w h)
                                        (map update-lines lines)))
                     :time-to-next time-between}
                    {:lines (map update-lines lines)
                     :time-to-next (dec time-to-next)}))
                (fn [ctx state]
                  (monet/stroke-width ctx stroke-width)
                  (doseq [{:keys [colour distance side positions]} (reverse (:lines state))
                          pos positions]
                    (monet/stroke-style ctx colour)
                    (monet/begin-path ctx)
                    (apply monet/move-to ctx (pos-from-side side 0 pos w h))
                    (apply monet/line-to ctx (pos-from-side side distance pos w h))
                    (monet/stroke ctx)))))

(defn entities [w h]
  {:background (utils/background "white" w h)
   :line-sets (line-sets w h)})

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
