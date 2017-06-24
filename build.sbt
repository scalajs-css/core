import org.scalajs.core.tools.linker.backend.ModuleKind.CommonJSModule

name := "core"

//version := "2017.6.0-SNAPSHOT"

enablePlugins(ScalaJSPlugin)

val scala211 = "2.11.11"

val scala212 = "2.12.2"

scalaVersion := scala211

crossScalaVersions := Seq(scala211, scala212)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-Xmacro-settings:autoprefix=true",
  "-Xmacro-settings:classShrink=components",
  "-P:scalajs:sjsDefinedByDefault" // TODO remove this when we upgrade to scala.js 1.0
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
  "org.scala-js" %%% "scalajs-dom" % "0.9.1" % Provided)

//bintray
resolvers += Resolver.jcenterRepo

organization := "scalajs-css"

licenses += ("Apache-2.0", url(
  "https://www.apache.org/licenses/LICENSE-2.0.html"))

bintrayOrganization := Some("scalajs-css")

bintrayRepository := "maven"

publishArtifact in Test := false

//Test

scalaJSModuleKind := CommonJSModule
resolvers += Resolver.bintrayRepo("scalajs-css", "maven")
libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % Test
libraryDependencies ++= Seq("io.scalajs" %%% "nodejs" % "0.4.0-pre2" % Test,
                            "org.scala-js" %%% "scalajs-dom" % "0.9.1" % Test)

//scalaJSStage in Global := FastOptStage
scalaJSStage in Global := FullOptStage
