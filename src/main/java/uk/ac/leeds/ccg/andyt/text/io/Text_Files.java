/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.text.io;

import java.io.File;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_Files;

/**
 *
 * @author geoagdt
 */
public class Text_Files extends Generic_Files {

    private File LexisNexisInputDataDir;

    protected Text_Files() {}
        
    public Text_Files(String dataDirName){
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
