(defproject projection-worker "0.1.0-SNAPSHOT"
  :description "Projection Worker: Consome eventos do Kafka e projeta no MongoDB (Read Model - CQRS)"
  :url "https://github.com/raidenario/event-driven-mall"
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

  :dependencies [;; 1. Core Clojure
                 [org.clojure/clojure "1.11.1"]

                 ;; 2. Apache Kafka (Cliente Java Oficial)
                 ;; Usado para consumir eventos dos tópicos
                 [org.apache.kafka/kafka-clients "3.5.1"]

                 ;; 3. MongoDB (Monger - Driver Clojure idiomático)
                 ;; Usado para persistir as projeções (Read Model)
                 [com.novemberain/monger "3.6.0"]

                 ;; 4. JSON (Cheshire - Parser/Generator JSON rápido)
                 ;; Usado para deserializar eventos do Kafka
                 [cheshire "5.11.0"]

                 ;; 5. Logging (SLF4J + Logback)
                 ;; Kafka 3.5.x usa SLF4J 1.7.x → logback 1.2.x
                 [org.clojure/tools.logging "1.2.4"]
                 [ch.qos.logback/logback-classic "1.2.13"]]

  ;; Define qual é o arquivo principal que o 'lein run' vai chamar
  :main ^:skip-aot consulta-worker.core
  
  ;; Alias para rodar com trampoline (evita problemas no Windows)
  :aliases {"run!" ["trampoline" "run"]}

  :target-path "target/%s"
  
  ;; Perfis de build
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:dependencies [[org.clojure/test.check "1.1.1"]]}})
