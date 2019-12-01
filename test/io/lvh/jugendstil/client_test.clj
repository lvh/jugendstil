(ns io.lvh.jugendstil.client-test
  (:require [io.lvh.jugendstil.client :as c]
            [clojure.test :as t]))

(def haystack
  (-> {}
      (assoc-in [:a :b :c] [0 0 "https://needle/xyzzy"])
      (assoc-in [:x :y :z] "https://needle/iddqd")
      (assoc-in [:p :q :r] "https://red.herring")))

(t/deftest rewrite-urls-test
  (t/testing "remove https from links to host needle"
    (let [match {:scheme "https" :host "needle"}
          new-parts {:scheme "http"}]
      (t/is (= (-> {}
                   (assoc-in [:a :b :c] [0 0 "http://needle/xyzzy"])
                   (assoc-in [:x :y :z] "http://needle/iddqd")
                   (assoc-in [:p :q :r] "https://red.herring"))
               (c/rewrite-urls haystack match new-parts))))))

