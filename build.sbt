// Nombre del proyecto
name := "C:\\uerre\\aplicacionesWeb\\Proyecto"

// Versión del proyecto
version := "1.0-SNAPSHOT"

// Configuración del proyecto principal
lazy val root = (project in file(".")).enablePlugins(PlayJava)

// Versión de Scala utilizada
scalaVersion := "2.11.7"

// Dependencias necesarias para el proyecto
libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  javaCore,
  "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final",
  "org.jsoup" % "jsoup" % "1.13.1",
  "javax.xml.bind" % "jaxb-api" % "2.3.1",
  "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.1"
)

dependencyOverrides += "javax.xml.bind" % "jaxb-api" % "2.3.1"
dependencyOverrides += "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.1"


// Configuración de Eclipse
// Compila el proyecto antes de generar los archivos para Eclipse
EclipseKeys.preTasks := Seq(compile in Compile, compile in Test)

// Configuración para un proyecto Java (sin Scala IDE)
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

// Utiliza archivos .class en lugar de archivos .scala generados para vistas y rutas
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(
  EclipseCreateSrc.ManagedClasses,
  EclipseCreateSrc.ManagedResources
)
