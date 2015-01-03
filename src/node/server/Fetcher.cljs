(ns server.Fetcher
  (:require [cljs.nodejs :as node]
            [application.interfaces :as i]
            [application.util :as u]) 
  )
(def qs (node/require "qs"))
(declare fetcherRequest)

(def resources (atom {}))
(defn register [{:keys [name read create update delete]}]
  (swap! resources assoc name 
         {:read read :create create
          :update update :delete delete}))

(defn normalizeErrors [cb]
  (fn [err res]
    (if err
      (cb {:type :application :err err} nil)
      (cb nil res))))


(deftype Fetcher [req]
  i/IFetcher
  (read [this resourceName params config cb]
    (fetcherRequest resourceName :read req
                    params config (normalizeErrors cb)))
  (create [this resourceName params body config cb]
    (fetcherRequest resourceName :create req
                    params config (normalizeErrors cb))))

(defn middleware [req res next]
  (letfn [(middlewareResponse [err data]
            (let [[s d] (if err [400 err] [200 data])]
              (-> res (.status s) (.send (u/serialize d)))))]
    (if (-> req .-method (= "GET"))
      (let [qsi (-> req .-path 
                  (.lastIndexOf "resource")
                  (+ (count "/resource")))
            path (-> req .-path (.substr qsi) (.split ";"))
            resourceName (.shift path)
            params (->> path 
                     first 
                     (.parse qs))
            cljParams (-> params .-parm u/deserialize)]
        (fetcherRequest resourceName :read req
                        cljParams nil middlewareResponse))    
      ;all others
      (let [body (-> req .-body u/deserialize)
            {:keys [resourceName operation params config]} body]
        (fetcherRequest resourceName operation req
                        params config middlewareResponse)))))

;this may need to handle errors better
(defn fetcherRequest [resourceName operation
                      req params config responseCb]
  (if-let [resource (@resources resourceName)]
    (if-let [method (resource operation)]
      (method req resourceName params config responseCb) 
      (responseCb 
        (str "Method [" operation 
             "] not defined for [" resourceName "]") nil))
    (responseCb 
      (str "Fetcher resource [" resourceName "] not found") nil)))
