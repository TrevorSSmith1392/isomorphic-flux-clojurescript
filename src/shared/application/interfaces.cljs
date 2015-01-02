(ns application.interfaces)

(defprotocol IFetcher
  (read [this resourceName params config cb])
  (update [this resourceName params body config cb]))
