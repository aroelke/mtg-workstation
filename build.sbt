import java.util.Properties
import sys.process._

val appProperties = settingKey[Properties]("Global application properties.")
appProperties := {
  val prop = new Properties()
  IO.load(prop, new File("src/main/resources/project.properties"))
  prop
}

name := "mtg-workstation"
version := appProperties.value.getProperty("version")

scalaVersion := "3.0.1"
libraryDependencies ++= Seq(
  "com.google.code.gson" % "gson" % "2.8.5",
  "com.joestelmach" % "natty" % "0.12",
  "org.jfree" % "jfreechart" % "1.5.2"
)

fork := true
javaOptions ++= Seq("-Dsun.java2d.d3d=false", "-Dfile.encoding=UTF-8")
semanticdbEnabled := true

assembly / assemblyJarName := s"${name.value}-${version.value}.jar"

lazy val jpackage = taskKey[Unit]("Generates a full Windows installer.")
jpackage := {
  Seq("jpackage",
    "--input", assembly.value.getParent(),
    "--main-jar", assembly.value.getName(),
    "--name", "MTG Workstation",
    "--win-dir-chooser", "--win-menu", "--win-shortcut",
  ) ++ javaOptions.value.flatMap(Seq("--java-options", _)) ++ Seq(
    "--icon", "src\\main\\resources\\icon\\icon.ico",
    "--app-version", version.value,
    "--dest", assembly.value.getParent()
  ) !
}