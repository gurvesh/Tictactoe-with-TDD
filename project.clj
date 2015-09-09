(defproject ttt-tdd "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [hiccup "1.0.5"]
                 [seesaw "1.4.5"]
                 [enlive "1.1.6"]]
  :profiles {:dev {:dependencies [[speclj "3.3.1"]]}}
  :plugins [[speclj "3.3.1"]
            [lein-ring "0.9.6"]]
  :ring {:handler ttt-tdd.web2/handler}
  :test-paths ["spec"]
  ;; :jvm-opts ["-Xms2G" "-Xmx4g"]
  :main ^:skip-aot ttt-tdd.core)
