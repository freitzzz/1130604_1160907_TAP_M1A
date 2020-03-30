name := "viva2020"

version := "0.1"

scalaVersion := "2.13.1"

// Show deprecation
scalacOptions ++= Seq("-deprecation", "-unchecked")

// XMl
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.3.0"
libraryDependencies += "software.purpledragon.xml" %% "xml-compare" % "1.2.0"

// ScalaTest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % "test"

// ScalaCheck
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"

// WartRemover: Mandatory linter
wartremoverErrors ++= Seq(Wart.Enumeration,Wart.Return,Wart.Throw,Wart.Var,Wart.While)
wartremoverWarnings ++= Seq(Wart.Recursion,Wart.TraversableOps)