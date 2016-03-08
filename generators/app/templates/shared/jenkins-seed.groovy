import javaposse.jobdsl.dsl.helpers.step.StepContext
import javaposse.jobdsl.dsl.Context
import javaposse.jobdsl.dsl.helpers.*

class DockerisedService {

    String name
    String gitRepoUrl
    String dockerRepoUrl
    Boolean requiresMysql

    public DockerisedService(name, gitRepoUrl, dockerRepoUrl, requiresMysql) {
        this.name = name
        this.gitRepoUrl = gitRepoUrl
        this.requiresMysql = requiresMysql
        this.dockerRepoUrl = dockerRepoUrl
    }
}

DockerisedService dockerisedService = new DockerisedService(
    <%= props.projectName %>, <%= props.gitRepoUrl %>,
    <%= props.dockerRepoUrl %>, <%= props.testsRequireMysql %>
)

/**
 *
 * Creates the job in jenkins
 *
 * @param isReleaseJob If true it will build and push the docker image
 *
 */
def createJob(service, isReleaseJob) {
    jobName = "${service-name}-ci"
    if (isReleaseJob) jobName += "-release"

    job(jobName) {
        logRotator(35,35)

        label('build-nodes')

        parameters {
            stringParam('GIT_BRANCH', 'master', 'Can be SHA, Tag or Branch')
            stringParam('DOCKER_IMAGE_TAG', 'latest', 'The docker image will be tagged using this tag')
        }

        scm {
            git {
                remote {
                    url(service.gitRepoUrl)
                }
                branch("$\GIT_BRANCH")
            }
        }

        concurrentBuild(true)

        wrappers {
            buildName('#${BUILD_NUMBER} - ${ENV,var="GitBranch"}')
        }

        steps {
            if (service.requiresMysql) startMysqlStep()

            if (isReleaseJob) {
                sbtCommandStep('release')
                dockerBuildAndPushStep(service)
            } else {
                sbtCommandStep('test')
            }
        }

        publishers {
            publishTestReport()
        }
    }
}

StepContext.metaClass.startMysql = {
  shell("""#!/bin/bash
      |sudo mysqladmin shutdown
      |sudo rm -rf /var/lib/mysql/*
      |sudo mysql_install_db
      |sudo service mysql start
      |mysql_tzinfo_to_sql /usr/share/zoneinfo | mysql -u root mysql
      |sudo rm -f flyway.properties
      |""".stripMargin()
       )
}

StepContext.metaClass.sbtCommandStep = { command ->
    shell("""#!/bin/bash
        |set -e
        |
        |sbt $command
        |
        |""".stripMargin()
    )
}

StepContext.metaClass.dockerBuildAndPushStep = { service ->
    shell("""#!/bin/bash
        |set -e
        |
        |DOCKER_PUSH_PATH=${service.dockerRepoUrl}${service.name}:\$DOCKER_IMAGE_TAG
        |
        |./docker-build.sh \$DOCKER_IMAGE_TAG
        |
        |docker push $DOCKER_PUSH_PATH
        |
        |echo "Pushed docker image to $DOCKER_PUSH_PATH"
        |""".stripMargin()
    )
}


