/*
 * Part of a library developed for text data processing tasks.
 * Copyright 2017 Andy Turner, University of Leeds.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leeds.ccg.andyt.text.io;

import java.io.File;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_Files;

/**
 * This class if for managing files for the library.
 */
public class Text_Files extends Generic_Files {

    private File LexisNexisInputDataDir;

    protected Text_Files() {
    }

    public Text_Files(String dataDirName) {
        super(dataDirName);
    }

    /**
     * @return the LexisNexisInputDataDir
     */
    public File getLexisNexisInputDataDir() {
        if (LexisNexisInputDataDir == null) {
            LexisNexisInputDataDir = new File(
                    getInputDataDir(), "LexisNexis");
        }
        return LexisNexisInputDataDir;
    }

}
