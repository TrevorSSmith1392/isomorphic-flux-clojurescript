(ns store.otherStore
  (:require-macros [application.macros :as m]))
(enable-console-print!)

(defn handler [context state signal & args]
  (m/dispatch signal
    "testSignal" 
    (do
      #_(println "other one"))

    "other signal" 
    (do
      (println "some other signla" args))))

(def schema
  {:id ::other-one
   :init-state {}
   :handler handler
   })

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handlerTwo [context state signal & args]
  (let [parent (deref (:store.test/test-store 
                       (:by-id context)))
        count (:count parent)
        ]
    (m/dispatch signal
     "testSignal" 
     (do
       (let [words (repeat count "words")]
         (reset! state words)))

     "fuck" 
     (do
       (println "some other signla" args)))))

(def schemaTwo
  {:id ::other-two
   :init-state '()
   :handler handlerTwo
   :wait-for #{:store.test/test-store}
   })
