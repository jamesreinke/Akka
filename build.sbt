name := "Akka"
version := "0.0.1"
lazy val root = (project in file(".")).enablePlugins()
scalaVersion := "2.10.4"
libraryDependencies ++= Seq(
"com.typesafe.akka"	%%	"akka-actor"	%	"2.3.9",
"com.typesafe.akka" %% 	"akka-remote" 	% 	"2.3.9"
)