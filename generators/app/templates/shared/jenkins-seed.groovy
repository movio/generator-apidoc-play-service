import javaposse.jobdsl.dsl.helpers.step.StepContext
import javaposse.jobdsl.dsl.Context
import javaposse.jobdsl.dsl.helpers.*

class DockerisedService {

    String name
    String gitCloneUrl
    String dockerRepoUrl
    String sbtSubDir
    Boolean requiresMysql

    public DockerisedService(name, gitCloneUrl, dockerRepoUrl, requiresMysql, sbtSubDir = '.') {
        this.name = name
        this.gitCloneUrl = gitCloneUrl
        this.dockerRepoUrl = dockerRepoUrl
        this.requiresMysql = requiresMysql
        this.sbtSubDir = sbtSubDir
    }
}

DockerisedService dockerisedService = new DockerisedService(
    '<%= props.appName %>', '<%= props.gitCloneUrl %>',
    '<%= props.dockerRepoUrl %>', <%= props.testsRequireMysql %>
)

createJob(dockerisedService, false)
createJob(dockerisedService, true)

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
            stringParam('GIT_BRANCH', 'master', 'Must be a git branch or tag')
            stringParam('TAG_VERSION', 'latest', 'The docker image will be tagged using this tag')
        }

        scm {
            git {
                remote {
                    url(service.gitCloneUrl)
                }
                branch("\$GIT_BRANCH")
            }
        }

        concurrentBuild(true)

        wrappers {
            buildName('#${BUILD_NUMBER} - ${ENV,var="GitBranch"}')
        }

        steps {
            gitFreshCheckoutBranch()
            if (service.requiresMysql) startMysqlStep()

            if (isReleaseJob) {
                sbtCommandStep('release-with-defaults', service.sbtSubDir, ['release_version':\${TAG_VERSION}])
                sbtCommandStep('stage', service.sbtSubdir)
                dockerBuildAndPushStep(service)
            } else {
                sbtCommandStep('test', sbtSubDir)
            }
        }

        publishers {
            publishTestReport()
        }
    }
}

// For the release plugin to work, we need to checkout the branch
// not a detached head that the git plugin does by default.
// This should be included in release
StepContext.metaClass.gitFreshCheckoutBranch = {
  shell("""#!/bin/bash
    |set -e
    |
    |# We need to do this again so that the release process can push changes to git repo
    |git fetch
    |git checkout \${GIT_BRANCH}
    |""".stripMargin()
  )
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

StepContext.metaClass.sbtCommandStep = { command, subdir, options = [:] ->
    options.put('sbt.log.noformat', true)
    sbtOptions = options.colect { key, value ->
        '-D' + key + '=' + value
    }.join(' ')
    sbt(
      'current',
      sbtCommand,
      sbtOptions,
      '-Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=384M',
      subdir,
      null
  )
}

StepContext.metaClass.dockerBuildAndPushStep = { service ->
    shell("""#!/bin/bash
        |set -e
        |
        |DOCKER_PUSH_PATH=${service.dockerRepoUrl}${service.name}:\$TAG_VERSION
        |
        |./docker-build.sh \$TAG_VERSION
        |
        |docker push $DOCKER_PUSH_PATH
        |
        |echo "Pushed docker image to $DOCKER_PUSH_PATH"
        |""".stripMargin()
    )
}

// Seed job that runs this file
job("<%=props.appName %>-seed") {
  logRotator(10,10)
  label('master')
  parameters {
    stringParam('GIT_COMMIT', 'master', 'Can be a git commit, tag or branch')
  }
  scm {
    git {
      remote {
        url("<%= props.gitCloneUrl %>")
      }
      branch('\$GIT_COMMIT')
    }
  }
  steps {
    dsl {
      external('jenkins-seed.groovy')
      removeAction('DISABLE')
    }
  }
}

// VIEWS
listView(<%= props.appName %>) {
    description('Release and CI jobs for <%= props.appName %>')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex('<%= props.appName %>*')
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

// VIEWS
listView('seed-jobs') {
    description('Seed jobs')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex('*seed*')
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
