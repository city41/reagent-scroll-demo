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

(defn- block [pv cv & {:keys [x y w h offset direction]}]
  (let [payload [:div {:style {:display :inline-block
                               :box-sizing :border-box
                               :width "100%"
                               :height "100%"
                               :background-color "#A78765"
                               :color :white
                               :padding 10}}]
        left-animation (if (= :left direction) [x 100] [x (+ x w 200)])]
    [animation-container
     pv
     cv
     payload
     {:style {:top y
              :left x
              :width w
              :height h}
      :animations {[offset (+ offset 4000)] {:left left-animation
                                             :width [w 0]
                                             :opacity [1 0]
                                             :height [h 0]}}}]))

(defn page []
  (listen!)
  (fn []
    (let [pv @prev-scroll-y
          cv @cur-scroll-y]
      [:div 
       {:style {:height 6000
                :font-family :sans-serif}}
       [:div {:style {:position :fixed
                      :z-index 1000
                      :background-color "#989DAF"
                      :color :white
                      :padding 10}}
        "frame rate: " @fps " y: " cv]
       [:div
        {:style {:position :fixed
                 :top 0
                 :left 0
                 :width 1000
                 :height 600
                 :background-color "#E9EBF1"}}]
       [:div {:style {:text-align :center
                      :position :fixed
                      :top 0
                      :left 300
                      :width 400
                      :height 600
                      :font-size 110
                      :background-color "#FCC6DB" 
                      :color :white}}
        "be sure to drink your ovaltine"]
       (for [i (range 3)]
         (let [h 200
               w 200
               y (* i h)
               x 300
               offset (* i 0)]
           ^{:key i}
           [:div
            [block pv cv :x x :y y :w w :h h :offset offset :direction :left]
            [block pv cv :x (+ 200 x) :y y :w w :h h :offset offset :direction :right]]))
       [:div {:style {:text-align :center
                      :position :fixed
                      :top 500
                      :left 0
                      :width 1000
                      :z-index 1000
                      :font-size 20}}
        [:p "scroll down to see the message"]]])))

(defn init! []
  (r/render-component [page] (.getElementById js/document "app")))
