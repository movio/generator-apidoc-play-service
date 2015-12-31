import sbt._

object Dependencies {
  val apidocDependencies = Set(
    "<%= props.apidocOrg %>" %% "<%= props.apidocApp %>-kafkalib_0_8" % "<%= props.apidocVersion %>",
    "<%= props.apidocOrg %>" %% "<%= props.apidocApp %>-playlib" % "<%= props.apidocVersion %>"
  )
}
