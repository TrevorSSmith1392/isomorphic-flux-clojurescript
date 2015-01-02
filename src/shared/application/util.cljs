(ns application.util
  (:require [cognitect.transit :as t]))

(def w (t/writer :json))
(def r (t/reader :json))

(defn serialize [o]
  (t/write w o))
(defn deserialize [o]
  (t/read r o))
