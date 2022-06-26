package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val tasks = repos.map { repo ->
        async {
            val responseUsers = service.getRepoContributors(req.org, repo.name)
            logUsers(repo, responseUsers)
            return@async responseUsers.bodyList()
        }
    }

    return@coroutineScope tasks.awaitAll().flatten().aggregate()
}