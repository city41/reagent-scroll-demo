(ns scroll-demo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<!]]
            [goog.events :as events]
            [cljsjs.react :as react]
            [scroll-demo.fps :refer [fps]]
            [scroll-demo.scroll-chan :refer [scroll-chan-events scroll-chan-test]]
            [scroll-demo.scroll-engine :refer [animation-container]]))

(def cur-scroll-y (r/atom 0))
(def prev-scroll-y (r/atom 0))

(defn listen! []
  (let [chan (scroll-chan-events)]
    (go-loop []
             (let [new-y (<! chan)]
               (reset! prev-scroll-y @cur-scroll-y)
               (reset! cur-scroll-y (max 0 new-y)))
               (recur))))

(defn- block [pv cv & {:keys [x y w h offset prop]}]
  (let [payload [:div {:style {:display :inline-block
                               :box-sizing :border-box
                               :width "100%"
                               :height "100%"
                               :background-color "rgb(100, 250, 80)"
                               :color :white
                               :padding 10}}]]
    [animation-container
     pv
     cv
     payload
     {:style {:top y
              prop x
              :width w
              :height h}
      :animations {[offset (+ offset 4000)] {prop [x 100]
                                             :width [w 0]
                                             :opacity [1 0]
                                             :height [h 0]}}}]))

(defn page []
  (listen!)
  (fn []
    (let [pv @prev-scroll-y
          cv @cur-scroll-y]
      [:div 
       {:style {:height 6000}}
       [:div {:style {:position :fixed}} "frame rate: " @fps " y: " cv]
       [:div {:style {:text-align :center
                      :position :fixed
                      :top 10
                      :font-size 20
                      :width "100%"}}
        [:p "scroll down to see the message"]]
       [:div {:style {:text-align :center
                      :position :fixed
                      :top 100
                      :left "30%"
                      :width "40%"
                      :height 600
                      :font-size 120
                      :background-color :orange
                      :color :white}}
        "be sure to drink your ovaltine"]
       (for [i (range 3)]
         (let [h 200
               w 200
               y (+ 100 (* i h))
               x 400
               offset (* i 0)]
           ^{:key i}
           [:div
            [block pv cv :x x :y y :w w :h h :offset offset :prop :left]
            [block pv cv :x x :y y :w w :h h :offset offset :prop :right]]))])))

(defn init! []
  (r/render-component [page] (.getElementById js/document "app")))
