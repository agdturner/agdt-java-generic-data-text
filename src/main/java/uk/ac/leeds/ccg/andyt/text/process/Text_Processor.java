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
package uk.ac.leeds.ccg.andyt.text.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_StaticIO;
import uk.ac.leeds.ccg.andyt.generic.lang.Generic_StaticString;
import uk.ac.leeds.ccg.andyt.text.io.Text_Files;

/**
 * This class has a main method and so can be run. It was developed originally
 * for processing a set of files downloaded from NexisLexis for an undergraduate
 * student dissertation project. The NexisLexis files are in HTML format and
 * comprise the text of sets of articles downloaded from
 * http://www.nexislexis.com from the Express and Guardian newspapers from the
 * 1st of April 2015 to the 10th of February 2016. The search criteria for the
 * articles included the words "refugee" or "brexit" or both.
 */
public class Text_Processor {

    public static void main(String[] args) {
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
            //System.out.println(input0);
            System.out.println(input0.getName());
            inputs1 = input0.listFiles();
            int grandTotalRefugeeWordCount = 0;
            int grandTotalSyriaWordCount = 0;
            int grandTotalBrexitWordCount = 0;
            Iterator<DayOfWeek> ite;
            DayOfWeek day;
            int i;
            TreeMap<DayOfWeek, Integer> grandTotalRefugeeWordCountOnDay = new TreeMap<>();
            TreeMap<DayOfWeek, Integer> grandTotalSyriaWordCountOnDay = new TreeMap<>();
            TreeMap<DayOfWeek, Integer> grandTotalBrexitWordCountOnDay = new TreeMap<>();
            //int grandTotalRefugeeWordCountOnSaturdays = 0;
            //int grandTotalSyriaWordCountOnSaturdays = 0;
            //int grandTotalBrexitWordCountOnSaturdays = 0;
            for (File input1 : inputs1) {
                //System.out.println(input1);
                if (input1.getName().endsWith("htm")) {
                    //System.out.println(input1);
                    Object[] results = parseHTML(input1);
                    int[] totals;
                    totals = (int[]) results[0];
                    grandTotalRefugeeWordCount += totals[0];
                    grandTotalSyriaWordCount += totals[1];
                    grandTotalBrexitWordCount += totals[2];
                    // Refugee count
                    TreeMap<DayOfWeek, Integer> totalRefugeeWordCountOnDay;
                    totalRefugeeWordCountOnDay = (TreeMap<DayOfWeek, Integer>) results[1];
                    ite = totalRefugeeWordCountOnDay.keySet().iterator();
                    while (ite.hasNext()) {
                        day = ite.next();
                        if (grandTotalRefugeeWordCountOnDay.containsKey(day)) {
                            i = grandTotalRefugeeWordCountOnDay.get(day);
                            i += totalRefugeeWordCountOnDay.get(day);
                        } else {
                            i = totalRefugeeWordCountOnDay.get(day);
                        }
                        grandTotalRefugeeWordCountOnDay.put(day, i);
                    }
                    // Syria count
                    TreeMap<DayOfWeek, Integer> totalSyriaWordCountOnDay;
                    totalSyriaWordCountOnDay = (TreeMap<DayOfWeek, Integer>) results[2];
                    ite = totalSyriaWordCountOnDay.keySet().iterator();
                    while (ite.hasNext()) {
                        day = ite.next();
                        if (grandTotalSyriaWordCountOnDay.containsKey(day)) {
                            i = grandTotalSyriaWordCountOnDay.get(day);
                            i += totalSyriaWordCountOnDay.get(day);
                        } else {
                            i = totalSyriaWordCountOnDay.get(day);
                        }
                        grandTotalSyriaWordCountOnDay.put(day, i);
                    }
                    // Brexit count
                    TreeMap<DayOfWeek, Integer> totalBrexitWordCountOnDay;
                    totalBrexitWordCountOnDay = (TreeMap<DayOfWeek, Integer>) results[3];
                    ite = totalBrexitWordCountOnDay.keySet().iterator();
                    while (ite.hasNext()) {
                        day = ite.next();
                        if (grandTotalBrexitWordCountOnDay.containsKey(day)) {
                            i = grandTotalBrexitWordCountOnDay.get(day);
                            i += totalBrexitWordCountOnDay.get(day);
                        } else {
                            i = totalBrexitWordCountOnDay.get(day);
                        }
                        grandTotalBrexitWordCountOnDay.put(day, i);
                    }
                    //if () {
                    TreeSet<DateHeadline> syriaDateHeadlines;
                    syriaDateHeadlines = (TreeSet<DateHeadline>) results[4];
                    Iterator<DateHeadline> ite2;
                    DateHeadline dh;
                    ite2 = syriaDateHeadlines.iterator();
                    while (ite2.hasNext()) {
                        dh = ite2.next();
                        System.out.println(dh.LD + " " + dh.Headline);
                    }
                    //}
//                    grandTotalRefugeeWordCountOnSaturdays += totals[3];
//                    grandTotalSyriaWordCountOnSaturdays += totals[4];
//                    grandTotalBrexitWordCountOnSaturdays += totals[5];
                }
            }
            //System.out.println(input0.getName());
            System.out.println("refugee word count " + grandTotalRefugeeWordCount);
            System.out.println("syria word count " + grandTotalSyriaWordCount);
            System.out.println("brexit word count " + grandTotalBrexitWordCount);
            // Refugee
            ite = grandTotalRefugeeWordCountOnDay.keySet().iterator();
            while (ite.hasNext()) {
                day = ite.next();
                i = grandTotalRefugeeWordCountOnDay.get(day);
                System.out.println("Refugee word count on " + day + " " + i);
            }
            // Syria
            ite = grandTotalSyriaWordCountOnDay.keySet().iterator();
            while (ite.hasNext()) {
                day = ite.next();
                i = grandTotalSyriaWordCountOnDay.get(day);
                System.out.println("Syria word count on " + day + " " + i);
            }
            // Brexit
            ite = grandTotalBrexitWordCountOnDay.keySet().iterator();
            while (ite.hasNext()) {
                day = ite.next();
                i = grandTotalBrexitWordCountOnDay.get(day);
                System.out.println("Brexit word count on " + day + " " + i);
            }
            System.out.println("");
        }
    }

    Object[] parseHTML(File input) {
        Object[] result = new Object[5];
        int[] totals = new int[3];
        result[0] = totals;
        TreeSet<DateHeadline> syriaDateHeadlines;
        syriaDateHeadlines = new TreeSet<>();
        BufferedReader br;
        br = Generic_StaticIO.getBufferedReader(input);
        String line = null;
        boolean read = false;
        int totalRefugeeWordCount = 0;
        int totalSyriaWordCount = 0;
        int totalBrexitWordCount = 0;
        TreeMap<DayOfWeek, Integer> totalRefugeeWordCountByDay = new TreeMap<>();
        TreeMap<DayOfWeek, Integer> totalSyriaWordCountByDay = new TreeMap<>();
        TreeMap<DayOfWeek, Integer> totalBrexitWordCountByDay = new TreeMap<>();
        int refugeeWordCount = 0;
        int syriaWordCount = 0;
        int brexitWordCount = 0;
        LocalDate oldDate = null;
        boolean gotFirstDate = false;
        boolean gotTitle = false;
        String title = null;
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
                if (!gotFirstDate) {
                    oldDate = parseDate(line);
                    if (oldDate != null) {
                        gotFirstDate = true;
                    }
                } else {
                    date = parseDate(line);
                    if (date == null) {
                        if (!gotTitle) {
                            title = parseTitle(line);
                            if (!title.isEmpty()) {
                                gotTitle = true;
                                //System.out.println(title);
                            }
                        }
                        // Add to word counts
                        refugeeWordCount += getWordCount("refugee", line);
                        syriaWordCount += getWordCount("syria", line);
                        brexitWordCount += getWordCount("brexit", line);
                    } else {
                        gotTitle = false;
//                    System.out.println("refugeeWordCount " + refugeeWordCount);
//                    System.out.println("syriaWordCount " + syriaWordCount);
//                    System.out.println("brexitWordCount " + brexitWordCount);
                        totalRefugeeWordCount += refugeeWordCount;
                        totalSyriaWordCount += syriaWordCount;
                        totalBrexitWordCount += brexitWordCount;
                        DayOfWeek day;
                        day = oldDate.getDayOfWeek();
                        int i;
                        // Refugee
                        if (totalRefugeeWordCountByDay.containsKey(day)) {
                            i = totalRefugeeWordCountByDay.get(day);
                            i += refugeeWordCount;
                        } else {
                            i = refugeeWordCount;
                        }
                        totalRefugeeWordCountByDay.put(day, i);
                        // Syria
                        if (totalSyriaWordCountByDay.containsKey(day)) {
                            i = totalSyriaWordCountByDay.get(day);
                            i += syriaWordCount;
                        } else {
                            i = syriaWordCount;
                        }
                        totalSyriaWordCountByDay.put(day, i);
                        // Brexit
                        if (totalBrexitWordCountByDay.containsKey(day)) {
                            i = totalBrexitWordCountByDay.get(day);
                            i += brexitWordCount;
                        } else {
                            i = brexitWordCount;
                        }
                        totalBrexitWordCountByDay.put(day, i);
                        //System.out.println(date);
                        if (syriaWordCount > 0) {
                            if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                                syriaDateHeadlines.add(new DateHeadline(date, title));
                            }
                        }
                        refugeeWordCount = 0;
                        syriaWordCount = 0;
                        brexitWordCount = 0;
                        oldDate = date;
                    }
                }
            }
        }
        totals[0] = totalRefugeeWordCount;
        totals[1] = totalSyriaWordCount;
        totals[2] = totalBrexitWordCount;
        result[1] = totalRefugeeWordCountByDay;
        result[2] = totalSyriaWordCountByDay;
        result[3] = totalBrexitWordCountByDay;
        result[4] = syriaDateHeadlines;
        return result;
    }

    int getWordCount(String word, String line) {
        int result = 0;
        // word
        result += line.split(word).length - 1;
        // word with capital first letter
        result += line.split(Generic_StaticString.getUpperCase(word.substring(0, 1)) + word.substring(1, word.length() - 1)).length - 1;
        // word all capitals
        result += line.split(Generic_StaticString.getUpperCase(word)).length - 1;
        return result;
    }

    String parseTitle(String line) {
        String result = "";
        if (line.startsWith("<br><div class=\"c5\"><p class=\"c6\"><span class=\"c7\">")) {
            String s;
            s = line.replace("<br><div class=\"c5\"><p class=\"c6\"><span class=\"c7\">", "");
            s = s.replace("</span><span class=\"c10\">refugees</span><span class=\"c7\"></span><span class=\"c10\">refugees</span><span class=\"c7\">", "refugees");
            s = s.replace("</span><span class=\"c10\">Refugees</span><span class=\"c7\"></span><span class=\"c10\">Refugees</span><span class=\"c7\">", "refugees");
            s = s.replace("</span><span class=\"c10\">Brexit</span><span class=\"c7\">", "Brexit");
            s = s.replace("&nbsp;", " ");
            s = s.replace("&amp;", " ");
            String[] split = s.split("</span>");
            result = split[0];
            if (result.contains("<br>")) {
                //result = result.split("<br>")[0].trim();
                result = result.replace("<br>", " ");
            }
        }
        return result;
    }

    LocalDate parseDate(String line) {
        LocalDate result = null;
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
//            String stringDate = "";
//            stringDate += "year " + year;
//            stringDate += " month " + month;
//            stringDate += " dayOfMonth " + dayOfMonth;
//            stringDate += " dayOfWeek " + dayOfWeek;
//            System.out.println(stringDate);
            Month m = Month.valueOf(Generic_StaticString.getUpperCase(month));
            result = LocalDate.of(Integer.valueOf(year), m, Integer.valueOf(dayOfMonth));
            //System.out.println(LD.toString());
        }
        return result;
    }

    class DateHeadline implements Comparable<DateHeadline> {

        LocalDate LD;
        String Headline;

        public DateHeadline() {
        }

        public DateHeadline(LocalDate ld, String headline) {
            LD = ld;
            Headline = headline;
        }

        @Override
        public int compareTo(DateHeadline t) {
            int dateComparison = LD.compareTo(t.LD);
            if (dateComparison == 0) {
                if (Headline == null) {
                    if (t.Headline == null) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (t.Headline == null) {
                        return -1;
                    } else {
                        return Headline.compareTo(t.Headline);
                    }
                }
            } else {
                return dateComparison;
            }
        }
    }
}
