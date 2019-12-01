(ns io.lvh.nginx-secure-link-test
  (:require [io.lvh.nginx-secure-link :as sl]
            [clojure.test :as t]))

(t/deftest secure-link-md5-tests
  ;; samples from https://www.nginx.com/blog/securing-urls-secure-link-module-nginx-plus/
  (let [secret "enigma"
        expiry 1483228740
        uri  "/files/pricelist.html"
        remote-addr "192.168.33.14"
        tag (sl/secure-link-md5 secret expiry uri remote-addr)]
    (t/is (= "AUEnXC7T-Tfv9WLsWbf-mw" tag)))

  (let [secret "NqxvZy8Wk"
        expiry 2082693600
        uri  "/tiles_webclient/7/28/43.pbf"
        tag (sl/secure-link-md5 secret expiry uri)]
    (t/is (= "oy1kqckBdiIKdpczrDBnpg" tag))))
