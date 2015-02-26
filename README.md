# Reagent Scroll Demo

A live version is [here](http://www.mattgreer.org/reagent-scroll-demo).

This is a simple demo of doing scroll based animations in Reagent.

Inside `src/cljs/scroll_demo`:

* `core.cljs` -- a simple Reagent app using `scroll-engine` to do the scrolling animations
* `scroll_chan.cljs` -- core.async hooks into the window scroll event
* `scroll_engine.cljs` -- the scrolling animation engine itself
* `fps.cljs` -- a tiny file to measure the browser's framerate. The result is displayed in `core.cljs`

## To get it running

`lein figwheel` will pull everything together and make the demo available at localhost:3449
