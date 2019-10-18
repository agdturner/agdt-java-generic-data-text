/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.text.core;

import java.io.IOException;
import uk.ac.leeds.ccg.andyt.generic.core.Generic_Environment;

/**
 *
 * @author geoagdt
 */
public class Text_Environment  {
    
    public transient Generic_Environment env;
    
    public Text_Environment() throws IOException {
        env = new Generic_Environment();
    }
    
}
