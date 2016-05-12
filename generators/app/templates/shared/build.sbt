import Dependencies._

organization := "<%= props.organization %>"

name := "<%= props.appName %>"

scalaVersion in ThisBuild := "2.11.8"

lazy val svc = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.7",

      // DI
      "com.google.inject" % "guice" % "4.0",

      // MYSQL
      "com.typesafe.play" %% "play-slick" % "1.0.1",
      "com.typesafe.play" %% "play-slick-evolutions" % "1.0.1",
      "com.pellucid" %% "case-config" % "0.1.2",
      "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0",
      "mysql" % "mysql-connector-java" % "5.1.37" % Runtime,

      // Kafka
      "mc" %% "kafka-testkit" % "1.3.3" % Test,

      // Elastic
      "mm" %% "estesttoolslib" % "1.0.1" % Test,

      // TEST
      "mm" %% "testinglib" % "1.0.0" % Test,
      "org.scalatestplus" %% "play" % "1.4.0-M4" % Test
    ) ++ Dependencies.apidocDependencies, // project/Dependencies.scala (generated)

    javaOptions in Test += "-Dconfig.file=test/resources/application.conf",
    javaOptions in Test += "-Dlogger.file=test/resources/logback.xml",

    routesGenerator := play.routes.compiler.InjectedRoutesGenerator
  )
  .enablePlugins(PlayScala)

// ------------------------
// Release Settings
// ------------------------
// Release Process:
// Manual:
//   sbt
//   > release
//   > Release version [0.1.0] : ...
// Automatic:
//   sbt -Drelease_version=1.0.1 "release with-defaults"
releaseVersionBump := sbtrelease.Version.Bump.Minor
releaseTagName := version.value

val manualReleaseVersion = settingKey[String]("We're going to manage the version")
manualReleaseVersion := sys.props.get("release_version").getOrElse(
  if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value
)

releaseTagName := manualReleaseVersion.value

releaseVersion := { ver =>
  sys.props.get("release_version").getOrElse(
    sbtrelease.Version(ver).map(_.withoutQualifier.string).getOrElse(sbtrelease.versionFormatError)
  )
}

publish <<= (publish) dependsOn  dist

publishLocal <<= (publishLocal) dependsOn dist

val publishDist = TaskKey[File]("dist-publish", "Publish dist")

artifact in publishDist ~= { (art: Artifact) => art.copy(`type` = "zip", extension = "zip") }

val publishDistSettings = Seq[Setting[_]] (
  publishDist <<= (target in Universal, normalizedName, version) map { (targetDir, id, version) =>
    val packageName = "%s" format(id)
    targetDir / (packageName + ".zip")
  }) ++ Seq(addArtifact(artifact in publishDist, publishDist).settings: _*)

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  packageName in Universal := moduleName.value,

  resolvers ++= Seq(
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
    "Movio" at "https://artifactory.movio.co/artifactory/repo",
    "Pellucid Bintray" at "http://dl.bintray.com/pellucid/maven"
  ),


  // Optional
  // Disable jar for this project (useless)
  publishArtifact in (Compile, packageBin) := false,

  // Disable scaladoc generation for this project (useless)
  publishArtifact in (Compile, packageDoc) := false,

  // Disable source jar for this project (useless)
  publishArtifact in (Compile, packageSrc) := false,

  scalacOptions ++= Seq(
    "-Xlint",
    "-deprecation",
    "-feature"
  ),

  publishTo := {
    val repo = "https://artifactory.movio.co/artifactory/"
    if (isSnapshot.value)
      Some("movio snapshots" at repo + "libs-snapshot-local")
    else
      Some("movio releases" at repo + "libs-release-local")
  },

  fork in Test := true
) ++ publishDistSettings
