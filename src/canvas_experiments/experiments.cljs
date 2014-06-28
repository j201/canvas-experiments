;; clojurescript doesn't support defmacro, and i'm too lazy to figure out how to import them

(ns canvas-experiments.experiments
  (:require canvas-experiments.ce1
            canvas-experiments.ce2
            canvas-experiments.ce3
            canvas-experiments.ce4
            canvas-experiments.ce5
            canvas-experiments.ce6
            canvas-experiments.ce7
            canvas-experiments.ce8
            canvas-experiments.ce9
            canvas-experiments.ce10
            ))

(def experiments
  [
   canvas-experiments.ce10/show
   canvas-experiments.ce9/show
   canvas-experiments.ce8/show
   canvas-experiments.ce7/show
   canvas-experiments.ce6/show
   canvas-experiments.ce5/show
   canvas-experiments.ce4/show
   canvas-experiments.ce3/show
   canvas-experiments.ce2/show
   canvas-experiments.ce1/show])
