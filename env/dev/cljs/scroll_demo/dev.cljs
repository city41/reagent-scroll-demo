(ns ^:figwheel-no-load scroll-demo.dev
  (:require [scroll-demo.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [weasel.repl :as weasel]
            [reagent.core :as r]))

(enable-console-print!)

#_(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn [] (r/force-update-all)))

#_(weasel/connect "ws://localhost:9001" :verbose true)

(core/init!)
