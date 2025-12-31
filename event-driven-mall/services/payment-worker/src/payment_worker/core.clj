(ns payment-worker.core
  "Worker de Pagamento: Escuta 'stock-reserved' e publica pagamento sucesso/falha considerando saldo"
  (:require [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [payment-worker.db :as db])
  (:import [org.apache.kafka.clients.consumer KafkaConsumer ConsumerConfig]
           [org.apache.kafka.clients.producer KafkaProducer ProducerConfig ProducerRecord]
           [org.apache.kafka.common.serialization StringSerializer StringDeserializer]
           [java.time Duration]
           [java.util Properties UUID])
  (:gen-class))

;; ============================================================================
;; Configuração
;; ============================================================================

(def kafka-config
  {:bootstrap-servers "localhost:9092"
   :consumer-group    "payment-group"
   :input-topic       "stock-reserved"
   :output-topic      "payment-success"
   :failed-topic      "payment-failed"})

(defn consumer-props []
  (doto (Properties.)
    (.put ConsumerConfig/BOOTSTRAP_SERVERS_CONFIG (:bootstrap-servers kafka-config))
    (.put ConsumerConfig/GROUP_ID_CONFIG (:consumer-group kafka-config))
    (.put ConsumerConfig/KEY_DESERIALIZER_CLASS_CONFIG StringDeserializer)
    (.put ConsumerConfig/VALUE_DESERIALIZER_CLASS_CONFIG StringDeserializer)
    (.put ConsumerConfig/AUTO_OFFSET_RESET_CONFIG "earliest")))

(defn producer-props []
  (doto (Properties.)
    (.put ProducerConfig/BOOTSTRAP_SERVERS_CONFIG (:bootstrap-servers kafka-config))
    (.put ProducerConfig/KEY_SERIALIZER_CLASS_CONFIG StringSerializer)
    (.put ProducerConfig/VALUE_SERIALIZER_CLASS_CONFIG StringSerializer)))

;; ============================================================================
;; Lógica de Pagamento com saldo
;; ============================================================================

(defn process-payment
  "Processa pagamento: debita saldo se houver fundos; caso contrário, gera falha."
  [stock-event]
  (let [user-id (:userId stock-event)
        amount  (bigdec (or (:totalAmount stock-event) 0))]
    (db/ensure-wallet-table!)
    (let [{:keys [status balance]} (db/charge! user-id amount)]
      (if (= status :ok)
        {:type :success
         :event {:orderId     (:orderId stock-event)
                 :status      "PAYMENT_SUCCESS"
                 :paymentId   (str (UUID/randomUUID))
                 :userId      user-id
                 :totalAmount amount
                 :balanceAfter balance
                 :processedAt (str (java.time.Instant/now))}}
        {:type :failed
         :event {:orderId     (:orderId stock-event)
                 :status      "PAYMENT_FAILED"
                 :userId      user-id
                 :totalAmount amount
                 :reason      "Saldo insuficiente"
                 :processedAt (str (java.time.Instant/now))}}))))

;; ============================================================================
;; Kafka Consumer/Producer
;; ============================================================================

(defn start-consumer! []
  (let [consumer (KafkaConsumer. (consumer-props))
        producer (KafkaProducer. (producer-props))]

    (.subscribe consumer [(:input-topic kafka-config)])
    (log/info "[Payment Worker] Iniciado! Escutando:" (:input-topic kafka-config))

    (try
      (while true
        (let [records (.poll consumer (Duration/ofMillis 1000))]
          (doseq [record records]
            (try
              (let [stock-event (json/parse-string (.value record) true)
                    {:keys [type event]} (process-payment stock-event)
                    topic (if (= type :success) (:output-topic kafka-config) (:failed-topic kafka-config))
                    output-record (ProducerRecord.
                                   topic
                                   (:orderId stock-event)
                                   (json/generate-string event))]
                (.send producer output-record)
                (log/info "[Payment] Resultado para pedido {}: {}", (:orderId stock-event) (:status event)))

              (catch Exception e
                (log/error "[Payment] Erro ao processar:" (.getMessage e)))))))

      (finally
        (.close consumer)
        (.close producer)))))

;; ============================================================================
;; Main
;; ============================================================================

(defn -main
  "Ponto de entrada do Payment Worker"
  [& _]
  (log/info "====================================")
  (log/info "       PAYMENT WORKER - Event-Driven Mall")
  (log/info "====================================")
  (start-consumer!))
