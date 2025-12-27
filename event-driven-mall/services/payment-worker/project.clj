(defproject payment-worker "0.1.0-SNAPSHOT"
  :description "Worker de Pagamento: Escuta eventos de estoque reservado e processa pagamentos"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  ;; Windows + caminhos com acento: evita problemas de classpath (trampoline)
  ;; Se preferir, troque para outro caminho ASCII (ex: C:/Users/Public/m2).
  :local-repo "C:/m2"

  ;; Evita bugs de encoding/paths no Windows quando TEMP tem acento no username.
  :jvm-opts ["-Djava.io.tmpdir=C:/temp" "-Dfile.encoding=UTF-8"]

  ;; Deixa explícito (ajuda o Lein no Windows)
  :source-paths ["src"]
  :resource-paths ["resources"]
  :test-paths ["test"]

  :dependencies [;; 1. Core
                 [org.clojure/clojure "1.11.1"]

                 ;; 2. Apache Kafka (Cliente Java Oficial)
                 ;; Fundamental para ler 'StockReserved' e publicar 'PaymentSuccess'
                 [org.apache.kafka/kafka-clients "3.5.1"]

                 ;; 3. JSON (Cheshire)
                 ;; Para processar os payloads dos eventos
                 [cheshire "5.11.0"]

                 ;; 4. Cliente HTTP (clj-http)
                 ;; Adicionado para simular chamadas a gateways de pagamento (Stripe/PayPal)
                 [clj-http "3.12.3"]

                 ;; 5. Logging (Kafka 3.5.x usa SLF4J 1.7.x → logback 1.2.x)
                 [org.clojure/tools.logging "1.2.4"]
                 [ch.qos.logback/logback-classic "1.2.13"]]

  :main ^:skip-aot payment-worker.core
  
  :aliases {"run!" ["trampoline" "run"]}

  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})