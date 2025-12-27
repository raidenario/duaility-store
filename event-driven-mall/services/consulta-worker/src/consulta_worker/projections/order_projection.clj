(ns consulta-worker.projections.order-projection
  "Projeções relacionadas a pedidos (orders)"
  (:require [consulta-worker.database.repository :as repo]
            [consulta-worker.utils.time :as time-utils]
            [clojure.tools.logging :as log]))

;; ============================================================================
;; Projeção: Order Created
;; ============================================================================

(defn project-order-created!
  "Cria documento inicial do pedido no MongoDB"
  [db event]
  (let [order-id (:orderId event)
        order-doc {:_id       order-id
                   :orderId   order-id
                   :userId    (:userId event)
                   :amount    (:totalAmount event)
                   :items     (:items event)
                   :status    "CREATED"
                   :createdAt (time-utils/now-iso)
                   :updatedAt (time-utils/now-iso)
                   :history   [{:status "CREATED" 
                               :timestamp (time-utils/now-iso)
                               :description "Pedido criado"}]}]
    
    (repo/upsert! db {:_id order-id} order-doc)
    (log/info "📝 [Projector] Pedido criado:" order-id)
    (log/debug "   Detalhes:" order-doc)))

;; ============================================================================
;; Projeção: Stock Reserved
;; ============================================================================

(defn project-stock-reserved!
  "Atualiza status do pedido para STOCK_RESERVED"
  [db event]
  (let [order-id (:orderId event)
        update-doc {"$set" {:status "STOCK_RESERVED"
                           :stockReservedAt (time-utils/now-iso)
                           :updatedAt (time-utils/now-iso)}
                    "$push" {:history {:status "STOCK_RESERVED"
                                      :timestamp (time-utils/now-iso)
                                      :description "Estoque reservado com sucesso"}}}]
    
    (repo/update! db {:_id order-id} update-doc)
    (log/info "📦 [Projector] Estoque reservado para pedido:" order-id)))

;; ============================================================================
;; Projeção: Payment Success
;; ============================================================================

(defn project-payment-success!
  "Atualiza status do pedido para COMPLETED"
  [db event]
  (let [order-id (:orderId event)
        payment-id (:paymentId event)
        update-doc {"$set" {:status "COMPLETED"
                           :paymentId payment-id
                           :completedAt (time-utils/now-iso)
                           :updatedAt (time-utils/now-iso)}
                    "$push" {:history {:status "COMPLETED"
                                      :timestamp (time-utils/now-iso)
                                      :paymentId payment-id
                                      :description "Pagamento processado com sucesso"}}}]
    
    (repo/update! db {:_id order-id} update-doc)
    (log/info "💳 [Projector] Pagamento aprovado para pedido:" order-id)
    (log/info "   Payment ID:" payment-id)))

;; ============================================================================
;; Projeção: Payment Failed (Futuro)
;; ============================================================================

(defn project-payment-failed!
  "Atualiza status do pedido para PAYMENT_FAILED"
  [db event]
  (let [order-id (:orderId event)
        reason (:reason event "Erro desconhecido")
        update-doc {"$set" {:status "PAYMENT_FAILED"
                           :failedAt (time-utils/now-iso)
                           :updatedAt (time-utils/now-iso)
                           :failureReason reason}
                    "$push" {:history {:status "PAYMENT_FAILED"
                                      :timestamp (time-utils/now-iso)
                                      :reason reason
                                      :description "Falha no processamento do pagamento"}}}]
    
    (repo/update! db {:_id order-id} update-doc)
    (log/warn "❌ [Projector] Pagamento falhou para pedido:" order-id)
    (log/warn "   Motivo:" reason)))

;; ============================================================================
;; Projeção: Order Cancelled (Futuro)
;; ============================================================================

(defn project-order-cancelled!
  "Atualiza status do pedido para CANCELLED"
  [db event]
  (let [order-id (:orderId event)
        reason (:reason event "Cancelado pelo usuário")
        update-doc {"$set" {:status "CANCELLED"
                           :cancelledAt (time-utils/now-iso)
                           :updatedAt (time-utils/now-iso)
                           :cancellationReason reason}
                    "$push" {:history {:status "CANCELLED"
                                      :timestamp (time-utils/now-iso)
                                      :reason reason
                                      :description "Pedido cancelado"}}}]
    
    (repo/update! db {:_id order-id} update-doc)
    (log/info "🚫 [Projector] Pedido cancelado:" order-id)
    (log/info "   Motivo:" reason)))

