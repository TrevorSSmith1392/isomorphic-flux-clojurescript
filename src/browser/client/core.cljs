(ns client.core
  (:require [reagent.core :as r :refer [atom]]
            [component.comp :as comp]
            [application.context :as context]
            [application.actions :as actions] 
            [application.interfaces :as i]  
            [application.util :as u]
            [client.Fetcher :as Fetcher]
            ))

(enable-console-print!)

(def fetcher (Fetcher/Fetcher. "/api"))

(i/read fetcher "test" {:from :read-call} nil (fn [err res] 
                              (println "client fetcher" err res)))
#_(i/create fetch "test" {:from :create-call} nil nil nil (fn [err res] 
                              (println "client fetcher" err res)))

(def givenState (-> (.getElementById js/document "state")
                  .-innerText u/deserialize))

(context/initializeStores)
(def context 
  (context/createContext fetcher givenState))

(r/render
  [comp/parent context]
  (.getElementById js/document "container"))
