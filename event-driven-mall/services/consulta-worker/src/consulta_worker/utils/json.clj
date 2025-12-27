(ns consulta-worker.utils.json
  "Utilitários para manipulação de JSON"
  (:require [cheshire.core :as cheshire]
            [clojure.tools.logging :as log]))

;; ============================================================================
;; Parsing e Serialização
;; ============================================================================

(defn parse
  "Parse JSON string para Clojure map (com keywords)"
  [json-string]
  (try
    (cheshire/parse-string json-string true)
    (catch Exception e
      (log/error "❌ Erro ao fazer parse do JSON:" (.getMessage e))
      (log/debug "JSON inválido:" json-string)
      nil)))

(defn parse-strict
  "Parse JSON string (lança exceção em caso de erro)"
  [json-string]
  (cheshire/parse-string json-string true))

(defn generate
  "Gera JSON string a partir de Clojure map"
  [clj-map]
  (try
    (cheshire/generate-string clj-map)
    (catch Exception e
      (log/error "❌ Erro ao gerar JSON:" (.getMessage e))
      nil)))

(defn generate-pretty
  "Gera JSON string formatado (pretty-print)"
  [clj-map]
  (try
    (cheshire/generate-string clj-map {:pretty true})
    (catch Exception e
      (log/error "❌ Erro ao gerar JSON:" (.getMessage e))
      nil)))

;; ============================================================================
;; Validação
;; ============================================================================

(defn valid-json?
  "Verifica se uma string é um JSON válido"
  [json-string]
  (try
    (cheshire/parse-string json-string)
    true
    (catch Exception _
      false)))

(defn has-keys?
  "Verifica se um map possui todas as chaves especificadas"
  [m required-keys]
  (every? #(contains? m %) required-keys))

