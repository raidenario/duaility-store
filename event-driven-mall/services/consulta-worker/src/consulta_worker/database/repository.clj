(ns consulta-worker.database.repository
  "Módulo com operações genéricas de banco de dados"
  (:require [monger.collection :as mc]
            [consulta-worker.config :as config]
            [clojure.tools.logging :as log]))

;; ============================================================================
;; Helpers
;; ============================================================================

(defn- resolve-collection
  [collection-key]
  (get-in config/mongo-config [:collections collection-key] (:orders (:collections config/mongo-config))))

;; ============================================================================
;; Operações CRUD Genéricas (com collection opcional)
;; ============================================================================

(defn upsert!
  "Realiza upsert (insert ou update) de um documento"
  ([db query document]
   (upsert! db query document :orders))
  ([db query document collection-key]
   (let [collection (resolve-collection collection-key)]
     (try
       (mc/upsert db collection query document)
       (log/debug "✅ Upsert realizado:" query "collection" collection)
       true
       (catch Exception e
         (log/error "❌ Erro no upsert:" (.getMessage e))
         (throw e))))))

(defn update!
  "Atualiza um documento existente"
  ([db query update-doc]
   (update! db query update-doc :orders))
  ([db query update-doc collection-key]
   (let [collection (resolve-collection collection-key)]
     (try
       (mc/update db collection query update-doc)
       (log/debug "✅ Update realizado:" query "collection" collection)
       true
       (catch Exception e
         (log/error "❌ Erro no update:" (.getMessage e))
         (throw e))))))

(defn find-one
  "Busca um único documento"
  ([db query]
   (find-one db query :orders))
  ([db query collection-key]
   (let [collection (resolve-collection collection-key)]
     (try
       (mc/find-one-as-map db collection query)
       (catch Exception e
         (log/error "❌ Erro ao buscar documento:" (.getMessage e))
         nil)))))

(defn find-all
  "Busca todos os documentos que atendem a query"
  ([db query]
   (find-all db query :orders))
  ([db query collection-key]
   (let [collection (resolve-collection collection-key)]
     (try
       (mc/find-maps db collection query)
       (catch Exception e
         (log/error "❌ Erro ao buscar documentos:" (.getMessage e))
         [])))))

(defn delete!
  "Remove um documento"
  ([db query]
   (delete! db query :orders))
  ([db query collection-key]
   (let [collection (resolve-collection collection-key)]
     (try
       (mc/remove db collection query)
       (log/debug "✅ Delete realizado:" query "collection" collection)
       true
       (catch Exception e
         (log/error "❌ Erro no delete:" (.getMessage e))
         (throw e))))))

(defn count-documents
  "Conta documentos que atendem a query"
  ([db query]
   (count-documents db query :orders))
  ([db query collection-key]
   (let [collection (resolve-collection collection-key)]
     (try
       (mc/count db collection query)
       (catch Exception e
         (log/error "❌ Erro ao contar documentos:" (.getMessage e))
         0)))))
