name := """play-slick-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

routesGenerator := InjectedRoutesGenerator

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
//resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
//resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += Resolver.jcenterRepo
val AkkaVersion = "2.6.19"
libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-slick" % "5.0.0",
    "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
    "com.typesafe.play" %% "play-json" % "2.8.1",
    "com.h2database" % "h2" % "1.4.192",
    "com.google.code.gson" % "gson" % "2.2.4",
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % "test",
    specs2 % Test,
    guice
)

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
sourceDirectories in (Compile, TwirlKeys.compileTemplates) :=
  (unmanagedSourceDirectories in Compile).value

fork in run := true
