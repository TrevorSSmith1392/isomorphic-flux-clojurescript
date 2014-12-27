(ns client.core
  (:require [reagent.core :as r :refer [atom]]
            [store.test :as store]
            [store.otherStore :as other]
            [component.comp :as comp]
            [application.context :as context])
  (:use [cljs.reader :only [read-string]]))

(enable-console-print!)

(context/initializeStores)

(def givenState (-> (.getElementById js/document "state")
                  (.-innerText)
                  (read-string)))

(def appState 
  (context/createContext givenState))

(r/render
  [comp/parent appState]
  (.getElementById js/document "container"))
