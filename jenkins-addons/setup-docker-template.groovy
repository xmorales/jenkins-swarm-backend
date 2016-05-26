import hudson.model.*
import jenkins.model.*
import com.nirima.jenkins.plugins.docker.*

// Parameters expected and an example of them
//def templateName = 'registry/clusterName/component'
//def memoryLimit = "1024"
def clusterName = templateName.tokenize('/')[1]

//Fill not obtainable parameters
def lxcConfString = ''
def hostname = ''
def bindPorts = ''
def bindAllPorts = true
def privileged = false
def tty = false
def swap = 0

def inst = Jenkins.getInstance()
def cloud = inst.getCloud(clusterName)
def templ = cloud.getTemplate(templateName)
def templBase = templ.getDockerTemplateBase();

//We want to change a final value, so we only can create a new object and remove the older
def newTemplBase = new DockerTemplateBase(
                                 templBase.getImage(),
                                 templBase.getDnsString(),
                                 templBase.getDockerCommandArray().join(" "),
                                 templBase.getVolumesString(),
                                 templBase.getVolumesFromString(),
                                 templBase.getEnvironmentsString(),
                                 lxcConfString,
                                 hostname,
                                 Integer.parseInt(memoryLimit),
                                 swap,
                                 templBase.getCpuShares(),
                                 bindPorts,
                                 bindAllPorts,
                                 privileged,
                                 tty,
                                 templBase.getMacAddress()
                                 )
templ.setDockerTemplateBase(newTemplBase)

cloud.getDescriptor().save()
