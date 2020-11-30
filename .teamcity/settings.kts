import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.2"

data class MicroserviceProject(val projectName: String, val vcsUrl: String, val deploy: Boolean)

val projects = ArrayList<MicroserviceProject>()
projects.add(MicroserviceProject("Movies Microservice","https://github.com/DanielGallo/microservice-movies", true))
projects.add(MicroserviceProject("Users Microservice","https://github.com/DanielGallo/microservice-users", false))

fun generateProject(proj: MicroserviceProject): Project {
    val serviceVcsRoot = generateVcsRoot(proj)

    return Project {
        id(proj.vcsUrl.toId())
        name = proj.projectName

        vcsRoot(serviceVcsRoot)

        buildType(BuildType {
            name = "Build"

            vcs {
                root(serviceVcsRoot)
            }

            artifactRules = ". => %teamcity.project.id%.zip"

            steps {
                script {
                    name = "Run Build"
                    scriptContent = """
                        npm install
                    """.trimIndent()
                }
            }
        })
    }
}

fun generateVcsRoot(proj: MicroserviceProject): GitVcsRoot {
    return GitVcsRoot{
        name = proj.projectName
        url = proj.vcsUrl
        branch = "refs/heads/main"
    }
}

project {
    for (proj in projects) {
        subProject(generateProject(proj))
    }
}
