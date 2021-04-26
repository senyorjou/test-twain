(ns test-twain.core
  (:require [clojure.string :as string]
            [clj-http.client :as client]
            [clojure.tools.cli :refer [parse-opts]]
            [test-twain.api-tests :as tests])
  (:gen-class))


(defn usage [options]
  (->> ["Test 31Twains tests"
        ""
        "Usage: test-twain [options]"
        ""
        "Options:"
        options]
      (string/join \newline)))


(def cli-options
  ;; An option with a required argument
  [["-u" "--url URL" "URL (and port) to connect"]
   ["-t" "--tests TEST" "Test number"]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])


(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:welp options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      )))

(defn exit [msg status]
  (println msg)
  (System/exit status))

(def print-menu
  (->> [""
        "Choose an option, please:"
        "0 - All tests"
        "1 - Test /auth endpoint"
        "2 - Test /me endpoint"
        "---"
        "q - Quit"]
      (string/join \newline)))

(def test-array
  {1 tests/call-to-root})


(defn -main
  [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      (exit (usage summary) 0)
      errors
      (exit errors 1)
      (:url options)
      (println (:url options)))
  (loop []
    (println print-menu)
    (let [input (string/trim-newline (read-line))]
      (when (= "q" input)
        (exit "That's all, thx" 0))
      (println (str "Running " input "to " (:url options)))
      ;;(resolve ((get test-array input) (:url options))))
      (tests/call-to-auth (:url options))
      (tests/call-to-root (:url options)))

    (recur))))
