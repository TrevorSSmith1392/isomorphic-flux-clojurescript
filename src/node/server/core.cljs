(ns server.core
  (:require [cljs.nodejs :as node]
            [reagent.core :as r]
            [component.comp :as comp]
            [application.context :as context]
            [application.actions :as actions]
            [application.interfaces :as i]
            [application.util :as u]
            [server.Fetcher :as Fetcher]
            ))

(.install (node/require "source-map-support"))
(node/enable-util-print!)

(def express (node/require "express"))
(def app  (express))

(def textParser (-> (node/require "body-parser") (.text)))

(def react (node/require "react"))
(set! (.-React js/global) react)

(def dot (node/require "dot"))
(set! (-> dot .-templateSettings .-strip) false)
(def dots (-> dot
            (.process #js {:path "./resources/templates"})))


(Fetcher/register
  {:name "test"
   :read (fn [req resourceName params config cb]
           (cb nil {:data "data" :some :stuff :params params}))
   :create (fn [req resourceName params config cb] 
             (cb nil {:ok :dude :params params}))})

(context/initializeStores)

(defn -main []
  (doto app
    (.use (.static express "./resources/public/"))
    (.use "/api" textParser Fetcher/middleware)

    (.get "/" 
          (fn [req res]
            (let  [fetcher (Fetcher/Fetcher. req)
                   context (context/createContext fetcher)]

              (i/read fetcher "test" nil nil 
                      (fn [err res]
                        (println "server fetcher" err res)))
              (let [markup (r/render-to-string [comp/parent context])
                    state (u/serialize (context/raw context))
                    page (.shell dots #js {:content markup :state state})]
                (.send res page)))))
    (.use (fn [err req res next]
            (.log js/console err)
            (-> res (.status 500) (.send "Something went wrong")))))


  (.log js/console 
        "Express server started on port:"  
        (-> app (.listen 4000) .address .-port)))

(set! *main-cli-fn* -main)
