/*
 * Copyright (C) 2021 The ORT Project Authors (see <https://github.com/oss-review-toolkit/ort/blob/main/NOTICE>)
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

package org.ossreviewtoolkit.scanner.provenance

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

import java.io.IOException

import org.ossreviewtoolkit.model.ArtifactProvenance
import org.ossreviewtoolkit.model.Hash
import org.ossreviewtoolkit.model.Identifier
import org.ossreviewtoolkit.model.Package
import org.ossreviewtoolkit.model.RemoteArtifact
import org.ossreviewtoolkit.model.RepositoryProvenance
import org.ossreviewtoolkit.model.SourceCodeOrigin
import org.ossreviewtoolkit.model.VcsInfo
import org.ossreviewtoolkit.model.VcsType
import org.ossreviewtoolkit.scanner.utils.DefaultWorkingTreeCache

class DefaultPackageProvenanceResolverFunTest : WordSpec() {
    private val workingTreeCache = DefaultWorkingTreeCache()
    private val resolver = DefaultPackageProvenanceResolver(DummyProvenanceStorage(), workingTreeCache)

    private val sourceArtifactUrl =
        "https://github.com/oss-review-toolkit/ort-test-data-npm/blob/test-1.0.0/README.md"
    private val repositoryUrl = "https://github.com/oss-review-toolkit/ort-test-data-npm"

    override suspend fun afterSpec(spec: Spec) {
        workingTreeCache.shutdown()
    }

    init {
        "Resolving an artifact provenance" should {
            "succeed if the artifact exists" {
                val pkg = Package.EMPTY.copy(
                    sourceArtifact = RemoteArtifact(
                        url = sourceArtifactUrl,
                        hash = Hash.NONE
                    )
                )

                resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.ARTIFACT)) shouldBe
                        ArtifactProvenance(pkg.sourceArtifact)
            }

            "fail if the artifact does not exist" {
                val pkg = Package.EMPTY.copy(
                    sourceArtifact = RemoteArtifact(
                        url = "$sourceArtifactUrl.invalid",
                        hash = Hash.NONE
                    )
                )

                shouldThrow<IOException> { resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.ARTIFACT)) }
            }
        }

        "Resolving a repository provenance" should {
            "succeed if the revision is correct" {
                val pkg = Package.EMPTY.copy(
                    vcsProcessed = VcsInfo(
                        type = VcsType.GIT,
                        url = repositoryUrl,
                        revision = "ad0367b7b9920144a47b8d30cc0c84cea102b821"
                    )
                )

                resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.VCS)) shouldBe
                        RepositoryProvenance(
                            vcsInfo = pkg.vcsProcessed,
                            resolvedRevision = "ad0367b7b9920144a47b8d30cc0c84cea102b821"
                        )
            }

            "resolve a tag name to a commit" {
                val pkg = Package.EMPTY.copy(
                    vcsProcessed = VcsInfo(
                        type = VcsType.GIT,
                        url = repositoryUrl,
                        revision = "test-1.0.0"
                    )
                )

                resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.VCS)) shouldBe
                        RepositoryProvenance(
                            vcsInfo = pkg.vcsProcessed,
                            resolvedRevision = "ad0367b7b9920144a47b8d30cc0c84cea102b821"
                        )
            }

            "fail if the revision does not exist" {
                val pkg = Package.EMPTY.copy(
                    vcsProcessed = VcsInfo(
                        type = VcsType.GIT,
                        url = repositoryUrl,
                        revision = "non-existing-revision"
                    )
                )

                shouldThrow<IOException> { resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.VCS)) }
            }

            "guess the correct tag for a package" {
                val pkg = Package.EMPTY.copy(
                    id = Identifier("NPM::test:1.0.0"),
                    vcsProcessed = VcsInfo(
                        type = VcsType.GIT,
                        url = repositoryUrl,
                        revision = ""
                    )
                )

                resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.VCS)) shouldBe
                        RepositoryProvenance(
                            vcsInfo = pkg.vcsProcessed,
                            resolvedRevision = "ad0367b7b9920144a47b8d30cc0c84cea102b821"
                        )
            }

            "work for Subversion tags" {
                val pkg = Package.EMPTY.copy(
                    id = Identifier("Maven:xml-apis:xml-apis:1.3.03"),
                    vcsProcessed = VcsInfo(
                        type = VcsType.SUBVERSION,
                        url = "https://svn.apache.org/repos/asf/xerces/xml-commons",
                        revision = "tags/xml-commons-external-1_3_03"
                    )
                )

                resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.VCS)) shouldBe
                        RepositoryProvenance(
                            vcsInfo = pkg.vcsProcessed,
                            resolvedRevision = "380970"
                        )
            }
        }

        "Resolving the provenance for multiple origins" should {
            "respect the source code origin priority" {
                val pkg = Package.EMPTY.copy(
                    sourceArtifact = RemoteArtifact(
                        url = sourceArtifactUrl,
                        hash = Hash.NONE
                    ),
                    vcsProcessed = VcsInfo(
                        type = VcsType.GIT,
                        url = repositoryUrl,
                        revision = "ad0367b7b9920144a47b8d30cc0c84cea102b821"
                    )
                )

                resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.ARTIFACT, SourceCodeOrigin.VCS)) shouldBe
                        ArtifactProvenance(pkg.sourceArtifact)

                resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.VCS, SourceCodeOrigin.ARTIFACT)) shouldBe
                        RepositoryProvenance(
                            vcsInfo = pkg.vcsProcessed,
                            resolvedRevision = "ad0367b7b9920144a47b8d30cc0c84cea102b821"
                        )
            }

            "try all provided source code origins" {
                val pkg = Package.EMPTY.copy(
                    sourceArtifact = RemoteArtifact(
                        url = sourceArtifactUrl,
                        hash = Hash.NONE
                    )
                )

                resolver.resolveProvenance(pkg, listOf(SourceCodeOrigin.VCS, SourceCodeOrigin.ARTIFACT)) shouldBe
                        ArtifactProvenance(pkg.sourceArtifact)
            }
        }
    }
}

internal class DummyProvenanceStorage : PackageProvenanceStorage {
    override fun readProvenance(id: Identifier, sourceArtifact: RemoteArtifact): PackageProvenanceResolutionResult? =
        null

    override fun readProvenance(id: Identifier, vcs: VcsInfo): PackageProvenanceResolutionResult? = null

    override fun readProvenances(id: Identifier): List<PackageProvenanceResolutionResult> = emptyList()

    override fun putProvenance(id: Identifier, vcs: VcsInfo, result: PackageProvenanceResolutionResult) { /** no-op */ }

    override fun putProvenance(
        id: Identifier,
        sourceArtifact: RemoteArtifact,
        result: PackageProvenanceResolutionResult
    ) { /** no-op */ }
}
