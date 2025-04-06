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
  javaJpa,
  filters,
  evolutions,
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.auth0" % "java-jwt" % "3.18.2",
  "commons-codec" % "commons-codec" % "1.15",
  "org.jsoup" % "jsoup" % "1.13.1" // Añade la dependencia de Jsoup
)

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
