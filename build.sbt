name := """touiteur"""

version := "1.0-SNAPSHOT"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.7"
)

def CommonProject(name: String): Project =
  Project(name, file(name)).
    settings(commonSettings: _*).
    settings(
      libraryDependencies ++= Seq(ws//, "com.typesafe.play" %% "play-streams-experimental" % "2.5.x"
        )
    ).enablePlugins(PlayScala)

lazy val consumer = {
  CommonProject("consumer")
}

lazy val tweeterFeed = {
  CommonProject("tweeterFeed")
}

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

PlayKeys.playOmnidoc := false
