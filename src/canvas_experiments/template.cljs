(ns canvas-experiments.ce6
  (:require [monet.canvas :as monet]
            [canvas-experiments.utils :as utils]))

(defn entities [w h]
  {})

(defn show [mcanvas w h]
  (doseq [[kw e] (entities w h)]
    (monet/add-entity mcanvas kw e)))
