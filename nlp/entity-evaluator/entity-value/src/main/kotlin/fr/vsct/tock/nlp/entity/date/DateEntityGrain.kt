/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.entity.date

import java.lang.Exception

/**
 *
 */
enum class DateEntityGrain(val time: Boolean) {

    //the  order is important
    timezone(false),
    unknown(false),
    second(true),
    minute(true),
    hour(true),
    day_of_week(false),
    day(false),
    week(false),
    month(false),
    quarter(false),
    year(false);

    fun from(s: String?): DateEntityGrain {
        try {
            return if (s == null) unknown else valueOf(s)
        } catch (e: Exception) {
            return unknown
        }

    }

}