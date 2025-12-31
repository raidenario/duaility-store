(ns payment-worker.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(def db-spec
  {:dbtype "postgresql"
   :dbname "event_store_db"
   :host "localhost"
   :port 5433
   :user "admin"
   :password "password"})

(defn ensure-wallet-table!
  []
  (jdbc/execute! db-spec
                 ["CREATE TABLE IF NOT EXISTS wallet_accounts (
                     user_id VARCHAR(120) PRIMARY KEY,
                     balance NUMERIC(14,2) NOT NULL DEFAULT 3000.00
                   );"])
  (log/info "[Wallet] Tabela wallet_accounts verificada"))

(defn get-wallet
  [user-id]
  (first (jdbc/query db-spec ["SELECT user_id, balance FROM wallet_accounts WHERE user_id = ?" user-id])))

(defn create-wallet!
  [user-id]
  (jdbc/execute! db-spec ["INSERT INTO wallet_accounts(user_id, balance) VALUES (?, 3000.00) ON CONFLICT (user_id) DO NOTHING" user-id])
  (get-wallet user-id))

(defn charge!
  [user-id amount]
  (ensure-wallet-table!)
  (create-wallet! user-id)
  (jdbc/with-db-transaction [t-con db-spec]
    (let [row (first (jdbc/query t-con ["SELECT balance FROM wallet_accounts WHERE user_id = ? FOR UPDATE" user-id]))
          balance (:balance row)
          new-balance (when balance (- (bigdec balance) (bigdec amount)))]
      (if (and new-balance (>= new-balance 0M))
        (do
          (jdbc/execute! t-con ["UPDATE wallet_accounts SET balance = ? WHERE user_id = ?" new-balance user-id])
          {:status :ok :balance new-balance})
        {:status :insufficient}))))
