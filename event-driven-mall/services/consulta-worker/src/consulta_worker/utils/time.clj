(ns consulta-worker.utils.time
  "Utilitários para manipulação de data e hora"
  (:import [java.time Instant ZoneId]
           [java.time.format DateTimeFormatter]))

;; ============================================================================
;; Funções de Timestamp
;; ============================================================================

(defn now-iso
  "Retorna timestamp atual no formato ISO-8601"
  []
  (str (Instant/now)))

(defn now-millis
  "Retorna timestamp atual em milissegundos"
  []
  (.toEpochMilli (Instant/now)))

(defn now-seconds
  "Retorna timestamp atual em segundos"
  []
  (.getEpochSecond (Instant/now)))

;; ============================================================================
;; Formatação de Data
;; ============================================================================

(defn format-instant
  "Formata um Instant com o padrão especificado"
  [instant pattern]
  (let [formatter (DateTimeFormatter/ofPattern pattern)
        zoned-dt (.atZone instant (ZoneId/systemDefault))]
    (.format formatter zoned-dt)))

(defn format-now
  "Formata o timestamp atual com o padrão especificado"
  [pattern]
  (format-instant (Instant/now) pattern))

;; ============================================================================
;; Conversões
;; ============================================================================

(defn millis->instant
  "Converte milissegundos para Instant"
  [millis]
  (Instant/ofEpochMilli millis))

(defn seconds->instant
  "Converte segundos para Instant"
  [seconds]
  (Instant/ofEpochSecond seconds))

(defn iso-string->instant
  "Converte string ISO-8601 para Instant"
  [iso-string]
  (Instant/parse iso-string))

