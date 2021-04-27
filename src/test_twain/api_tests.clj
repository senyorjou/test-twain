(ns test-twain.api-tests
  (:require  [clj-http.client :as client]))


(defn print-title [& msgs]
  (let [message (clojure.string/join " " msgs)]
    (println "")
    (println message)
    (println (apply str (repeat (count message) "=")))))


(defn print-assertion [msg cond]
  (println (str " " (if cond "✓" "x") " " msg)))


(defn get-token [url]
  (let [endpoint (str url "/auth")]
    (client/post endpoint {:form-params {:user_id "username" :password "password"}
                           :content-type :json
                           :as :json})))

(defn header-token [token]
  {:headers {:Authorization (str "Bearer " token)}})


(defn call-to-auth [url]
  (let [endpoint (str url "/auth")]
    (print-title "Calling endpoint" endpoint)

    (let [response (client/get endpoint {:throw-exceptions false})]
      (print-assertion "GET call to /auth returns 405" (= (:status response) 405)))

    (let [response (get-token url)
          token (:token (:body response))]
      (print-assertion "POST call to /auth with username/password returns 200" (= (:status response) 200))
      (let [condition (and (string? token) (not (empty? token)))]
        (print-assertion "POST call to /auth with username/password returns a valid Token" condition)))))


(defn call-to-root [url]
  (let [endpoint (str url "/me")]
    (print-title "Calling endpoint" endpoint)

    (let [response (client/get endpoint {:throw-exceptions false})]
      (print-assertion "GET call to /me without token returns 401" (= (:status response) 401)))

    (let [token (:token (:body (get-token url)))
          response (client/get endpoint (into {:as :json} (header-token token)))
          condition (= (set (keys (:body response))) #{:user :files})]
      (print-assertion "GET call to /me with token returns valid payload" condition))))



(comment
  (let [token (get-token "http://localhost:5000")
        endpoint "http://localhost:5000/me"
        response (client/get endpoint (into {:as :json} (header-token token)))]
      (:body response))
  (print-title "abc" "def")
  )
