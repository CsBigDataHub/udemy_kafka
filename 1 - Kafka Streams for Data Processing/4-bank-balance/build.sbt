name := "4-bank-balance"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.3.1"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.28"
libraryDependencies += "com.banno" %% "kafka4s" % "2.1.0-M20"

libraryDependencies ++= ((version: String) => Seq(
  "com.github.cb372" %% "cats-retry-core"        % version,
  "com.github.cb372" %% "cats-retry-cats-effect" % version
))("0.3.1")

libraryDependencies ++= ((version: String) => Seq(
  "io.chrisdavenport" %% "log4cats-core"    % version,  // Only if you want to Support Any Backend
  "io.chrisdavenport" %% "log4cats-slf4j"   % version,  // Direct Slf4j Support - Recommended
))("1.0.1")

resolvers += "confluent" at "https://packages.confluent.io/maven/"

disableScalacFlag("-Ywarn-dead-code")
disableScalacFlag("-Xfatal-warnings")

def disableScalacFlag(flag: String) = scalacOptions := scalacOptions.value.filter(_ != flag)