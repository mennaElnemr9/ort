/*
 * Copyright (C) 2017 The ORT Project Authors (see <https://github.com/oss-review-toolkit/ort/blob/main/NOTICE>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package org.ossreviewtoolkit.analyzer.managers

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

import org.ossreviewtoolkit.downloader.VersionControlSystem
import org.ossreviewtoolkit.model.config.AnalyzerConfiguration
import org.ossreviewtoolkit.model.config.RepositoryConfiguration
import org.ossreviewtoolkit.utils.ort.normalizeVcsUrl
import org.ossreviewtoolkit.utils.test.USER_DIR
import org.ossreviewtoolkit.utils.test.getAssetFile
import org.ossreviewtoolkit.utils.test.patchActualResult
import org.ossreviewtoolkit.utils.test.patchExpectedResult

class BabelFunTest : WordSpec({
    val projectDir = getAssetFile("projects/synthetic/npm-babel").absoluteFile
    val vcsDir = VersionControlSystem.forDirectory(projectDir)!!
    val vcsUrl = vcsDir.getRemoteUrl()
    val vcsRevision = vcsDir.getRevision()

    "Babel dependencies" should {
        "be correctly analyzed" {
            val definitionFile = projectDir.resolve("package.json")

            val expectedResult = patchExpectedResult(
                projectDir.resolveSibling("npm-babel-expected-output.yml"),
                url = normalizeVcsUrl(vcsUrl),
                revision = vcsRevision
            )
            val actualResult = createNPM().resolveSingleProject(definitionFile, resolveScopes = true)

            patchActualResult(actualResult.toYaml()) shouldBe expectedResult
        }
    }
})

private fun createNPM() = Npm("NPM", USER_DIR, AnalyzerConfiguration(), RepositoryConfiguration())
