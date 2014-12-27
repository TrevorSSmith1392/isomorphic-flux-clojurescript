(ns application.macros)

(defmacro dispatch [& forms]
  (let [odd (-> forms (count) (mod 2) (= 1))
        forms' (if odd 
                 (concat forms [nil])
                 forms)]
    (cons 'case forms')))
