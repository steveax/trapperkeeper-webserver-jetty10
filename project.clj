(def jetty-version "10.0.15")

(defproject com.puppetlabs/trapperkeeper-webserver-jetty10 "1.0.1-SNAPSHOT"
  :description "A jetty10-based webserver implementation for use with the puppetlabs/trapperkeeper service framework."
  :url "https://github.com/puppetlabs/trapperkeeper-webserver-jetty10"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :min-lein-version "2.9.1"

  :parent-project {:coords [puppetlabs/clj-parent "6.0.0"]
                   :inherit [:managed-dependencies]}

  ;; Abort when version ranges or version conflicts are detected in
  ;; dependencies. Also supports :warn to simply emit warnings.
  ;; requires lein 2.2.0+.
  :pedantic? :abort
  :dependencies [[org.clojure/clojure]
                 [org.clojure/java.jmx]
                 [org.clojure/tools.logging]

                 [org.codehaus.janino/janino]
                 [org.flatland/ordered "1.5.9"]

                 [javax.servlet/javax.servlet-api "4.0.1"]
                 ;; Jetty Webserver
                 [org.eclipse.jetty/jetty-server ~jetty-version]
                 [org.eclipse.jetty/jetty-servlet ~jetty-version]
                 [org.eclipse.jetty/jetty-servlets ~jetty-version]
                 [org.eclipse.jetty/jetty-webapp ~jetty-version]
                 [org.eclipse.jetty/jetty-proxy ~jetty-version]
                 [org.eclipse.jetty/jetty-jmx ~jetty-version]
                 [org.eclipse.jetty.websocket/websocket-jetty-server ~jetty-version]

                 [prismatic/schema]
                 [ring/ring-servlet]
                 [ring/ring-codec]

                 [puppetlabs/ssl-utils]
                 [puppetlabs/kitchensink]
                 [puppetlabs/trapperkeeper]
                 [puppetlabs/i18n]
                 [puppetlabs/trapperkeeper-filesystem-watcher]

                 [org.slf4j/jul-to-slf4j]]

  :source-paths  ["src"]
  :java-source-paths  ["java"]

  :plugins [[lein-parent "0.3.7"]
            [puppetlabs/i18n "0.8.0"]]

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/clojars_jenkins_username
                                     :password :env/clojars_jenkins_password
                                     :sign-releases false}]]

  ;; By declaring a classifier here and a corresponding profile below we'll get an additional jar
  ;; during `lein jar` that has all the code in the test/ directory. Downstream projects can then
  ;; depend on this test jar using a :classifier in their :dependencies to reuse the test utility
  ;; code that we have.
  :classifiers [["test" :testutils]]

  :test-paths ["test/clj"]

  :profiles {:defaults {:source-paths ["examples/multiserver_app/src"
                                       "examples/ring_app/src"
                                       "examples/servlet_app/src/clj"
                                       "examples/war_app/src"
                                       "examples/webrouting_app/src"]
                        :java-source-paths ["examples/servlet_app/src/java"
                                            "test/java"]
                        :resource-paths ["dev-resources"]
                        :jvm-opts ["-Djava.util.logging.config.file=dev-resources/logging.properties"]}
             :pseudo-dev [:defaults
                          {:dependencies [[puppetlabs/http-client]
                                          [puppetlabs/kitchensink nil :classifier "test"]
                                          [puppetlabs/trapperkeeper nil :classifier "test"]
                                          [org.clojure/tools.namespace]
                                          [compojure]
                                          [ring/ring-core]
                                          [ch.qos.logback/logback-classic "1.2.12"]
                                          [ch.qos.logback/logback-core "1.2.12"]
                                          [ch.qos.logback/logback-access "1.2.12"]
                                          [hato "0.9.0"]]}]
             :dev [:defaults
                   :pseudo-dev
                   {:dependencies [[org.bouncycastle/bcpkix-jdk18on]
                                   [hato "0.9.0"]]}]

             ;; per https://github.com/technomancy/leiningen/issues/1907
             ;; the provided profile is necessary for lein jar / lein install
             :provided {:dependencies [[org.bouncycastle/bcpkix-jdk18on]]
                        :resource-paths ["dev-resources"]}

             :fips {:dependencies [[org.bouncycastle/bcpkix-fips]
                                   [org.bouncycastle/bc-fips]
                                   [org.bouncycastle/bctls-fips]
                                   [hato "0.9.0"]]
                     :exclusions [[org.bouncycastle/bcpkix-jdk18on]]
                     ;; this only ensures that we run with the proper profiles
                     ;; during testing. This JVM opt will be set in the puppet module
                     ;; that sets up the JVM classpaths during installation.
                     :jvm-opts ~(let [version (System/getProperty "java.version")
                                      [major minor _] (clojure.string/split version #"\.")
                                      unsupported-ex (ex-info "Unsupported major Java version. Expects 11 or 17."
                                                       {:major major
                                                        :minor minor})]
                                  (condp = (java.lang.Integer/parseInt major)
                                    11 ["-Djava.security.properties==dev-resources/jdk11-fips-security"]
                                    17 ["-Djava.security.properties==dev-resources/jdk17-fips-security"]
                                    (throw unsupported-ex)))}

             :testutils {:source-paths ^:replace ["test/clj"]
                         :java-source-paths ^:replace ["test/java"]}}

  :main puppetlabs.trapperkeeper.main

  :repositories [["puppet-releases" "https://artifactory.delivery.puppetlabs.net/artifactory/list/clojure-releases__local/"]
                 ["puppet-snapshots" "https://artifactory.delivery.puppetlabs.net/artifactory/list/clojure-snapshots__local/"]])

