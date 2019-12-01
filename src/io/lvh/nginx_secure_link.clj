(ns io.lvh.nginx-secure-link
  (:import (org.apache.commons.codec.binary Base64)
           (java.security MessageDigest))
  (:require [clojure.string :as str]))

(defn ^:private md5
  [^bytes in]
  (-> (MessageDigest/getInstance "md5") (.digest in)))

(defn secure-link-md5
  [secret & parts]
  (-> parts (str/join) (str " " secret) (.getBytes) (md5) (Base64/encodeBase64URLSafe) (String.)))
