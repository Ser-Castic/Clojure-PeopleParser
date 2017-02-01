(ns people-parser.core
  (:require [clojure.string :as str]
            [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [hiccup.core :as h])
  (:gen-class))

(defonce server (atom nil))

(defn read-people [country-name]
  (let [people (slurp "people.csv")
        people (str/split-lines people)
        people (map #(str/split % #",") people)
        header (first people)
        people (rest people)
        people (map (fn [pants]
                      (zipmap header pants))
                    people)]
    (if (.isEmpty country-name)
      people
      (filter (fn [line]
                (.equalsIgnoreCase (get line "country") country-name)) people))))

(defn people-html [country-name]
  (let [people (read-people country-name)]
    [:ol
     (map (fn [person]
            [:li (str (get person "first_name") " " (get person "last_name"))])
          people)]))

(defn -main []
  (c/defroutes app
    (c/GET "/:country{.*}" [country]
       (h/html [:html
                [:body (people-html country)]])))

  (when @server
    (.stop @server))

  (reset! server (j/run-jetty app {:port 3000 :join? false})))