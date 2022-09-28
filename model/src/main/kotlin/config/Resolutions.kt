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

package org.ossreviewtoolkit.model.config

import com.fasterxml.jackson.annotation.JsonInclude

fun Resolutions?.orEmpty() = this ?: Resolutions()

/**
 * Resolutions for issues with a repository.
 */
data class Resolutions(
    /**
     * Resolutions for issues with the analysis or scan of the projects in this repository and their dependencies.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val issues: List<IssueResolution> = emptyList(),

    /**
     * Resolutions for license policy violations.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val ruleViolations: List<RuleViolationResolution> = emptyList(),

    /**
     * Resolutions for vulnerabilities provided by the advisor.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val vulnerabilities: List<VulnerabilityResolution> = emptyList()
) {
    /**
     * Merge this [Resolutions] with [other] [Resolutions]. Duplicates are removed.
     */
    fun merge(other: Resolutions) =
        Resolutions(
            issues = (issues + other.issues).distinct(),
            ruleViolations = (ruleViolations + other.ruleViolations).distinct(),
            vulnerabilities = (vulnerabilities + other.vulnerabilities).distinct()
        )
}
