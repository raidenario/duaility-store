(ns payment-worker.core
  "Worker de Pagamento: Escuta 'stock-reserved' e publica 'payment-success'"
  (:require [clojure.tools.logging :as log]
            [cheshire.core :as json])
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
   :output-topic      "payment-success"})

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
;; Lógica de Pagamento (Simulada)
;; ============================================================================

(defn process-payment
  "Simula processamento de pagamento com gateway externo"
  [stock-event]
  (log/info "💳 [Payment] Processando pagamento para pedido:" (:orderId stock-event))
  
  ;; Simula latência de gateway (Stripe/PayPal)
  (Thread/sleep 300)
  
  ;; Retorna evento de sucesso
  {:orderId      (:orderId stock-event)
   :status       "PAYMENT_SUCCESS"
   :paymentId    (str (UUID/randomUUID))
   :processedAt  (str (java.time.Instant/now))})

;; ============================================================================
;; Kafka Consumer/Producer
;; ============================================================================

(defn start-consumer! []
  (let [consumer (KafkaConsumer. (consumer-props))
        producer (KafkaProducer. (producer-props))]
    
    (.subscribe consumer [(:input-topic kafka-config)])
    (log/info "🚀 [Payment Worker] Iniciado! Escutando:" (:input-topic kafka-config))
    
    (try
      (while true
        (let [records (.poll consumer (Duration/ofMillis 1000))]
          (doseq [record records]
            (try
              (let [stock-event (json/parse-string (.value record) true)
                    payment-result (process-payment stock-event)
                    output-record (ProducerRecord. 
                                    (:output-topic kafka-config)
                                    (:orderId stock-event)
                                    (json/generate-string payment-result))]
                
                (.send producer output-record)
                (log/info "✅ [Payment] Pagamento aprovado! Pedido:" (:orderId stock-event)))
              
              (catch Exception e
                (log/error "❌ [Payment] Erro ao processar:" (.getMessage e)))))))
      
      (finally
        (.close consumer)
        (.close producer)))))

;; ============================================================================
;; Main
;; ============================================================================

(defn -main
  "Ponto de entrada do Payment Worker"
  [& args]
  (log/info "═══════════════════════════════════════════════════")
  (log/info "       💳 PAYMENT WORKER - Event-Driven Mall")
  (log/info "═══════════════════════════════════════════════════")
  (start-consumer!))
