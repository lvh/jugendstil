(ns io.lvh.jugendstil.client
  (:require
   [clj-http.lite.client :as client]
   [taoensso.timbre :refer [spy]]
   [clojure.string :as str]
   [cheshire.core :as json]
   [lambdaisland.uri :as uri]
   [byte-streams :as bs]
   [com.rpl.specter :as sr]
   [eidolon.core :as ei]))

(def chrome-ua "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")

(defn wrap-ua
  [client]
  (fn ua-wrapping-client
    [req]
    (-> req (assoc-in [:headers "user-agent"] chrome-ua) client)))

(defn wrap-clean-response
  "A client wrapper to clean up the client response so it can be returned as a
  proxy response."
  [client]
  (fn [request]
    (-> request
        client
        (select-keys [:status :headers :body])
        (update :headers dissoc "transfer-encoding" "content-encoding" "content-length"))))

(defn wrap-log-request
  "A client wrapper to log requests right before they're sent out, coupled with their response.

  Add this as the first wrapper so that you log the combined effect of all other
  wrappers."
  [client]
  (fn request-logging-client
    [ext-request]
    (let [ext-response (client (spy ext-request))]
      (spy ext-response))))

(def default-client
  (-> client/request
      wrap-log-request
      wrap-clean-response
      wrap-ua))

(defn parts-match-url?
  "Given URL parts (map with :scheme, :host etc), does given URL match?"
  [parts url]
  (-> url uri/parse (select-keys (keys parts)) (= parts)))

(defn tweak-url
  "Given a URL, update just the given parts (:host, :scheme, etc)."
  [url parts]
  (-> url uri/parse (merge parts) str))

(defn rewrite-urls
  "Given a nested data structure (haystack) and a bunch of parsed url parts (a map
  with :scheme, :host, etc) find occurrences of URLs matching the first parts
  and replace new-parts into the url as per [[tweak-url]]."
  [haystack parts new-parts]
  (let [match? (fn [url] (parts-match-url? parts url))
        tweak (fn [url] (tweak-url url new-parts))]
    (sr/transform [ei/TREE-LEAVES string? match?] tweak haystack)))

(defn wrap-mirror
  "Point incoming requests elsewhere.

  Basically lets you be a transparent proxy for all requests matching
  matching-url-parts. Pass `{}` if you just want to redirect all requests."
  [client matching-url-parts new-url-parts]
  (fn mirror-client [{:keys [url] :as req}]
    (if (parts-match-url? matching-url-parts url)
      (let [new-req (-> req
                        (update :headers dissoc "host")
                        (update :url tweak-url new-url-parts))]
        (client new-req))
      (client req))))

(def starts-with?* (fnil str/starts-with? ""))

(defn wrap-modify-json-response
  [client modify-body]
  (let [modify-body* (comp
                      bs/to-byte-array
                      json/generate-string
                      modify-body
                      json/parse-stream bs/to-reader)]
    (fn [req]
      (let [resp (client req)]
        ;; we have to check there's a body first: HEAD requests will show as
        ;; application/json but have nothing to change
        (if (and (some? (resp :body))
                 (-> resp
                     (get-in [:headers "content-type"])
                     (starts-with?* "application/json")))
          (update resp :body modify-body*)
          resp)))))
