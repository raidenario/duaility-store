(ns consulta-worker.handlers.event-handler
  "Router de eventos - direciona cada evento para sua projeção correspondente"
  (:require [consulta-worker.projections.order-projection :as order-proj]
            [clojure.tools.logging :as log]))

;; ============================================================================
;; Multi-method para Roteamento de Eventos
;; ============================================================================

(defmulti handle-event
  "Multi-method que roteia eventos baseado no tópico do Kafka"
  (fn [_db topic _event] topic))

;; ============================================================================
;; Implementações por Tópico
;; ============================================================================

(defmethod handle-event "orders"
  [db _ event]
  (log/debug "📨 Processando evento: orders")
  (order-proj/project-order-created! db event))

(defmethod handle-event "stock-reserved"
  [db _ event]
  (log/debug "📨 Processando evento: stock-reserved")
  (order-proj/project-stock-reserved! db event))

(defmethod handle-event "payment-success"
  [db _ event]
  (log/debug "📨 Processando evento: payment-success")
  (order-proj/project-payment-success! db event))

;; ============================================================================
;; Eventos Futuros (Preparados para Expansão)
;; ============================================================================

(defmethod handle-event "payment-failed"
  [db _ event]
  (log/debug "📨 Processando evento: payment-failed")
  (order-proj/project-payment-failed! db event))

(defmethod handle-event "order-cancelled"
  [db _ event]
  (log/debug "📨 Processando evento: order-cancelled")
  (order-proj/project-order-cancelled! db event))

;; ============================================================================
;; Handler Padrão (Eventos Desconhecidos)
;; ============================================================================

(defmethod handle-event :default
  [_ topic _]
  (log/warn "⚠️  Tópico desconhecido recebido:" topic)
  (log/warn "   Nenhuma projeção configurada para este tópico"))

;; ============================================================================
;; Função Auxiliar de Processamento
;; ============================================================================

(defn process-event!
  "Processa um evento com tratamento de erros"
  [db topic event]
  (try
    (handle-event db topic event)
    (catch Exception e
      (log/error "❌ Erro ao processar evento do tópico:" topic)
      (log/error "   Mensagem de erro:" (.getMessage e))
      (log/error "   Evento:" event)
      (log/debug "   Stack trace:" e))))

