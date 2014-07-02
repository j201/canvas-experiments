(ns canvas-experiments.music)

; All notes are semitones from middle c
; All durations are in s

(def context (js/AudioContext.))

(defn freq [note]
  (* 261.626 ; middle c
     (.pow js/Math
           (.pow js/Math 2 (/ 1 12))
           note)))

(def e-dorian-chords
  [[-8 -5 -1 4]
   [-10 -3 2 4]
   [-8 -5 1 4] 
   [-8 0 2 6]
   [-8 -5 -1 4]
   [-7 -5 0 4]
   [-10 -5 2 4]
   [-10 -3 2 6]])

(def e-dorian
  [4 6 7 9 11 13 14 16])

(defn set-freq! [osc freq]
  (set! (.-value (.-frequency osc)) freq))

(defn play-note [osc freq start duration]
  (set! (.-value (.-frequency osc)) freq) 
  (.start osc start)
  (.stop osc (+ start duration)))

(defn automate! [param coords]
  (.setValueAtTime param (ffirst coords) (second (first coords)))
  (doseq [[v t] (rest coords)]
    (.linearRampToValueAtTime param v t)))

(defn adsr!
  "a, d, r: seconds, s: 0 to 1"
  [param a d s r start duration]
  (automate! param (map (fn [[v t]] [v (+ t start)])
                        [[0 0]
                         [1 a]
                         [s (+ d a)]
                         [s duration]
                         [0 (+ r duration)]])))

(defn adsr-gain [a d s r start duration]
  (let [gain (.createGain context)]
    (adsr! (.-gain gain) a d s r start duration)
    gain))

(defn biquad [type' freq q gain]
  (let [bq (.createBiquadFilter context)]
    (set! (.-type bq) (str type'))
    (set! (.-value (.-frequency bq)) freq)
    (set! (.-value (.-Q bq)) q)
    (set! (.-value (.-gain bq)) gain)
    bq))

(defn chain-nodes! [& nodes]
  (reduce #(do (.connect %1 %2) %2)
          nodes))

(defn chord-pad-osc [note start duration]
  (let [osc (.createOscillator context)
        gain (adsr-gain 0.007 0.1 0.5 0.2 start duration)
        lpf (biquad "lowpass" 1000 0.7 0)
        vol (.createGain context)]
    (set! (.-type osc) "triangle")
    (set! (.-value (.-gain vol)) 0.1)
    (chain-nodes! osc gain lpf vol)
    (.connect vol (.-destination context))
    (play-note osc (freq note) start (+ duration 0.2))))

(defn play-test! []
  (doseq [n (range (count e-dorian-chords))
          note (nth e-dorian-chords n)]
    (chord-pad-osc note (+ (* (rand) 0.03)
                           (* n 1.8))
                   4)))
