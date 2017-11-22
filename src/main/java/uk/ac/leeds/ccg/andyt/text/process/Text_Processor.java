/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.text.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.andyt.generic.core.Generic_Strings;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_StaticIO;
import uk.ac.leeds.ccg.andyt.generic.lang.Generic_StaticString;
import uk.ac.leeds.ccg.andyt.text.io.Text_Files;

/**
 *
 * @author geoagdt
 */
public class Text_Processor {
     
    public static void main (String[] args) {
        new Text_Processor().run();
    }
    
    Text_Files Files;
        
    public void run() {
        String dataDirName;
        dataDirName = System.getProperty("user.dir") + "/data";
        Files = new Text_Files(dataDirName);
        File inputDir;
        inputDir = new File(
                Files.getLexisNexisInputDataDir(),
                "LexisNexis-20171122T195223Z-001/LexisNexis");
            System.out.println(inputDir);
        File[] inputs0;
        File[] inputs1;
            inputs0 = inputDir.listFiles();
        for (File input0 : inputs0) {
            System.out.println(input0);
            inputs1 = input0.listFiles();
            for (File input1 : inputs1) {
                //System.out.println(input1);
                if (input1.getName().endsWith("htm")){
                    System.out.println(input1);
                    parseHTML(input1);
                }
            }
        }
    }
    
    void parseHTML(File input) {
        BufferedReader br;
        br = Generic_StaticIO.getBufferedReader(input);
        String line = null;
        boolean read = false;
        while (!read) {
            try {
                line = br.readLine();
            } catch (IOException ex) {
                Logger.getLogger(Text_Processor.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (line == null) {
                read = true;
            } else {
                //System.out.println(line);
                LocalDate date;
                date = parseExpressLine(line);
                if (date != null) {
                    System.out.println(date);
                }
            }
        }
    }
    
    LocalDate parseExpressLine(String line) {
        LocalDate result = null;
        String stringDate = "";
        if (line.startsWith("<br><div class=\"c3\"><p class=\"c1\"><span class=\"c4\">")) {
            String month;
            String dayOfMonth;
            String year;
            String dayOfWeek;
            String s;
            s = line.replaceAll("<br><div class=\"c3\"><p class=\"c1\"><span class=\"c4\">", "");
            String[] split;
            split = s.split("</span><span class=\"c2\"> ");
            month = split[0];
            String[] split2;
            split2 = split[1].replace("</span><span", "").trim().split(",");
            dayOfMonth = split2[0];
            String[] split3;
            split3 = split2[1].trim().split(" ");
            year = split3[0];
            dayOfWeek = split3[1];
            stringDate += "year " + year;
            stringDate += " month " + month;
            stringDate += " dayOfMonth " + dayOfMonth;
            stringDate += " dayOfWeek " + dayOfWeek;
            //System.out.println(stringDate);
            Month m = Month.valueOf(Generic_StaticString.getUpperCase(month));
            result = LocalDate.of(Integer.valueOf(year), m, Integer.valueOf(dayOfMonth));
            //System.out.println(LD.toString());
       }
        return result;
    }

}