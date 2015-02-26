(ns scroll-demo.scroll-engine
  (:require [reagent.core :as r]))

(defn- length [[from to]]
  (- to from))

(defn- between [[from to] v]
  (and (>= v from)
       (< v to)))

(defn- get-animation [animations cv]
  (first (filter #(between (first %) cv) animations)))

(defn- domain-to-range [[domain-from domain-to :as domain] [range-from range-to :as range] domain-value]
  (let [domain-len (length domain)
        range-len (length range)
        domain-offset (- domain-value domain-from)
        ratio (/ domain-offset domain-len)
        range-offset (* ratio range-len)]
    (+ range-offset range-from)))

(def ^:private max-duration 500)

(defn- abs [v]
  (if (< v 0) (- v) v))

(defn- calc-style [domain domain-val [style-name range]]
  (let [range-val (domain-to-range domain range domain-val)]
    [style-name range-val]))

(defn update-tween [tween pv cv duration ani]
  (let [current (:current tween pv)
        remaining (:remaining tween 0)
        new-duration (+ duration remaining)]
    (assoc tween
           :ani ani
           :slice [current cv]
           :duration new-duration
           :remaining new-duration
           :current current)))

(defn progress-tween [tween delta]
  (let [{:keys [duration remaining slice ani]} tween
        new-remaining (max 0 (- remaining delta))
        elapsed (- duration new-remaining)
        percent (if (zero? duration) 100 (/ elapsed duration))
        length (length slice)
        domain-step (* percent length)
        domain-value (+ domain-step (first slice))
        [domain ranges] ani
        styles (into {} (map #(calc-style domain domain-value %) ranges))]
    (assoc tween 
           :remaining new-remaining
           :current domain-value
           :styles styles)))

(defn animate! [pv cv [domain anis :as ani] a-tween]
  (let [domain-length (length domain)
        delta (abs (- cv pv))
        percent (/ delta domain-length)
        duration (* percent max-duration)]
    (swap! a-tween update-tween pv cv duration ani)))

(defn start-loop! [a-tween]
  (let [last-ts (atom nil)]
    (letfn [(raf [ts]
              (let [last (or @last-ts ts)
                    delta (- ts last)]
                (reset! last-ts ts)
                (swap! a-tween progress-tween delta)
                (.requestAnimationFrame js/window raf)))]
      (.requestAnimationFrame js/window raf))))

(defn animation-container [pv cv payload {:keys [animations style] :as ani-config}]
  (let [tween (r/atom {})
        prev-cv (atom nil)]
    (start-loop! tween)
    (fn [pv cv] 
      (when (not= cv @prev-cv)
        (reset! prev-cv cv)
        (when-let [animation (get-animation animations cv)]
          (animate! pv cv animation tween)))
      [:div.animation
       {:style (merge style
                      (:styles @tween)
                      {:position :fixed})}
       payload])))


