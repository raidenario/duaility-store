(ns consulta-worker.config
  "Configurações centralizadas do Projection Worker"
  (:import [org.apache.kafka.clients.consumer ConsumerConfig]
           [org.apache.kafka.common.serialization StringDeserializer]
           [java.util Properties]))

;; ============================================================================
;; Configurações do Kafka
;; ============================================================================

(def kafka-config
  {:bootstrap-servers "localhost:9092"
   :consumer-group    "projector-group"
   :topics            ["orders" "stock-reserved" "payment-success" "payment-failed" "products"]
   :poll-timeout-ms   1000})

;; ============================================================================
;; Configurações do MongoDB
;; ============================================================================

(def mongo-config
  {:host       "localhost"
   :port       27018
   :db         "event_store_db"
   :username   "admin"
   :password   "password"
   :collections {:orders "orders"
                 :products "products"}})

;; ============================================================================
;; Propriedades do Kafka Consumer
;; ============================================================================

(defn consumer-props
  "Retorna Properties configuradas para o KafkaConsumer"
  []
  (doto (Properties.)
    (.put ConsumerConfig/BOOTSTRAP_SERVERS_CONFIG (:bootstrap-servers kafka-config))
    (.put ConsumerConfig/GROUP_ID_CONFIG (:consumer-group kafka-config))
    (.put ConsumerConfig/KEY_DESERIALIZER_CLASS_CONFIG StringDeserializer)
    (.put ConsumerConfig/VALUE_DESERIALIZER_CLASS_CONFIG StringDeserializer)
    (.put ConsumerConfig/AUTO_OFFSET_RESET_CONFIG "earliest")
    (.put ConsumerConfig/ENABLE_AUTO_COMMIT_CONFIG "true")
    (.put ConsumerConfig/AUTO_COMMIT_INTERVAL_MS_CONFIG "1000")))

