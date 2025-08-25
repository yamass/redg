/*
 * Copyright Yann Massard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.yamass.redg.runtime;

/**
 * Exception indicating that an entity that was presumed to be in the database could not be found. The message should contain detailed information.
 */
public class ExistingEntryMissingException extends RuntimeException {
    public ExistingEntryMissingException() {
        super();
    }

    public ExistingEntryMissingException(final String message) {
        super(message);
    }

    public ExistingEntryMissingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ExistingEntryMissingException(final Throwable cause) {
        super(cause);
    }

    protected ExistingEntryMissingException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
