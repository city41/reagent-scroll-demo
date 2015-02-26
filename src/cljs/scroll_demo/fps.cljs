(ns scroll-demo.fps
  (:require [reagent.core :as r]))

(def fps (r/atom 60))

(defn measure! [interval]
  (let [elapsed (atom 0)
        last-ts (atom nil)
        frames (atom 0)]
    (letfn [(cb [ts]
              (let [last (or @last-ts ts)
                    delta (- ts last)
                    cur-elapsed (swap! elapsed + delta)
                    cur-frames (swap! frames inc)]
                (reset! last-ts ts)
                (if (> cur-elapsed interval)
                  (do
                    (reset! fps (->> (/ cur-frames cur-elapsed)
                                    (* 1000)
                                    (.round js/Math))) 
                    (swap! elapsed + (- interval))
                    (reset! frames 0))))
              (.requestAnimationFrame js/window cb))]
      (.requestAnimationFrame js/window cb))))

(measure! 2000)
