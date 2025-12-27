(ns consulta-worker.core
  "Projector Worker - Orquestração principal do serviço"
  (:require [clojure.tools.logging :as log]
            [consulta-worker.config :as config]
            [consulta-worker.kafka.consumer :as kafka]
            [consulta-worker.database.connection :as db-conn]
            [consulta-worker.handlers.event-handler :as handler]
            [consulta-worker.utils.json :as json-utils])
  (:gen-class))

;; ============================================================================
;; Estado da Aplicação
;; ============================================================================

(def ^:private app-state
  "Átomo para armazenar o estado da aplicação"
  (atom {:running? false
         :consumer nil
         :db-connection nil}))

;; ============================================================================
;; Processamento de Mensagens
;; ============================================================================

(defn process-record!
  "Processa um único record do Kafka"
  [db record]
  (let [topic (kafka/extract-topic record)
        value (kafka/extract-value record)
        event (json-utils/parse value)]
    
    (when event
      (log/debug "📨 Record recebido - Tópico:" topic "| Offset:" (kafka/extract-offset record))
      (handler/process-event! db topic event))))

(defn process-records!
  "Processa um batch de records do Kafka"
  [db records]
  (doseq [record records]
    (process-record! db record)))

;; ============================================================================
;; Loop Principal do Consumer
;; ============================================================================

(defn start-consumer-loop!
  "Inicia o loop principal de consumo de mensagens"
  [consumer db]
  (log/info "🔄 Iniciando loop de consumo de mensagens...")
  
  (while (:running? @app-state)
    (try
      (let [records (kafka/poll-records consumer)]
        (when (> (.count records) 0)
          (log/debug "📦 Recebidos" (.count records) "records")
          (process-records! db records)))
      
      (catch Exception e
        (log/error "❌ Erro no loop de consumo:" (.getMessage e))
        (log/debug "   Stack trace:" e)
        ;; Pequeno delay para evitar loop infinito em caso de erro persistente
        (Thread/sleep 1000)))))

;; ============================================================================
;; Inicialização e Shutdown
;; ============================================================================

(defn start!
  "Inicia o Projection Worker"
  []
  (log/info "═══════════════════════════════════════════════════")
  (log/info "       📊 PROJECTION WORKER - Event-Driven Mall")
  (log/info "═══════════════════════════════════════════════════")
  
  (try
    ;; Conecta ao MongoDB
    (let [{:keys [conn db]} (db-conn/connect!)
          ;; Cria o Kafka Consumer
          consumer (kafka/create-consumer!)]
      
      ;; Atualiza o estado da aplicação
      (swap! app-state assoc
             :running? true
             :consumer consumer
             :db-connection {:conn conn :db db})
      
      (log/info "✅ Projection Worker iniciado com sucesso!")
      (log/info "🎯 Aguardando eventos...")
      
      ;; Inicia o loop de consumo
      (start-consumer-loop! consumer db))
    
    (catch Exception e
      (log/error "❌ Erro ao iniciar Projection Worker:" (.getMessage e))
      (throw e))))

(defn stop!
  "Para o Projection Worker gracefully"
  []
  (log/info "🛑 Parando Projection Worker...")
  
  ;; Marca como não executando
  (swap! app-state assoc :running? false)
  
  ;; Fecha o consumer do Kafka
  (when-let [consumer (:consumer @app-state)]
    (kafka/close-consumer! consumer))
  
  ;; Fecha a conexão com MongoDB
  (when-let [conn (get-in @app-state [:db-connection :conn])]
    (db-conn/disconnect! conn))
  
  (log/info "✅ Projection Worker parado com sucesso!"))

;; ============================================================================
;; Shutdown Hook
;; ============================================================================

(defn add-shutdown-hook!
  "Adiciona hook para shutdown graceful"
  []
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread. ^Runnable stop!)))

;; ============================================================================
;; Main
;; ============================================================================

(defn -main
  "Ponto de entrada do Projection Worker"
  [& _args]
  (add-shutdown-hook!)
  (start!))
