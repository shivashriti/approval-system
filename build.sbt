lazy val akka = Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.10",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
)

lazy val persistance = Seq(
  "com.typesafe.slick" %% "slick" % "3.3.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.0",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42"
)

lazy val test = Seq(
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.23",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.10",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

lazy val commonSettings = Seq(
  scalaVersion := "2.12.8",
  version := "0.1.0",
  libraryDependencies ++= akka ++ persistance ++ test,
  dockerfile in docker := {
    val artifact: File = assembly.value
    val artifactTargetPath = s"/app/${artifact.name}"

    new Dockerfile {
      from("openjdk:8-jre")
      add(artifact, artifactTargetPath)
      entryPoint("java", "-jar", artifactTargetPath)
    }
  }
)

lazy val orderService = (project in file("order-service"))
  .settings(
    commonSettings,
    name := "orderService",
    assemblyJarName in assembly := "orderService.jar",
    mainClass in assembly := Some("com.shivashriti.order.OrderServer")
  )
  .enablePlugins(DockerPlugin)

lazy val approvalService = (project in file("approval-service"))
  .settings(
    commonSettings,
    name := "approvalService",
    assemblyJarName in assembly := "orderService.jar",
    mainClass in assembly := Some("com.shivashriti.admin.Server"),
  )
  .enablePlugins(DockerPlugin)

lazy val root = (project in file("."))
  .settings(
    commonSettings
  )