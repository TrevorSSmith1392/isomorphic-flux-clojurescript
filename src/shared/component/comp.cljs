(ns component.comp
  (:require [reagent.core :as reagent :refer [atom]]
            [store.test :as store]
            [application.context :as c]
            ))
(enable-console-print!)

(defn something [context]
  (let [[state desc] (c/stores context 
                               :store.test/test-store
                               :store.otherStore/other-two)
        count (:count state)]
    [:div
     [:p "this is from the store: " count]
     [:p desc]
     [:div
      [:button {:on-click #(c/dispatch context "testSignal")} "button text"]]]))

(defn parent [context]
  [:div
   [something context]
   [something context]
   ]
  )
