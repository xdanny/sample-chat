val Http4sVersion = "0.19.0-M1"
val Specs2Version = "4.2.0"
val CirceVersion = "0.9.3"
val LogbackVersion = "1.2.3"
val DoobieVersion = "0.5.2"
val FlywayVersion = "4.2.0"
val PureConfigVersion = "0.9.1"

scalacOptions ++= Seq("-Ypartial-unification")

lazy val root = (project in file("."))
  .settings(
    name := "sample-chat-backend",
    version := "0.0.1-SNAPSHOT",
    scalaVersion :=  "2.12.6",
    libraryDependencies ++= Seq(
  "org.http4s"            %% "http4s-blaze-server"        % Http4sVersion,
  "org.http4s"            %% "http4s-circe"               % Http4sVersion,
  "org.http4s"            %% "http4s-dsl"                 % Http4sVersion,
  "org.specs2"            %% "specs2-core"                % Specs2Version % "test",
  "ch.qos.logback"        %  "logback-classic"            % LogbackVersion,
  "net.debasishg"         %% "redisclient"                % "3.7",
  "io.circe"              %% "circe-core"                 % CirceVersion,
  "io.circe"              %% "circe-parser"               % CirceVersion,
  "org.tpolecat"          %% "doobie-core"                % DoobieVersion,
  "org.tpolecat"          %% "doobie-postgres"            % DoobieVersion,
  "org.tpolecat"          %% "doobie-hikari"              % DoobieVersion,
  "com.github.pureconfig" %% "pureconfig"                 % PureConfigVersion,
  "org.flywaydb"          %  "flyway-core"                % FlywayVersion,
  "io.dropwizard.metrics" % "metrics-core"                % "4.0.3",
  "com.typesafe.akka"     %% "akka-actor"                 % "2.5.14",
  "com.typesafe.akka"     %% "akka-testkit"               % "2.5.14" % Test
)
)