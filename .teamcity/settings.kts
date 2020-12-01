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

fun generateProject(proj: MicroserviceProject): Project {
    val serviceVcsRoot = generateVcsRoot(proj)

    val build = BuildType {
        id("build_" + proj.vcsUrl.toId())
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
    }

    val test = BuildType {
        id("test_" + proj.vcsUrl.toId())
        name = "Test"

        vcs {
            root(serviceVcsRoot)
        }

        if (proj.deploy.equals(false)) {
            triggers {
                vcs {
                }
            }
        }

        steps {
            script {
                name = "NPM Install"
                scriptContent = """
                        npm install
                    """.trimIndent()
            }
            script {
                name = "Run Tests"
                scriptContent = """
                        ./node_modules/mocha/bin/mocha test --reporter mocha-teamcity-reporter
                    """.trimIndent()
            }
        }
    }

    val deploy = BuildType {
        id("deploy_" + proj.vcsUrl.toId())
        name = "Deploy"
        type = BuildTypeSettings.Type.DEPLOYMENT

        vcs {
            root(serviceVcsRoot)
        }

        triggers {
            vcs {
            }
        }

        steps {
            script {
                name = "Run Deployment"
                scriptContent = """
                            echo "Deploying!"
                        """.trimIndent()
            }
        }
    }

    return Project {
        id(proj.vcsUrl.toId())
        name = proj.projectName

        vcsRoot(serviceVcsRoot)

        buildType(build)
        buildType(test)

        if (proj.deploy.equals(true)) {
            buildType(deploy)

            sequential {
                buildType(build)
                buildType(test)
                buildType(deploy)
            }

            buildTypesOrder = arrayListOf(build, test, deploy)
        } else {
            sequential {
                buildType(build)
                buildType(test)
            }

            buildTypesOrder = arrayListOf(build, test)
        }
    }
}

fun generateVcsRoot(proj: MicroserviceProject): GitVcsRoot {
    val vcsId = "vcs_" + proj.vcsUrl

    return GitVcsRoot{
        id(vcsId.toId())
        name = proj.projectName
        url = proj.vcsUrl
        branch = "refs/heads/main"
    }
}

project {
    val projects = microserviceProjects()

    for (proj in projects) {
        subProject(generateProject(proj))
    }
}
