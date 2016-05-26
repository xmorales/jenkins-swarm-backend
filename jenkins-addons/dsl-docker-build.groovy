import hudson.model.*

// get current thread / Executor
def thr = Thread.currentThread()
// get current build
def build = thr?.executable

def repo = build.buildVariableResolver.resolve("Repository")
def branch = build.buildVariableResolver.resolve("Branch")
def ram = build.buildVariableResolver.resolve("RAM")
def clusterName = build.buildVariableResolver.resolve("Cluster Name")
def builderHost = build.buildVariableResolver.resolve("Builder Host")
def registry = build.buildVariableResolver.resolve("Registry Hostname")
def auxRepoFile = "https://raw.githubusercontent.com/xmorales/jenkins-swarm-backend/master/jenkins-addons/setup-docker-template.groovy"

// Split repo into components
// git@pdihub.hi.inet:DEV/test.git
def serverRepo = repo.tokenize('@:')[1]
def componentOrg = repo.tokenize(':/')[1]
def componentParse = /\/.*.git$/
def componentName = ( repo =~ /$componentParse/)[0][1..-5]

freeStyleJob("_docker-slave-builder") {
  description("<h3>Build and configure a docker container for ${componentName}.</h3>")
  scm {
    github("${componentOrg}/${componentName}",branch,"ssh",serverRepo)
  }
  triggers {
    githubPush()
  }
  steps {
    dockerBuildAndPublish {
      repositoryName("${clusterName}/${componentName}")
      dockerHostURI("${builderHost}")
      dockerRegistryURL("https://${registry}")
      registryCredentials('76335134-dbbb-4195-8c31-482395a5854c')
      forcePull(true)
      createFingerprints(false)
      skipDecorate()
    }

    configure{ project ->
        project / 'builders' / 'com.nirima.jenkins.plugins.docker.builder.DockerBuilderNewTemplate' {
          dockerTemplate() {
            configVersion("2")
            labelString("${clusterName}_${componentName}")
            launcher(class: "com.nirima.jenkins.plugins.docker.launcher.DockerComputerSSHLauncher") {
              sshConnector() {
                port("22")
                credentialsId('e40adca1-3abd-4db9-953d-e7d47ec87a55')
                jvmOptions()
                javaPath()
                maxNumRetries("0")
                retryWaitTime("0")
              }
            }
            remoteFsMapping()
            remoteFs('/home/contint')
            instanceCap("5")
            mode("EXCLUSIVE")  // Not working, needed to set via groovy script after
            retentionStrategy(class: "com.nirima.jenkins.plugins.docker.strategy.DockerOnceRetentionStrategy") {
              idleMinutes('5')
              idleMinutes('defined-in': "com.nirima.jenkins.plugins.docker.strategy.DockerOnceRetentionStrategy",'5')
            }
            numExecutors("1")
            dockerTemplateBase() {
              image("${registry}/${clusterName}/${componentName}")
              dockerCommand()
              lxcConfString()
              hostname()
              dnsHosts()
              volumes()
              volumesFrom2("jenkins_cache")
              environment()
              bindPorts()
              bindAllPorts('true')
              memoryLimit("${ram}")
              privileged('false')
              tty('false')
              extraHosts(class: "java.util.Collections$UnmodifiableRandomAccessList"){
                c(class: "list")
                list('reference': "../c")
              }
            }
          removeVolumes("true")
          pullStrategy("NEVER_PULL")
          }
        version("1")
        }
      }
      shell("[ -f setup-docker-template.groovy ] || curl ${auxRepoFile} -o setup-docker-template.groovy")
      systemGroovyScriptFile("setup-docker-template.groovy"){
        binding("templateName","${registry}/${clusterName}/${componentName}")
        binding("memoryLimit",ram)
      }
  }
}
