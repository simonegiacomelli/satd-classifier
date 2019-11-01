package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import satd.step1.Folders
import satd.utils.AntiSpin
import satd.utils.Rate
import satd.utils.pathSize
import satd.utils.printStats
import java.nio.charset.Charset

/**
 * Tracker of SATD across the repository history
 */
class Tracker(val repo: Repository) {
    val blobs = mutableMapOf<ObjectId, Blob>()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
//            val gp = gp1()
//            gp.rebuild()
//            val git = gp.git
//            val git = Git.open(Folders.repos.resolve("square_retrofit").toFile())
            val git = Git.open(Folders.repos.resolve("google_guava").toFile())
            git.printStats()
//            val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
            Tracker(git.repository).walk()
        }
    }

    val commitRate = Rate(10)
    val blobRate = Rate(10)
    val satdRate = Rate(10)
    val ratePrinter =
        AntiSpin { "commits/sec:$commitRate blob/sec:$blobRate satd/sec: $satdRate" }


    fun walk() {
        commitRate.reset()
        blobRate.reset()
        val walk = CRevWalk(repo)
        walk.all()

        for (commit in walk.call())
            findSatd(commit)

    }

    private fun findSatd(commit: CRevCommit) {
        commitRate.spin()
        commit.addReverseEdges()
        val treeWalk = TreeWalk(repo)
        treeWalk.addTree(commit.tree)
        treeWalk.filter = PathSuffixFilter.create(".java")
        treeWalk.isRecursive = true
        while (treeWalk.next()) {
            val objectId = treeWalk.getObjectId(0)!!

            val blob = blobs.getOrPut(objectId) {
                blobRate.spin()
                val content = repo.open(objectId).bytes.toString(Charset.forName("UTF-8"))
                val blob = Blob(objectId, content)
                if (blob.satdList.isNotEmpty())
                    satdRate.spin()
                blob
            }

            if (blob.satdList.isNotEmpty())
                commit.blobWithSatd.add(blob)

            ratePrinter.spin()
        }
    }

}
