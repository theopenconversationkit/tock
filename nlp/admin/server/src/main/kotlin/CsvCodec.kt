/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.admin

import ai.tock.shared.property
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

/**
 * Csv utilities.
 */
object CsvCodec {
    private val s = property("tock_csv_delimiter", ";")

    fun csvFormat(): CSVFormat = CSVFormat.DEFAULT.withDelimiter(s[0]).withTrim(true)

    fun newPrinter(sb: StringBuilder): CSVPrinter = CSVPrinter(sb, csvFormat())
}
