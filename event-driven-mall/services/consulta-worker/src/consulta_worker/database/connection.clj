(ns consulta-worker.database.connection
  "Módulo responsável pela conexão com o MongoDB"
  (:require [monger.core :as mg]
            [consulta-worker.config :as config]
            [clojure.tools.logging :as log]))

;; ============================================================================
;; Conexão com MongoDB
;; ============================================================================

(defn build-connection-uri
  "Constrói a URI de conexão do MongoDB"
  []
  (let [{:keys [username password host port db]} config/mongo-config]
    (format "mongodb://%s:%s@%s:%d/%s?authSource=admin"
            username password host port db)))

(defn connect!
  "Estabelece conexão com o MongoDB e retorna conn e db"
  []
  (log/info "🚀 Conectando ao MongoDB...")
  (try
    (let [uri (build-connection-uri)
          {:keys [conn db]} (mg/connect-via-uri uri)]
      (log/info "✅ MongoDB conectado com sucesso!")
      (log/info "   Database:" (:db config/mongo-config))
      (log/info "   Collections:" (:collections config/mongo-config))
      {:conn conn :db db})
    (catch Exception e
      (log/error "❌ Erro ao conectar no MongoDB:" (.getMessage e))
      (throw e))))

(defn disconnect!
  "Fecha a conexão com o MongoDB"
  [conn]
  (when conn
    (log/info "🧊 Fechando conexão com MongoDB...")
    (mg/disconnect conn)
    (log/info "✅ MongoDB desconectado")))

(defn health-check
  "Verifica se a conexão com MongoDB está ativa"
  [db]
  (try
    (mg/get-db-names (:conn db))
    true
    (catch Exception _
      false)))
