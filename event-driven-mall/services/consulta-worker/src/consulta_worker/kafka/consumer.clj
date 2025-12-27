(ns consulta-worker.kafka.consumer
  "Módulo responsável pela comunicação com o Kafka"
  (:require [consulta-worker.config :as config]
            [clojure.tools.logging :as log])
  (:import [org.apache.kafka.clients.consumer KafkaConsumer]
           [java.time Duration]))

;; ============================================================================
;; Criação e Gerenciamento do Consumer
;; ============================================================================

(defn create-consumer!
  "Cria e configura um KafkaConsumer"
  []
  (log/info "🔌 Criando Kafka Consumer...")
  (let [consumer (KafkaConsumer. (config/consumer-props))]
    (.subscribe consumer (:topics config/kafka-config))
    (log/info "✅ Kafka Consumer criado e inscrito nos tópicos:" (:topics config/kafka-config))
    consumer))

(defn poll-records
  "Busca registros do Kafka com timeout configurável"
  ([consumer]
   (poll-records consumer (:poll-timeout-ms config/kafka-config)))
  ([consumer timeout-ms]
   (.poll consumer (Duration/ofMillis timeout-ms))))

(defn close-consumer!
  "Fecha o consumer gracefully"
  [consumer]
  (log/info "🔌 Fechando Kafka Consumer...")
  (.close consumer)
  (log/info "✅ Kafka Consumer fechado"))

;; ============================================================================
;; Extração de Dados dos Records
;; ============================================================================

(defn extract-topic
  "Extrai o tópico de um record"
  [record]
  (.topic record))

(defn extract-key
  "Extrai a chave de um record"
  [record]
  (.key record))

(defn extract-value
  "Extrai o valor de um record"
  [record]
  (.value record))

(defn extract-partition
  "Extrai a partição de um record"
  [record]
  (.partition record))

(defn extract-offset
  "Extrai o offset de um record"
  [record]
  (.offset record))

