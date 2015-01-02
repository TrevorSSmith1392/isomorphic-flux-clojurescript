(ns server.Fetcher
  (:require [cljs.nodejs :as node]
            [application.interfaces :as i]
            [application.util :as u]) 
  )
(def qs (node/require "qs"))

(def resources (atom {}))
(defn register [{:keys [name read create]}]
  (swap! resources assoc name {:read read :update update}))

(defn normalizeErrors [cb]
  (fn [err res]
    (if err
      (cb {:type :application :err err} nil)
      (cb nil res))))

(deftype Fetcher [req]
  i/IFetcher
  (read [this resourceName params config cb]
    (let [resource (get-in @resources [resourceName :read])]
      (resource req resourceName params config (normalizeErrors cb))))
  (create [this resourceName params body config cb]
    (let [resource (get-in @resources [resourceName :create])]
      (resource req resourceName params config (normalizeErrors cb)))))

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
            cljParams (-> params .-parm u/deserialize)
            resource (get-in @resources [resourceName :read])]
        (resource req resourceName cljParams nil middlewareResponse))
      ;all others
      (let [body (-> req .-body u/deserialize)
            {:keys [resourceName operation params config]} body
            resource (get-in @resources [resourceName operation])]
        (resource req resourceName params config middlewareResponse)))))
