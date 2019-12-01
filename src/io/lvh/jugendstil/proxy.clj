(ns io.lvh.jugendstil.proxy
  (:require
   [ring.util.request :as req]
   [taoensso.timbre :refer [spy info]]
   [clojure.java.io :as io]
   [byte-streams :as bs]))

(defn wrap-client
  [handler client]
  (fn [r] (-> r (assoc ::client client) handler)))

(defn proxy-handler
  [{:keys [request-method uri headers body] ::keys [client] :as request}]
  (client
   {:method request-method
    :url (req/request-url request)
    :headers (dissoc headers "content-length")
    :body (if (-> request-method #{:head :get :options})
            ;; jetty gives you an (empty) streaming body on GET
            ;; requests, which confuses the http client because
            ;; it doesn't know the body is empty.
            nil
            (bs/to-byte-array body))
    :throw-exceptions false
    :as :byte-array
    ::orig-request request}))

(defn wrap-request-response-log
  [handler log]
  (fn [request]
    (let [response (handler request)]
      (swap! log conj {::request request ::response response})
      response)))
