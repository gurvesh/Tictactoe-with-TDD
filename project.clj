(defproject ttt-tdd "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.9.5"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [enlive "1.1.6"]]
  :profiles {:dev {:dependencies [[speclj "3.3.1"]]}}
  :plugins [[speclj "3.3.1"]
            [lein-ring "0.12.6"]]
  :ring {:handler ttt-tdd.web/handler}
  :test-paths ["spec"]
  ;; :jvm-opts ["-Xms2G" "-Xmx4g"]
  :main ^:skip-aot ttt-tdd.core)
