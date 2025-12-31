(ns consulta-worker.projections.product-projection
  "Projeções relacionadas a produtos"
  (:require [consulta-worker.database.repository :as repo]
            [consulta-worker.utils.time :as time-utils]
            [clojure.tools.logging :as log]))

(defn project-product-created!
  "Insere o produto no modelo de leitura (MongoDB)"
  [db event]
  (let [product-id (:productId event)
        now (time-utils/now-iso)
        created-at (or (:createdAt event) now)
        doc {:_id        product-id
             :productId  product-id
             :name       (:name event)
             :type       (:type event)
             :price      (:price event)
             :createdAt  (str created-at)
             :updatedAt  now
             :events     [{:stage "ProductCreatedEvent"
                           :description "Produto criado no lado de escrita (Postgres ➜ Kafka)"
                           :at now}]}]
    (repo/upsert! db {:_id product-id} doc :products)
    (log/info "[Projector] Produto projetado no Mongo:" product-id)))
