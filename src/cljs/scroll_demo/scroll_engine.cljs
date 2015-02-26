(ns scroll-demo.scroll-engine
  (:require [reagent.core :as r]))

(defn- length [[from to]]
  (- to from))

(defn- between [[from to] v]
  (and (>= v from)
       (< v to)))

(defn- get-animation
  "given a set of animations and the current state of the world,
  finds the one (if any) that should currently be animating
  
  TODO: allow more than one simultaneous animation"
  [animations cv]
  (first (filter #(between (first %) cv) animations)))

(defn- domain-to-range
  "Converts a value from domain to range. In other words, maps the current state of the world
  to what the animation's value needs to be."
  [[domain-from domain-to :as domain] [range-from range-to :as range] domain-value]
  (let [domain-len (length domain)
        range-len (length range)
        domain-offset (- domain-value domain-from)
        ratio (/ domain-offset domain-len)
        range-offset (* ratio range-len)]
    (+ range-offset range-from)))

(def ^:private max-duration 500)

(defn- abs [v]
  (if (< v 0) (- v) v))

(defn- calc-style
  "given the domain and an animation definition, determines what 
  the CSS value for its property (ie opacity, left, etc) should be."
  [domain domain-val [style-name range]]
  (let [range-val (domain-to-range domain range domain-val)]
    [style-name range-val]))

(defn update-tween-props
  "Given a tween object and the new state of the world, updates
  the tween so it can now progress towards the new goal. Takes into 
  account how far the tween already progressed when deciding the tweens new goals."
  [tween pv cv duration ani]
  (let [current (:current tween pv)
        remaining (:remaining tween 0)
        new-duration (+ duration remaining)]
    (assoc tween
           :ani ani
           :slice [current cv]
           :duration new-duration
           :remaining new-duration
           :current current)))

(defn progress-tween 
  "Given a tween object and how much time has passed, causes
  the tween to update its CSS properties. This is ultimate what causes
  things in the browser to smoothly animate."
  [tween delta]
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

(defn animate! 
  "Called whenever the state of the outside world changes (ie, cv has a new value).
  Responds to this change by setting up the tween object to move towards a new goal."
  [pv cv [domain anis :as ani] a-tween]
  (let [domain-length (length domain)
        delta (abs (- cv pv))
        percent (/ delta domain-length)
        duration (* percent max-duration)]
    (swap! a-tween update-tween-props pv cv duration ani)))

(defn start-loop! 
  "Called once when an animation-container is created, uses requestAnimationFrame
  to continually move the object towards its current goal using the tween object.
  
  TODO: only call requestAnimationFrame when a tween object has work to do.
  
  TODO: only setup one RAF callback and instead pass each animation-container's tween object
  into a collection. Currently each animation-container sets up its own RAF callback."
  [a-tween]
  (let [last-ts (atom nil)]
    (letfn [(raf [ts]
              (let [last (or @last-ts ts)
                    delta (- ts last)]
                (reset! last-ts ts)
                (swap! a-tween progress-tween delta)
                (.requestAnimationFrame js/window raf)))]
      (.requestAnimationFrame js/window raf))))

(defn animation-container
  "A Reagent component that wraps another component and grants it animation abilities.
  
  TODO: not happy with prev-cv, but since both pv/cv and the tween Reagent atom can cause
  a rerender of this component, need to only call animate! when cv has changed."
  [pv cv payload {:keys [animations style] :as ani-config}]
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


