lazy val finchVersion = "0.15.0"
lazy val catsVersion = "0.9.0"
lazy val circeVersion = "0.8.0"
lazy val doobieVersion = "0.4.1"
lazy val fs2Version = "0.9.6"
lazy val featherbedVersion = "0.3.1"
lazy val scalatestVersion = "3.0.1"

enablePlugins(JavaAppPackaging)

val allSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.2",
  mainClass := Some("murzinov.moneytransfer.Main"),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "co.fs2" %% "fs2-core" % fs2Version,
    "com.github.finagle" %% "finch-core" % finchVersion,
    "com.github.finagle" %% "finch-circe" % finchVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "org.tpolecat" %% "doobie-core-cats" % doobieVersion,
    "org.tpolecat" %% "doobie-h2-cats" % doobieVersion,
    "org.tpolecat" %% "doobie-contrib-h2" % "0.3.0a",
    "org.scalatest" %% "scalatest" % scalatestVersion % "test",
    "io.github.finagle" %% "featherbed-core" % featherbedVersion % "test",
    "io.github.finagle" %% "featherbed-circe" % featherbedVersion % "test"
  ),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xlint",
    "-language:higherKinds",
    "-Ywarn-unused-import",
    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-nullary-override",
    "-Ywarn-numeric-widen",
    "-Xfatal-warnings"
  )
)

lazy val moneyTransfer = project
  .in(file("."))
  .settings(name := "money-transfer")
  .settings(allSettings)
