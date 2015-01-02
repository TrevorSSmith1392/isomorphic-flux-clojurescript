(ns server.resources)

(def aFetcher 
  #js {
       :name "resourceName"
       :read (fn [req resource params config callback]
               (callback nil #js {:data "something"}))
       })

(def db (atom 5))

(def wordCount
  #js {
       :name "count"
       :read (fn [req resource params config callback]
               (js/setTimeout 
                 #(callback nil @db) 
                 500))
       :update (fn [req resource params config body callback]
         (let [{:keys [op val] 
                :or  {op :set}} (js->clj params)
               [op mod] (if (= :set op)
                    [reset! val]
                    [swap! #(+ % val)])]
            (callback nil (op db mod))))
       }
  )

(def resources [aFetcher wordCount])
