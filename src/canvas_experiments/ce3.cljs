(ns canvas-experiments.ce3
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(def ticks-between-new-lines 60)
(def max-finished-lines 100)
(def max-growing-lines 100)
(def max-line-length 100)
(def branches 2)
(def line-colour-1 "hsl(50, 100%, 80%)")
(def line-colour-2 "hsl(230, 100%, 80%)")
(def line-speed 2)
(def opacity-loss 0.01)

(defn rand-dir [r]
  (let [theta (* 2 (.-PI js/Math) (rand))]
    [(* r (.cos js/Math theta))
     (* r (.sin js/Math theta))]))

(defn new-line 
  ([w h] (new-line [(rand-int w) (rand-int h)]))
  ([point]
   {:from point
    :to point
    :opacity 1
    :dir (rand-dir line-speed)}))

(defn sqr [x] (* x x))

(defn line-length [line]
  (.sqrt js/Math (apply + (map sqr (map - (:to line) (:from line))))))

(defn on-screen [line w h] ; just tests if both ends are off-screen
  (let [point-on (fn [[x y]]
                   (and (> x 0)
                        (< x w)
                        (> y 0)
                        (< y h)))]
    (or (point-on (:from line))
        (point-on (:to line)))))

(defn update-opacity [lines]
  (map (fn [line]
         (assoc line :opacity (- (:opacity line)
                                 opacity-loss)))
       (filter #(> (:opacity line) opacity-loss)
               lines)))

(defn brancher [colour w h]
  (monet/entity {:finished-lines []
                 :growing-lines [(new-line [(/ w 2) (/ h 2)])]}
                (fn [state]
                  (let [{new-finished-lines true, growing-lines false} (group-by #(>= (line-length %)
                                                                                      max-line-length)
                                                                                 (:growing-lines state))]
                    {:finished-lines (take max-finished-lines (update-opacity (concat new-finished-lines (:finished-lines state))))
                     :growing-lines (concat (take max-growing-lines (update-opacity (map #(assoc % :to (map + 
                                                                                            (:to %)
                                                                                            (:dir %)))
                                                                         growing-lines)))
                                            (filter #(on-screen % w h)
                                                    (mapcat (fn [line]
                                                              (take branches
                                                                    (repeatedly #(new-line (:to line)))))
                                                            new-finished-lines)))}))
                (fn [ctx state]
                  (monet/stroke-style ctx colour)
                  (monet/begin-path ctx)
                  (doseq [{:keys [from to]} (concat (:finished-lines state)
                                                    (:growing-lines state))]
                    (apply monet/move-to ctx from)
                    (apply monet/line-to ctx to))
                  (monet/close-path ctx)
                  (monet/stroke ctx))))

(defn entities [w h]
  {:brancher-1 (brancher line-colour-1 w h)
   :brancher-2 (brancher line-colour-2 w h)})

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
