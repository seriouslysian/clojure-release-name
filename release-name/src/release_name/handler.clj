(ns release-name.handler
  (:require [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [clj-http.client :as client]
    [cheshire.core :refer :all]))

(defn getDomain
  [domains]
  (str (get domains (rand-int (alength (to-array domains)))))
)

(defn domain
  []
  (getDomain
    (vector "mammal" "bird" "fish" "reptile"))
)

(defn getTotalItems
  [totalReturned]
  (def maxItems 5000)

  (if (> totalReturned maxItems)
    maxItems
    totalReturned)
)

(defn getSingleAnimal
  [url]
  (def animalsApi (
      client/get url{
        :headers {
          :app_id "4503f41d", 
          :app_key "25d720367050dc18e633a2a8d6a6496c"
        }
        :accept :json
      }))
    
  (def animalResults (parse-string (get-in animalsApi [:body])))
  (def totalAnimals (getTotalItems (get-in animalResults ["metadata" "total"])))
  (def animalIndex (rand-int totalAnimals))
  (def animals (get-in animalResults ["results"]))
  (def singleAnimal (get-in animals [animalIndex "word"]))

  (if (= singleAnimal nil)
    (do (println animalResults)
      (println totalAnimals)
      (println animalIndex)
      (println singleAnimal)
      ))
  (str singleAnimal)
)

(defn getSingleAdjective
  [url]
  (def adjectivesApi (
      client/get url{
        :headers {
          :app_id "4503f41d", 
          :app_key "25d720367050dc18e633a2a8d6a6496c"
        }
        :accept :json
      }))
  (def adjectiveResults (parse-string (get-in adjectivesApi [:body])))
  (def totalAdjectives (getTotalItems (get-in adjectiveResults ["metadata" "total"])))
  (def adjectiveIndex (rand-int totalAdjectives))
  (def adjectives (get-in adjectiveResults ["results"]))
  (def singleAdjective (get-in adjectives [adjectiveIndex "word"]))

  (if (= singleAdjective nil)
    (do (println (str "Results " adjectiveResults))
      (println (str "Total " totalAdjectives))
      (println (str "Index " adjectiveIndex))
      (println (str "Single " singleAdjective))
      ))
  (str singleAdjective)
)

(defn getAdjective
  [baseUrl releaseAnimal]
  (def firstLetter (subs releaseAnimal 0 1))
  (def url (str baseUrl "adjective?prefix=" firstLetter))
  (str (getSingleAdjective url))
)

(def hook-url "https://impactful.slack.com/")
(def auth-token "mkLEsf6TLuPqwaYrfcG6cZQT")

(defn generate-release-name
  []
  (def baseUrl "https://od-api.oxforddictionaries.com/api/v1/wordlist/en/lexicalCategory=")
  (def domainType (domain))
  (def url (str baseUrl "noun;domains=" domainType))
  (def releaseAnimal (getSingleAnimal url))
  (def releaseAdjective (getAdjective baseUrl releaseAnimal))

  (generate-string {:text (str releaseAdjective " " releaseAnimal)})
)

(defroutes app-routes
  (POST "/slack" [] 
    (generate-release-name))
  (route/resources "/")
  (route/not-found "Not Found")
)

(def app
  (wrap-defaults app-routes 
   (assoc-in site-defaults [:security :anti-forgery] false))
)