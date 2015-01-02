(ns client.Fetcher
  (:require [application.interfaces :as i]
            [application.util :as u]) 
  )

(defn handleResponse [cb]
  (fn [err res]
    (if (or err (.-error res))
      (let [appErr (if err 
                     {:type :external :err err}
                     (try 
                       {:type :application 
                        :err (-> res .-text u/deserialize)}
                       (catch js/Object e 
                         {:type :server :err (.-error res)})))]
        (cb appErr nil))
      (let [clj (-> res .-text u/deserialize)]
        (cb nil clj)))))

(deftype Fetcher [xhrPath]
  i/IFetcher
  (read [this resourceName params config cb]
    (let [qs (u/serialize params)
          eqs (str ";parm=" 
               (.encodeURIComponent js/window qs))
          url (str xhrPath "/resource/" resourceName eqs)] 
      (-> js/superagent 
        (.get url)
        (.end (handleResponse cb)))))

  (create [this resourceName params body config cb] ;difference between params and body?
    (let [payload (u/serialize
                   {:resourceName resourceName
                    :operation :create
                    :params params
                    :config config })]
      (-> js/superagent
        (.post xhrPath)
        (.set "Content-Type" "text/plain")
        (.send payload)
        (.end (handleResponse cb))))))
