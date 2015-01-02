(ns client.core
  (:require [reagent.core :as r :refer [atom]]
            [component.comp :as comp]
            [application.context :as context]
            [application.actions :as actions] 
            [application.interfaces :as i]  
            [cognitect.transit :as t]))

(enable-console-print!)

(defn serialize [o]
  (t/write (t/writer :json) o))
(defn deserialize [o]
  (t/read (t/reader :json) o))

;underlying clientside javascript dependencies need to be cleaned up a lot
(def fetcher (js/__Fetcher. #js {:xhrPath "/api"}))

(deftype SFetcher [xhrPath]
  i/IFetcher
  (read [this resourceName params config cb]
    (let [qs (serialize params)
          eqs (str ";parm=" 
               (.encodeURIComponent js/window qs))
          url (str xhrPath "/resource/" resourceName eqs)] 
      (-> js/superagent 
        (.get url)
        (.end (fn [res]
                (let [clj (deserialize (.-body res))]
                  (println clj)))))))

  (update [this resourceName params body config cb] ;difference between params and body?
    (let [payload (serialize
                   {:resourceName resourceName
                    :operation :update
                    :params params
                    :config config })]
      (-> js/superagent
        (.post xhrPath)
        (.set "Content-Type" "application/json")
        (.send payload)
        (.end (fn [res]
                (let [clj (deserialize (.-body res))]
                  (println clj))))))))

(def fetch (SFetcher. "/api"))

(i/read fetch "test" nil nil (fn [err res] 
                              (println "client fetcher" res)))
(i/update fetch "test" nil nil nil (fn [err res] 
                              (println "client fetcher" res)))

(def givenState (-> (.getElementById js/document "state")
                  .-innerText deserialize))

(context/initializeStores)
(def appState 
  (context/createContext fetcher givenState))

(r/render
  [comp/parent appState]
  (.getElementById js/document "container"))
