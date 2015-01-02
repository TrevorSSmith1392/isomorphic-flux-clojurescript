(ns application.actions
  (:use [cljs.reader :only [read-string]]) 
  )


(defn wrapEdn [cb]
  (fn [err result]
    (cb err (js->clj result))))

#_(defn someAction [context cb & [args]]
  (let [fetcher (:fetcher (meta context))]
    (.read fetcher "resourceName" #js {:parm :eter} nil 
           (wrapEdn cb))))

(defn someAction [context cb & [args]]
  (let [fetcher (:fetcher (meta context))]
    (.update fetcher "resourceName" #js {:parm :eter} nil nil cb)))

(defn getCount [context cb]
  (let [fetcher (:fetcher (meta context))]
    (println "called")
    (.read fetcher "count" #js {:parm :eter} nil
           (wrapEdn cb))))

(defn setCount [context value cb]
  (let [fetcher (:fetcher (meta context))]
    (.update fetcher "count" #js {:val value} nil nil
           (wrapEdn cb)))) 

