(ns store.test
  (:require-macros [application.macros :as m])) 
(enable-console-print!)

(defn handler [context state signal & args]
  (m/dispatch signal
    "testSignal" 
    (do
      (swap! state update-in [:count] inc))

    "other signal" 
    (do
      (println "some other signal" args))))

(def schema
  {:id ::test-store
   :init-state {:count 0}
   :handler handler})
