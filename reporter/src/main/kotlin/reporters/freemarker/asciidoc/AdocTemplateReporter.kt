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

package org.ossreviewtoolkit.reporter.reporters.freemarker.asciidoc

import java.io.File

import org.asciidoctor.Attributes

import org.ossreviewtoolkit.reporter.Reporter
import org.ossreviewtoolkit.reporter.ReporterInput

/**
 * A [Reporter] that uses [Apache Freemarker][1] templates to create AsciiDoc[2] files.
 *
 * [1]: https://freemarker.apache.org
 * [2]: https://asciidoc.org
 */
class AdocTemplateReporter : AsciiDocTemplateReporter("adoc", "AdocTemplate") {
    override fun processAsciiDocFiles(
        input: ReporterInput,
        outputDir: File,
        asciiDocFiles: List<File>,
        asciidoctorAttributes: Attributes
    ) = asciiDocFiles.map {
        it.copyTo(
            target = outputDir.resolve(it.name),
            overwrite = input.ortConfig.forceOverwrite
        )
    }
}
