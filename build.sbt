name := "ingestionframework"

version := "0.1"

scalaVersion := "2.10.6"

val json4SVer = "3.2.11"

libraryDependencies ++= Seq(

   // spark
  "org.apache.spark" %% "spark-core" % "1.6.1",
  "org.apache.spark" %% "spark-sql" % "1.6.0",
  "com.databricks" %% "spark-avro" % "0.1",

  // java libs
  "joda-time" % "joda-time" % "2.7",

  // For JSON parsing (see https://github.com/json4s/json4s)
  "org.json4s" %%  "json4s-jackson" % json4SVer,
  "org.json4s" %%  "json4s-ext" % json4SVer,
  
  // For testing:
  "org.scalactic" %% "scalactic" % "2.2.6",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

