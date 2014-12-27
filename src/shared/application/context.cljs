(ns application.context
  (:require [reagent.core :as reagent :refer [atom]]
            [application.topoSort :as topo]
            [store.test :as store]
            [store.otherStore :as other]))

(def storesGraph);{}
(def storesTopo);[]

(defn invert  [in]
  (reduce (fn [acc [key vals]]
            (reduce (fn [a v] (assoc a v (conj (get a v #{}) key)))
                    acc vals))
          {} in))

(defn registerStores [stores]
  (let [inverted (->> stores
                   (reduce (fn [inv {:keys [id wait-for]}]
                             (assoc inv id (or wait-for #{}))
                             ) {}))
        graph (merge 
                (zipmap (keys inverted) (repeat #{}))
                (invert inverted))
        sorted (topo/kahn-sort graph)

        resolver (->> stores 
                   (reduce #(assoc % (:id %2) %2) {}))]
    (def storesGraph resolver)
    (def storesTopo sorted)))

;config, doesn't really belong here
(defn initializeStores [] 
  (registerStores [other/schemaTwo store/schema other/schema]) )

(defn createContext 
  "Create an instance of the stores for the application, 
  reinitializing from the given context if available"
  [& [serialized]]
  (let [refs (transient {}) 
        ordered (->> storesTopo
                  (mapv (fn [storeId]
                          (let [store (storesGraph storeId)
                                state (atom 
                                        (if serialized
                                          (storeId serialized)
                                          (:init-state store)))]
                            (assoc! refs storeId state)
                            {:state state
                             :handler (:handler store)}))))]
    {:by-id (persistent! refs)
     :ordered ordered}))

(defn dispatch [context signal & args]
  (doseq [store (:ordered context)]
    (let [handler (:handler store)
          state (:state store)]
      (apply handler context state signal args))))

(defn store [context keyword]
  ;this deref might be a bad idea, but would nicely force the architecture
  (deref (keyword (:by-id context))))

(defn stores [context & stores]
  (mapv #(store context %) stores))

(defn serialize [context]
  (let [by-id (:by-id context)
        res (transient {})]
    (doseq [[id atom] by-id]
      (assoc! res id @atom))
    (pr-str (persistent! res))))
