name := "mtg-workstation"

scalaVersion := "3.0.1"
libraryDependencies ++= Seq(
    "com.google.code.gson" % "gson" % "2.8.5",
    "com.joestelmach" % "natty" % "0.12",
    "org.jfree" % "jfreechart" % "1.5.2"
)

fork := true
javaOptions ++= Seq("-Dsun.java2d.d3d=false", "-Dfile.encoding=UTF-8")