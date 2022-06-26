package tasks

import kotlinx.coroutines.launch
import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    val responseRepos = service.getOrgRepos(req.org)
    logRepos(req, responseRepos)

    val repos = responseRepos.bodyList()
    val allUsers = mutableListOf<User>()

    val jobs = repos.map { repo ->
        GlobalScope.launch {
            val responseUsers = service.getRepoContributors(req.org, repo.name)
            logUsers(repo, responseUsers)
            val users = responseUsers.bodyList()
            allUsers += users
        }
    }
    jobs.joinAll()

    return allUsers.aggregate()
}