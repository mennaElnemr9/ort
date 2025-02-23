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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

import org.ossreviewtoolkit.downloader.VersionControlSystem
import org.ossreviewtoolkit.model.config.AnalyzerConfiguration
import org.ossreviewtoolkit.model.config.RepositoryConfiguration
import org.ossreviewtoolkit.utils.ort.normalizeVcsUrl
import org.ossreviewtoolkit.utils.test.USER_DIR
import org.ossreviewtoolkit.utils.test.getAssetFile
import org.ossreviewtoolkit.utils.test.patchExpectedResult

class GradleKotlinScriptFunTest : StringSpec() {
    private val projectDir = getAssetFile("projects/synthetic/multi-kotlin-project").absoluteFile
    private val vcsDir = VersionControlSystem.forDirectory(projectDir)!!
    private val vcsUrl = vcsDir.getRemoteUrl()
    private val vcsRevision = vcsDir.getRevision()

    init {
        "root project dependencies are detected correctly" {
            val definitionFile = projectDir.resolve("build.gradle.kts")
            val expectedResult = patchExpectedResult(
                projectDir.resolveSibling("multi-kotlin-project-expected-output-root.yml"),
                url = normalizeVcsUrl(vcsUrl),
                revision = vcsRevision
            )

            val result = createGradle().resolveSingleProject(definitionFile, resolveScopes = true)

            result.toYaml() shouldBe expectedResult
        }

        "core project dependencies are detected correctly" {
            val definitionFile = projectDir.resolve("core/build.gradle.kts")
            val expectedResult = patchExpectedResult(
                projectDir.resolveSibling("multi-kotlin-project-expected-output-core.yml"),
                url = normalizeVcsUrl(vcsUrl),
                revision = vcsRevision
            )

            val result = createGradle().resolveSingleProject(definitionFile, resolveScopes = true)

            result.toYaml() shouldBe expectedResult
        }

        "cli project dependencies are detected correctly" {
            val definitionFile = projectDir.resolve("cli/build.gradle.kts")
            val expectedResult = patchExpectedResult(
                projectDir.resolveSibling("multi-kotlin-project-expected-output-cli.yml"),
                url = normalizeVcsUrl(vcsUrl),
                revision = vcsRevision
            )

            val result = createGradle().resolveSingleProject(definitionFile, resolveScopes = true)

            result.toYaml() shouldBe expectedResult
        }
    }

    private fun createGradle() =
        Gradle("Gradle", USER_DIR, AnalyzerConfiguration(), RepositoryConfiguration())
}
