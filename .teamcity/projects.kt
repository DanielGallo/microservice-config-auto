data class MicroserviceProject(val projectName: String, val vcsUrl: String, val deploy: Boolean)

fun MicroserviceProjects(): ArrayList<MicroserviceProject> {
    val projects = ArrayList<MicroserviceProject>()

    //projects.add(MicroserviceProject("Movies Microservice", "https://github.com/DanielGallo/microservice-movies", true))
    projects.add(MicroserviceProject("Users Microservice", "https://github.com/DanielGallo/microservice-users", false))

    return projects
}
