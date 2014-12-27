(ns server.core
  (:require [cljs.nodejs :as node]
            [reagent.core :as reagent :refer [atom]]
            [component.comp :as comp]
            [application.context :as context]
            ) 
  (:require-macros [server.macros :as m]))

(node/enable-util-print!)

(def express (node/require "express"))
(def app  (express))
(def react (node/require "react"))
(set! (.-React js/global) react)

(def dots (-> (node/require "dot")
            (.process #js {:path "./resources/templates"})))

(context/initializeStores)

(defn -main []

  (doto app
    (.get "/" (fn [req res]
                (let  [appState (context/createContext)]
                  (context/dispatch appState "testSignal") 
                  (context/dispatch appState "testSignal") 

                  (let [markup (reagent/render-to-string [comp/parent appState])
                        edn (context/serialize appState)
                        page (.shell dots #js {:content markup :state edn})]
                    (.send res page)))))

    )

  (let [root (str "./") public (str root "resources/public/" )]
    (.use app (.static express public)))

  (.log js/console (str "Express server started on port: "  
                        (.-port (.address (.listen app 4000))))))

(set! *main-cli-fn* -main)

    ;#_(.get app "/" (fn [req res]
                    ;(.sendFile res "index.html" #js {:root public})))
