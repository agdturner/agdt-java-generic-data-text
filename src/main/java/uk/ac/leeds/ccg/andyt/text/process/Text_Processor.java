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

    /**
     * Files is used to help manage input and output to the file system.
     */
    Text_Files Files;

    public Text_Processor() {
    }

    public static void main(String[] args) {
        new Text_Processor().run();
    }

    /**
     * This is the main processing method.
     */
    public void run() {

        boolean writeHeadlines;
        writeHeadlines = true;
        writeHeadlines = false;

        // Initialise directories
        String dataDirName;
        dataDirName = System.getProperty("user.dir") + "/data";
        Files = new Text_Files(dataDirName);
        File inputDir;
        inputDir = new File(
                Files.getLexisNexisInputDataDir(),
                "LexisNexis-20171127T155442Z-001/LexisNexis");
//                "LexisNexis-20171122T195223Z-001/LexisNexis");
        System.out.println(inputDir);
        /**
         * Process the data going through each input file. Currently the output
         * is simply printed to std.out.
         */
        File[] inputs0;
        File[] inputs1;
        inputs0 = inputDir.listFiles();
        /**
         * Iterate through all the directories in inputDir. It is known that
         * inputDir contains only directories and no files.
         */
        for (File input0 : inputs0) {
            /**
             * Print out the name of the directory/File.
             */
            //System.out.println(input0);
            System.out.println(input0.getName());
            /**
             * Iterate through all the files in the directory.
             */
            inputs1 = input0.listFiles();
            /**
             * Initialise results.
             */
            int grandTotalWordCount_Refugee = 0;
            int grandTotalWordCount_Syria = 0;
            int grandTotalWordCount_Brexit = 0;
            int grandTotalWordCount_MigrantCrisis = 0;

            TreeMap<DayOfWeek, Integer> grandTotalWordCountOnDays_Refugee = new TreeMap<>();
            TreeMap<DayOfWeek, Integer> grandTotalWordCountOnDays_Syria = new TreeMap<>();
            TreeMap<DayOfWeek, Integer> grandTotalWordCountOnDays_Brexit = new TreeMap<>();
            TreeMap<DayOfWeek, Integer> grandTotalWordCountOnDays_MigrantCrisis = new TreeMap<>();

            /**
             * Iterate through all the subdirectories in inputDir. It is known
             * that each subdirectory contains a set of HTML files and
             * associated directories. For the purposes of this processing, only
             * the HTML files are processed.
             */
            for (File input1 : inputs1) {
                //System.out.println(input1);
                /**
                 * Filter to only process the HTML files.
                 */
                if (input1.getName().endsWith("htm")) {
                    //System.out.println(input1);
                    /**
                     * Parse the HTML file and obtain part of the result.
                     */
                    Object[] results = parseHTML(input1);
                    /**
                     * Combine the results from parsing this file to the overall
                     * results.
                     */
                    // Process counts.
                    int[] totals;
                    totals = (int[]) results[0];
                    grandTotalWordCount_Refugee += totals[0];
                    grandTotalWordCount_Syria += totals[1];
                    grandTotalWordCount_Brexit += totals[2];
                    grandTotalWordCount_MigrantCrisis += totals[3];
                    // Refugee count
                    addToCount(
                            (TreeMap<DayOfWeek, Integer>) results[1],
                            grandTotalWordCountOnDays_Refugee);
                    // Syria count
                    addToCount(
                            (TreeMap<DayOfWeek, Integer>) results[2],
                            grandTotalWordCountOnDays_Syria);
                    // Brexit count
                    addToCount(
                            (TreeMap<DayOfWeek, Integer>) results[3],
                            grandTotalWordCountOnDays_Brexit);
                    // Migrant Crisis count
                    addToCount(
                            (TreeMap<DayOfWeek, Integer>) results[4],
                            grandTotalWordCountOnDays_MigrantCrisis);
                    if (writeHeadlines) {
                        // Process dates and headlines writing out a list.
                        TreeSet<DateHeadline> syriaDateHeadlines;
                        syriaDateHeadlines = (TreeSet<DateHeadline>) results[5];
                        Iterator<DateHeadline> ite2;
                        DateHeadline dh;
                        ite2 = syriaDateHeadlines.iterator();
                        while (ite2.hasNext()) {
                            dh = ite2.next();
                            System.out.println(dh.LD + " " + dh.Headline);
                        }
                    }
                }
            }
            /**
             * Write out summaries of counts.
             */
            //System.out.println(input0.getName());
            // Overall summaries.
            System.out.println("Refugee word count " + grandTotalWordCount_Refugee);
            System.out.println("Syria word count " + grandTotalWordCount_Syria);
            System.out.println("Brexit word count " + grandTotalWordCount_Brexit);
            System.out.println("Migrant Crisis word count " + grandTotalWordCount_MigrantCrisis);
            // Summaries for each day of the week.
            // Refugee
            printWordCountOnDay("Refugee", grandTotalWordCountOnDays_Refugee);
            // Syria
            printWordCountOnDay("Syria", grandTotalWordCountOnDays_Syria);
            // Brexit
            printWordCountOnDay("Brexit", grandTotalWordCountOnDays_Brexit);
            // Brexit
            printWordCountOnDay("Migrant Crisis", grandTotalWordCountOnDays_MigrantCrisis);
        }
    }

    void printWordCountOnDay(
            String word,
            TreeMap<DayOfWeek, Integer> grandTotalWordCountOnDay) {
        Iterator<DayOfWeek> ite;
        DayOfWeek day;
        int i;
        ite = grandTotalWordCountOnDay.keySet().iterator();
        while (ite.hasNext()) {
            day = ite.next();
            i = grandTotalWordCountOnDay.get(day);
            System.out.println(word + " word count on " + day + " " + i);
        }
        System.out.println("");
    }

    /**
     * This method parses the HTML file and returns results that are packed into
     * an Object[] result of size 5: result[0] is an int[] of size 3 containing
     * counts of the numbers of mentions of the terms "refugee", "syria" and
     * "brexit" respectively. result[1] is TreeMap with keys that are the
     * DayOfWeek and values that are counts of the number of times the word
     * "refugee" appears on those days. result[2] is TreeMap with keys that are
     * the DayOfWeek and values that are counts of the number of times the word
     * "syria" appears on those days. result[3] is TreeMap with keys that are
     * the DayOfWeek and values that are counts of the number of times the word
     * "brexit" appears on those days. result[4] is a TreeSet of DateHeadlines
     * which provides the dated and headlines of articles that have mention of
     * "syria" in them.
     *
     * @param input The input file to be parsed.
     * @return
     */
    public Object[] parseHTML(File input) {
        Object[] result = new Object[6];
        int[] totals = new int[4];
        result[0] = totals;
        TreeSet<DateHeadline> syriaDateHeadlines;
        syriaDateHeadlines = new TreeSet<>();
        BufferedReader br;
        br = Generic_StaticIO.getBufferedReader(input);
        String line = null;
        boolean read = false;
        int totalWordCount_Refugee = 0;
        int totalWordCount_Syria = 0;
        int totalWordCount_Brexit = 0;
        int totalWordCount_MigrantCrisis = 0;
        TreeMap<DayOfWeek, Integer> totalWordCountByDay_Refugee = new TreeMap<>();
        TreeMap<DayOfWeek, Integer> totalWordCountByDay_Syria = new TreeMap<>();
        TreeMap<DayOfWeek, Integer> totalWordCountByDay_Brexit = new TreeMap<>();
        TreeMap<DayOfWeek, Integer> totalWordCountByDay_MigrantCrisis = new TreeMap<>();
        int wordCount_Refugee = 0;
        int wordCount_Syria = 0;
        int wordCount_Brexit = 0;
        int wordCount_MigrantCrisis = 0;
        LocalDate date0 = null;
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
                /**
                 * The file is completely read, so set read to true to exit the
                 * loop.
                 */
                read = true;
            } else {
                //System.out.println(line);
                LocalDate date;
                if (!gotFirstDate) {
                    // Get to the first article and store the date in date0.
                    date0 = parseDate(line);
                    if (date0 != null) {
                        gotFirstDate = true;
                    }
                } else {
                    /**
                     * If the line contains a new date, then assume this is the
                     * start of a new article: compile the data from the last
                     * article; and, reset the variables to store information
                     * about the next article.
                     */
                    date = parseDate(line);
                    if (date == null) {
                        /**
                         * Parse the next article by first getting the title.
                         */
                        if (!gotTitle) {
                            title = parseTitle(line);
                            if (!title.isEmpty()) {
                                gotTitle = true;
                                //System.out.println(title);
                            }
                        }
                        // Add to word counts
                        wordCount_Refugee += getWordCount("refugee", line);
                        wordCount_Syria += getWordCount("syria", line);
                        wordCount_Brexit += getWordCount("brexit", line);
                        wordCount_MigrantCrisis += getWordCount("migrant crisis", line);
                    } else {
                        gotTitle = false;
//                    System.out.println("refugeeWordCount " + refugeeWordCount);
//                    System.out.println("syriaWordCount " + syriaWordCount);
//                    System.out.println("brexitWordCount " + brexitWordCount);
                        totalWordCount_Refugee += wordCount_Refugee;
                        totalWordCount_Syria += wordCount_Syria;
                        totalWordCount_Brexit += wordCount_Brexit;
                        totalWordCount_MigrantCrisis += wordCount_MigrantCrisis;
                        DayOfWeek day;
                        day = date0.getDayOfWeek();
                        int i;
                        // Refugee
                        addToCount(totalWordCountByDay_Refugee, day, wordCount_Refugee);
                        // Syria
                        addToCount(totalWordCountByDay_Syria, day, wordCount_Syria);
                        // Brexit
                        addToCount(totalWordCountByDay_Brexit, day, wordCount_Brexit);
                        // Brexit
                        addToCount(totalWordCountByDay_MigrantCrisis, day, wordCount_MigrantCrisis);
                        //System.out.println(date);
                        /**
                         * Store DateHeadline's for those articles on Saturdays
                         * that contain the word "syria".
                         */
                        if (wordCount_Syria > 0) {
                            if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                                syriaDateHeadlines.add(new DateHeadline(date, title));
                            }
                        }
                        wordCount_Refugee = 0;
                        wordCount_Syria = 0;
                        wordCount_Brexit = 0;
                        wordCount_MigrantCrisis = 0;
                        date0 = date;
                    }
                }
            }
        }
        totals[0] = totalWordCount_Refugee;
        totals[1] = totalWordCount_Syria;
        totals[2] = totalWordCount_Brexit;
        totals[3] = totalWordCount_MigrantCrisis;
        result[1] = totalWordCountByDay_Refugee;
        result[2] = totalWordCountByDay_Syria;
        result[3] = totalWordCountByDay_Brexit;
        result[4] = totalWordCountByDay_MigrantCrisis;
        result[5] = syriaDateHeadlines;
        return result;
    }

    public void addToCount(
            TreeMap<DayOfWeek, Integer> totalWordCountOnDay,
            TreeMap<DayOfWeek, Integer> grandTotalWordCountOnDays) {
        DayOfWeek day;
        int i;
        Iterator<DayOfWeek> ite = totalWordCountOnDay.keySet().iterator();
        while (ite.hasNext()) {
            day = ite.next();
            if (grandTotalWordCountOnDays.containsKey(day)) {
                i = grandTotalWordCountOnDays.get(day);
                i += totalWordCountOnDay.get(day);
            } else {
                i = totalWordCountOnDay.get(day);
            }
            grandTotalWordCountOnDays.put(day, i);
        }
    }

    public void addToCount(
            TreeMap<DayOfWeek, Integer> wordCount,
            DayOfWeek day, int count) {
        int i;
        if (wordCount.containsKey(day)) {
            i = wordCount.get(day);
            i += count;
        } else {
            i = count;
        }
        wordCount.put(day, i);
    }

    /**
     * A generalised method that counts the number of times word appears in
     * line. This is done for the case that word is provided in, for word in all
     * upper case and for word with a capitalised/uppercase first letter.
     *
     * @param word
     * @param line
     * @return
     */
    int getWordCount(String word, String line) {
        String lowerCaseLine = Generic_StaticString.getLowerCase(line);
        int result = 0;
        result += lowerCaseLine.split(word).length - 1;
        return result;
    }

    /**
     * For parsing a line. If it is thought to be a title as it contains some
     * key text that it is assumed all titles have, then the title is returned
     * otherwise an empty String is returned.
     *
     * @param line The line that is parsed.
     * @return
     */
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

    /**
     * For parsing a line. If it is thought to be a date as it contains some key
     * text that it is assumed all dates have, then the date is returned
     * otherwise an empty String is returned.
     *
     * @param line
     * @return
     */
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

    /**
     * A simple inner class for wrapping a LocalDate and a String such that
     * different instances can be ordered which the are first by date and then
     * by the String.
     */
    public class DateHeadline implements Comparable<DateHeadline> {

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
