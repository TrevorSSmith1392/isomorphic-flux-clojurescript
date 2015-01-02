(ns component.comp
  (:require [reagent.core :as reagent :refer [atom]]
            [store.test :as store]
            [application.context :as c]
            [application.actions :as actions]
            )
  (:use [cljs.reader :only [read-string]])) 
(enable-console-print!)

(def with-unmount (with-meta 
  (fn [] 
    [:p "hoo ah!"]) 
  {}
  #_{:component-will-mount #(println "will mount")
   :component-did-mount #(println "did mount")
   :component-will-unmount #(println "will unmount")
   :component-will-update #(println "will update")}))

(defn something [context]
  (let [internal (atom 0)]
    #_(println "created, initial internal state here")

    (fn [context] 
      (let [[state desc] (c/stores context 
                                  :store.test/test-store
                                  :store.otherStore/other-two)
            count (:count state)] 
        #_(println "render, flux state here")
        [:div
         [:p "this is from the store: " count]
         [:p desc]
         [:p "internal " @internal]
         [:div
          [:button {:on-click #(c/dispatch context "testSignal")} "button text"]
          [:button {:on-click #(swap! internal inc)} "inc component"]
          (if (even? @internal)
            [with-unmount]
            [:p "uh oh!"])]]))))    

(defn cb [err result]
  (prn result)
  )

(defn parent [context]
  [:div
   [:button {:on-click #(actions/someAction context cb)} "fetcher action"]
   [something context]
   [something context]
   ]
  )



