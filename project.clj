(defproject gobgp-grpc-java "0.1.0-SNAPSHOT"
  :description "call gobgp-api from java"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :repl-options {:init-ns gobgp-grpc-java.core}
  :main ^skip-aot gobgpapi.example.Client
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [javax.annotation/javax.annotation-api "1.3.2"]
                 [com.google.protobuf/protobuf-java "3.21.7"]
                 [io.grpc/grpc-api "1.50.1"]
                 [io.grpc/grpc-core "1.50.1" :exclusions [io.grpc/grpc-api]]
                 [io.grpc/grpc-netty-shaded "1.50.1" :exclusions [io.grpc/grpc-api io.grpc/grpc-core]]
                 [io.grpc/grpc-protobuf "1.50.1"]
                 [io.grpc/grpc-stub "1.50.1"]]

  :plugins [[lein-protoc "0.5.0"]
            [lein-cljfmt "0.6.7"]
            [jonase/eastwood "0.3.10"]
            [lein-kibit "0.1.8"]
           ]

  ; for lein-protoc
  :protoc-version "3.11.4"
  :proto-source-paths ["src/main/proto"]
  :protoc-grpc {:version "1.50.1"}
  :proto-target-path "target/generated-sources/protobuf"
  :java-source-pathes ["target/generated-sources/protobuf"]

  ; for lein-javac
  :java-source-paths ["src/main/java" "target/generated-sources/protobuf"]
  :javac-options {:debug "true"}

  ; for uberjar
  :jar-exclusions [#".java$" #"^main/" #"^META-INF/maven/" #"^clojure/" ]
)
