(ns consulta-worker.database.repository
  "Módulo com operações genéricas de banco de dados"
  (:require [monger.collection :as mc]
            [consulta-worker.config :as config]
            [clojure.tools.logging :as log]))

;; ============================================================================
;; Operações CRUD Genéricas
;; ============================================================================

(defn upsert!
  "Realiza upsert (insert ou update) de um documento"
  [db query document]
  (try
    (mc/upsert db (:collection config/mongo-config) query document)
    (log/debug "✅ Upsert realizado:" query)
    true
    (catch Exception e
      (log/error "❌ Erro no upsert:" (.getMessage e))
      (throw e))))

(defn update!
  "Atualiza um documento existente"
  [db query update-doc]
  (try
    (mc/update db (:collection config/mongo-config) query update-doc)
    (log/debug "✅ Update realizado:" query)
    true
    (catch Exception e
      (log/error "❌ Erro no update:" (.getMessage e))
      (throw e))))

(defn find-one
  "Busca um único documento"
  [db query]
  (try
    (mc/find-one-as-map db (:collection config/mongo-config) query)
    (catch Exception e
      (log/error "❌ Erro ao buscar documento:" (.getMessage e))
      nil)))

(defn find-all
  "Busca todos os documentos que atendem a query"
  [db query]
  (try
    (mc/find-maps db (:collection config/mongo-config) query)
    (catch Exception e
      (log/error "❌ Erro ao buscar documentos:" (.getMessage e))
      [])))

(defn delete!
  "Remove um documento"
  [db query]
  (try
    (mc/remove db (:collection config/mongo-config) query)
    (log/debug "✅ Delete realizado:" query)
    true
    (catch Exception e
      (log/error "❌ Erro no delete:" (.getMessage e))
      (throw e))))

(defn count-documents
  "Conta documentos que atendem a query"
  [db query]
  (try
    (mc/count db (:collection config/mongo-config) query)
    (catch Exception e
      (log/error "❌ Erro ao contar documentos:" (.getMessage e))
      0)))

