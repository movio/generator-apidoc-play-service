import sbt._

object Dependencies {
  val apidocDependencies = Set(
    "<%= props.apidocOrg %>" %% "<%= props.apidocApp %>-kafkalib_0_8" % "<%= props.apidocVersion %>" excludeAll (
      ExclusionRule("org.slf4j", "slf4j-log4j12")
    ),
    "<%= props.apidocOrg %>" %% "<%= props.apidocApp %>-playlib" % "<%= props.apidocVersion %>" excludeAll (
      ExclusionRule("org.slf4j", "slf4j-log4j12")
    ),
  )
}
