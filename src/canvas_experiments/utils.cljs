(ns canvas-experiments.utils)

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
             :s (rand-int 101)
             :l (rand-int 101)}
            colour))))

(defn ticks->s [x] (/ x 60))
