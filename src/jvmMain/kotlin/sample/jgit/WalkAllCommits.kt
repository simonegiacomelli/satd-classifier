package sample.jgit

/*
   Copyright 2016 Dominik Stadler

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

import org.dstadler.jgit.helper.CookbookHelper
import org.eclipse.jgit.revwalk.RevWalk

import java.io.IOException


/**
 * Simple snippet which shows how to use RevWalk to iterate over all commits
 * across all branches/tags/remotes in the given repository
 *
 * See the original discussion at http://stackoverflow.com/a/40803945/411846
 */
object WalkAllCommits {

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        CookbookHelper.openJGitCookbookRepository().use { repository ->
            // get a list of all known heads, tags, remotes, ...
            val allRefs = repository.refDatabase.refs

            // a RevWalk allows to walk over commits based on some filtering that is defined
            RevWalk(repository).use { revWalk ->
                for (ref in allRefs) {
                    revWalk.markStart(revWalk.parseCommit(ref.objectId))
                }
                println("Walking all commits starting with " + allRefs.size + " refs: " + allRefs)
                var count = 0
                for (commit in revWalk) {
                    println("Commit: $commit")
                    count++
                }
                println("Had $count commits")
            }
        }
    }
}
