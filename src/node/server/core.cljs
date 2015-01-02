(ns server.core
  (:require [cljs.nodejs :as node]
            [reagent.core :as reagent :refer [atom]]
            [component.comp :as comp]
            [application.context :as context]
            [application.actions :as actions]
            [server.resources :as resources]
            [application.interfaces :as i]
            [cognitect.transit :as t]
            ) 
  (:require-macros [server.macros :as m]))

;this doesn't work
(.install (node/require "source-map-support"))


(node/enable-util-print!)
(defn printlnn [& args]
  (apply println args "\n"))

(defn roundtrip  [x]
  (let  [w  (t/writer :json)
         r  (t/reader :json)]
    (t/read r  (t/write w x))))

(defn serialize [o]
  (t/write (t/writer :json) o))
(defn deserialize [o]
  (t/read (t/reader :json) o))

(printlnn (roundtrip {:x :a}))

(def express (node/require "express"))
(def app  (express))
(def Fetcher (node/require "fetchr"))
(set! (.-Fetcher js/global) Fetcher)

;On the server only, register the fetchers before instantiating them
(doseq [resource resources/resources]
  (.registerFetcher Fetcher resource))

(def bodyParser (node/require "body-parser"))
(def textParser (-> bodyParser (.text #js {:type "application/json"})))
(def qs (node/require "qs"))
(def react (node/require "react"))
(set! (.-React js/global) react)

(def dot (node/require "dot"))
(set! (-> dot .-templateSettings .-strip) false)
(def dots (-> dot
            (.process #js {:path "./resources/templates"})))


(def resources (atom {}))
(defn registerFetcher [{:keys [name read update]}]
  (swap! resources assoc name {:read read :update update}))

(registerFetcher
  {:name "test"
   :read (fn [req resourceName params config cb]
           (cb nil {:data "data" :some :stuff}))
   :update (fn [req resourceName params config cb] (cb nil {:ok :dude}))})

(deftype SFetcher [req]
  i/IFetcher
  (read [this resourceName params config cb]
    (let [resource (get-in @resources [resourceName :read])]
      (resource req resourceName params config cb)))
  (update [this resourceName params body config cb]
    (let [resource (get-in @resources [resourceName :update])]
      (resource req resourceName params config cb))))

(defn FetcherMiddleware [req res next]
  (if (-> req .-method (= "GET"))
    (let [qsi (-> req
                .-path (.lastIndexOf "resource")
                (+ (count "/resource")))
          path (-> req .-path (.substr qsi) (.split ";"))
          resourceName (.shift path)
          params (-> path 
                   first
                   (as-> ps (.parse qs ps)))
          cljParams (->> params
              .-parm
              (deserialize))
          resource (get-in @resources [resourceName :read])]
      (resource req resourceName cljParams nil 
                (fn [err data]
                  (-> res
                    (.status 200)
                    (.json (serialize data))))))
    ;all others
    (let [body (-> req .-body deserialize)
          {:keys [resourceName operation params config]} body
          resource (get-in @resources [resourceName operation])
          cb (fn [err data]
               (-> res
                 (.status 200)
                 (.json (serialize data))))]
      (resource req resourceName params config cb))
    ))

(def fetcher (SFetcher. nil))

(i/read fetcher "test" nil nil (fn [err res]
                               (println "server fetcher" res)
                               ))




(context/initializeStores)

(defn -main []
  (doto app

    (.use "/api" textParser FetcherMiddleware)

    (.get "/" (fn [req res]
                (let  [fetcher (Fetcher. #js {:req req})
                       context (context/createContext fetcher)]

                  (let [markup (reagent/render-to-string [comp/parent context])
                        state (serialize (context/raw context))
                        page (.shell dots #js {:content markup :state state})]
                    (.send res page)))))

    )

  (let [root (str "./") public (str root "resources/public/" )]
    (.use app (.static express public)))

  (.log js/console (str "Express server started on port: "  
                        (-> app (.listen 4000)
                          (.address) (.-port)))))

(set! *main-cli-fn* -main)

    ;#_(.get app "/" (fn [req res]
                    ;(.sendFile res "index.html" #js {:root public})))
