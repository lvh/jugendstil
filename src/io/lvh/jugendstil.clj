(ns io.lvh.jugendstil
  (:require
   [io.lvh.jugendstil.proxy :as proxy]
   [org.httpkit.server :as server])
  (:import (sun.util.logging PlatformLogger PlatformLogger$Level))
  (:gen-class))

(def ^:private logger
  "This throwaway static/compile-time invocation is a workaround for runtime
   reflection of sun.util.logging.{LoggingSupport|PlatformLogger}."
  (-> "dummy" (PlatformLogger/getLogger) (.isLoggable PlatformLogger$Level/ALL)))

(defn -main
  "Runs a jugendstil proxy server."
  [& args]
  ;; TODO
  )
