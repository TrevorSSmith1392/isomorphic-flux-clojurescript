(ns application.interfaces)

(defprotocol IFetcher
  (read [this resourceName params config cb])
  (create [this resourceName params body config cb])
  ;update
  ;delete
  
  )

