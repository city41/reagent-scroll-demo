(ns scroll-demo.scroll-chan
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! put! chan timeout]]
            [goog.events :as events]
            [goog.events.EventType :as EventType]
            [goog.dom :as dom]))

(defn- get-scroll []
  (-> (dom/getDocumentScroll) (.-y)))

(defn- events->chan [el event-type c]
  (events/listen el event-type #(put! c %))
  c)

(defn scroll-chan-events [] 
  (events->chan js/window EventType/SCROLL (chan 1 (map get-scroll))))

(defn scroll-chan-test
 "A canned channel that sends out well known fake scroll events. Useful to test things while developing."
  []
  (let [c (chan 1)]
    (go
      (put! c 0)
      (<! (timeout 2000))
      (put! c 50)
      (<! (timeout 20))
      (put! c 100))
    c))

