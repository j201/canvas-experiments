(ns canvas-experiments.ce1
  (:require [monet.canvas :as monet]))

(defn entities [w h]
  {:circle (monet/entity 1000 
                         (fn [n]
                           (max 0 (- n 10)))
                         (fn [ctx r]
                           (-> ctx
                               (monet/fill-style (str "hsl(" (rand-int 360) ", 50%, 50%)"))
                               (monet/circle {:x (/ w 2)
                                             :y (/ h 2)
                                             :r r}))))})

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
