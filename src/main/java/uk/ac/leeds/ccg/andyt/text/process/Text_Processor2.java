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
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
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
 * articles included the terms "refugee" or "brexit" or both.
 */
public class Text_Processor2 {

    /**
     * Files is used to help manage input and output to the file system.
     */
    Text_Files Files;

    public Text_Processor2() {
    }

    public static void main(String[] args) {
        new Text_Processor2().run();
    }

    String GuardianAPIKey;

    /**
     * This is the main processing method.
     */
    public void run() {

        /**
         * Set writeHeadlines to be true to write out the titles of articles for
         * those articles containing the term " syria ".
         */
        boolean writeHeadlines;
        //writeHeadlines = true;
        writeHeadlines = false;

        /**
         * Initialise terms
         */
        TreeMap<Integer, ArrayList<String>> allterms = new TreeMap<>();

        HashMap<Integer, String> termTypes = new HashMap<>();
        int i = 0;
        int index = -1;
        // Key Migrant/Refugee Terms
        termTypes.put(i, "Key Migrant/Refugee Terms");
        ArrayList mrterms = new ArrayList();
        allterms.put(i, mrterms);
        i++;
        mrterms.add(" asylum seeker");
        mrterms.add(" economic migrant ");
        mrterms.add(" humanitarian crisis ");
        mrterms.add(" illegal immigrant ");
        mrterms.add(" illegal migrant ");
        mrterms.add(" immigration crisis ");
        mrterms.add(" migrant crisis ");
        mrterms.add(" migrant flood OR flood of migrants ");
        mrterms.add(" refugee crisis ");
        mrterms.add(" refugee camp ");
        index += mrterms.size();
        // People Terms
        // Types of People Terms
        termTypes.put(i, "Types of People");
        ArrayList ptterms = new ArrayList();
        allterms.put(i, ptterms);
        i++;
        ptterms.add(" alien ");
        ptterms.add(" afghan ");
        ptterms.add(" christian ");
        ptterms.add(" civilian ");
        ptterms.add(" fellow human ");
        ptterms.add(" foreigner ");
        ptterms.add(" human ");
        ptterms.add(" innocent ");
        ptterms.add(" intruder ");
        ptterms.add(" kurd ");
        ptterms.add(" migrant ");
        ptterms.add(" muslim ");
        ptterms.add(" people trafficker OR human trafficker ");
        ptterms.add(" rabid dog ");
        ptterms.add(" refugee ");
        ptterms.add(" smuggler OR smuggling ");
        ptterms.add(" stow away ");
        ptterms.add(" syrian ");
        ptterms.add(" terrorist ");
        ptterms.add(" yemeni ");
        index += ptterms.size();
        // Individuals
        termTypes.put(i, "Individuals");
        ArrayList iterms = new ArrayList();
        allterms.put(i, iterms);
        i++;
        iterms.add(" alan kurdi "); // 3 year old boy washed up on beach (https://en.wikipedia.org/wiki/Death_of_Alan_Kurdi)
        iterms.add(" assad "); // Leader of Syria
        iterms.add(" david cameron OR teresa may "); // UK Prime Ministers
        iterms.add(" erdoğan OR erdogan "); // Leader of Turkey
        iterms.add(" gutteres OR grandi "); // Head of UNHCR
        iterms.add(" hammond OR johnson OR foreign secretary "); // UK foreign secretarys
        iterms.add(" hollande "); // France Prime Minister
        iterms.add(" juncker "); // Head of EC
        iterms.add(" ki-moon OR ki moon OR annan"); // Head of UN
        iterms.add(" merkel "); // Germany Prime Minister
        iterms.add(" nichola sturgeon "); // Leader of The SNP
        iterms.add(" nigel farage "); // Leader of UKIP
        iterms.add(" putin "); // Russian president
        iterms.add(" steven woolfe "); // UKIP Migration Spokesman and UK MEP
        index += iterms.size();
        // Organisations
        // Political
        termTypes.put(i, "Polictical Organisations");
        ArrayList poterms = new ArrayList();
        allterms.put(i, poterms);
        i++;
        poterms.add(" eu turkey deal ");
        poterms.add(" edl ");
        poterms.add(" far right ");
        poterms.add(" is ");
        poterms.add(" isil ");
        poterms.add(" isis ");
        poterms.add(" islamic state ");
        poterms.add(" nazi ");
        poterms.add(" right wing ");
        poterms.add(" ukip ");
        index += poterms.size();
        // Relief Agencies
        termTypes.put(i, "Relief Agencies");
        ArrayList raterms = new ArrayList();
        allterms.put(i, raterms);
        i++;
        raterms.add(" amnesty international ");
        raterms.add(" oxfam ");
        raterms.add(" un ");
        raterms.add(" unicef ");
        raterms.add(" unhcr ");
        raterms.add(" unrwa ");
        raterms.add(" msf ");
        raterms.add(" red cross ");
        index += raterms.size();
        // Places
        // Large Regions
        termTypes.put(i, "Large Regions");
        ArrayList lrterms = new ArrayList();
        allterms.put(i, lrterms);
        i++;
        lrterms.add(" africa ");
        lrterms.add(" asia ");
        lrterms.add(" eu ");
        lrterms.add(" europe ");
        lrterms.add(" mediterranean ");
        index += lrterms.size();
        // Countries
        termTypes.put(i, "Countries");
        ArrayList cterms = new ArrayList();
        allterms.put(i, cterms);
        i++;
        cterms.add(" afghanistan ");
        index++;
        cterms.add(" egypt ");
        index++;
        cterms.add(" england OR britain OR uk ");
        index++;
        cterms.add(" france OR french ");
        index++;
        cterms.add(" germany ");
        index++;
        cterms.add(" greek OR greece ");
        index++;
        cterms.add(" iraq ");
        index++;
        cterms.add(" israel ");
        index++;
        cterms.add(" italy OR italian ");
        index++;
        cterms.add(" jordan ");
        index++;
        cterms.add(" lebanon OR lebanese ");
        index++;
        cterms.add(" macedonia ");
        index++;
        cterms.add(" russia ");
        index++;
        cterms.add(" spain ");
        index++;
        cterms.add(" syria ");
        index++;
        int syriaIndex = index;
        cterms.add(" turkey ");
        index++;
        cterms.add(" west bank ");
        index++;
        cterms.add(" yemen ");
        index++;
        // Regions in Countries
        termTypes.put(i, "Regions in Countries");
        ArrayList ricterms = new ArrayList();
        allterms.put(i, ricterms);
        i++;
        ricterms.add(" bodrum "); // Region of Turkey
        ricterms.add(" lesbos "); // Greek island
        index += ricterms.size();
        // Syrian Cities
        termTypes.put(i, "Syrian Cities");
        ArrayList scterms = new ArrayList();
        allterms.put(i, scterms);
        i++;
        scterms.add(" aleppo ");
        scterms.add(" damascus ");
        scterms.add(" homs ");
        scterms.add(" raqqa ");
        index += scterms.size();
        // Syrian Refugee Camps (see https://en.wikipedia.org/wiki/Syrian_refugee_camps)
        // Refugee Camps in Turkey
        termTypes.put(i, "Refugee Camps in Turkey");
        ArrayList rcitterms = new ArrayList();
        allterms.put(i, rcitterms);
        i++;
        rcitterms.add(" altınözü OR altinozu ");
        rcitterms.add(" yayladağı OR yayladagi ");
        rcitterms.add(" apaydın ");
        rcitterms.add(" güveççi OR guvecci ");
        rcitterms.add(" ceylanpınar ");
        rcitterms.add(" akçakale OR akcakale ");
        rcitterms.add(" harran ");
        rcitterms.add(" viranşehir OR viransehir ");
        rcitterms.add(" suruç OR suruc ");
        rcitterms.add(" islahiye ");
        rcitterms.add(" karkamış OR karkamis ");
        rcitterms.add(" nizip ");
        rcitterms.add(" öncüpınar OR oncupinar ");
        rcitterms.add(" elbeyli besiriye ");
        rcitterms.add(" merkez ");
        rcitterms.add(" cevdetiye ");
        rcitterms.add(" sarıçam OR saricam ");
        rcitterms.add(" midyat ");
        rcitterms.add(" beydağı or beydagi ");
        index += rcitterms.size();
        // Refugee Camps in Jordan
        termTypes.put(i, "Refugee Camps in Jordan");
        ArrayList rcijterms = new ArrayList();
        allterms.put(i, rcijterms);
        i++;
        rcijterms.add(" zaatari ");
        rcijterms.add(" azraq ");
        rcijterms.add(" mrajeeb al fhood ");
        rcijterms.add(" rukban ");
        rcijterms.add(" hadalat ");
        index += rcijterms.size();
        // Refugee Camps in Iraq
        termTypes.put(i, "Refugee Camps in Iraq");
        ArrayList rciiterms = new ArrayList();
        allterms.put(i, rciiterms);
        i++;
        rciiterms.add(" domiz ");
        rciiterms.add(" gawilan ");
        rciiterms.add(" akre ");
        rciiterms.add(" darashakran ");
        rciiterms.add(" kawergosk ");
        rciiterms.add(" qushtapa ");
        rciiterms.add(" basirma ");
        rciiterms.add(" arbat ");
        rciiterms.add(" al-obaidi OR al obaidi OR alobaidi ");
        index += rciiterms.size();
        // Refugee Camps in Macedonia
        termTypes.put(i, "Refugee Camps in Macedonia");
        ArrayList rcimterms = new ArrayList();
        allterms.put(i, rcimterms);
        i++;
        rcimterms.add(" gevgelija ");
        rcimterms.add(" tabanovce ");
        index += rcimterms.size();
        // Refugee Camps in Greece
        termTypes.put(i, "Refugee Camps in Greece");
        ArrayList rcigterms = new ArrayList();
        allterms.put(i, rcigterms);
        i++;
        rcigterms.add(" doliana ");
        rcigterms.add(" katsika ");
        rcigterms.add(" konitsa ");
        rcigterms.add(" filippada ");
        rcigterms.add(" tselepevo ");
        rcigterms.add(" alexandreia ");
        rcigterms.add(" cherso ");
        rcigterms.add(" derveni alexill ");
        rcigterms.add(" eko ");
        rcigterms.add(" diavata ");
        rcigterms.add(" giannitsa ");
        rcigterms.add(" idomeni ");
        rcigterms.add(" kalochori OR iliadi ");
        rcigterms.add(" lagadika ");
        rcigterms.add(" nea kavala ");
        rcigterms.add(" oraiokastro ");
        rcigterms.add(" piera ");
        rcigterms.add(" sinatex ");
        rcigterms.add(" sindos ");
        rcigterms.add(" softex ");
        rcigterms.add(" thessaloniki ");
        rcigterms.add(" vasilika ");
        rcigterms.add(" veria ");
        rcigterms.add(" viagiohori ");
        rcigterms.add(" chalkero ");
        rcigterms.add(" drama ");
        rcigterms.add(" andravidas ");
        rcigterms.add(" oiuofyta ");
        rcigterms.add(" ritsona ");
        rcigterms.add(" thermopiles ");
        rcigterms.add(" kipselochori ");
        rcigterms.add(" larissa ");
        rcigterms.add(" volos ");
        rcigterms.add(" agios andreas ");
        rcigterms.add(" elefsina ");
        rcigterms.add(" eleonas ");
        rcigterms.add(" elliniko ");
        rcigterms.add(" lavrio ");
        rcigterms.add(" malakasa ");
        rcigterms.add(" piraeus ");
        rcigterms.add(" schisto ");
        rcigterms.add(" skaramagas ");
        rcigterms.add(" victoria square ");
        rcigterms.add(" moria ");
        rcigterms.add(" chios ");
        rcigterms.add(" vial ");
        rcigterms.add(" vathy ");
        rcigterms.add(" leros ");
        rcigterms.add(" lepida ");
        rcigterms.add(" kos ");
        rcigterms.add(" rhodes ");
        index += rcigterms.size();
        // Other Camp Terms
        termTypes.put(i, "Other Camp Terms");
        ArrayList octterms = new ArrayList();
        allterms.put(i, octterms);
        i++;
        octterms.add(" calais ");
        octterms.add(" camp ");
        octterms.add(" container camp ");
        octterms.add(" jungle OR jules ferry "); // https://en.wikipedia.org/wiki/Calais_Jungle https://en.wikipedia.org/wiki/Migrants_around_Calais
        octterms.add(" tent camp ");
        index += octterms.size();

        // Other Key terms
        // Border Movement Terms
        termTypes.put(i, "Border Movement Terms");
        ArrayList bmterms = new ArrayList();
        allterms.put(i, bmterms);
        i++;
        bmterms.add(" across the border ");
        bmterms.add(" border ");
        bmterms.add(" crossing ");
        bmterms.add(" boat ");
        bmterms.add(" displacement ");
        bmterms.add(" exodus ");
        bmterms.add(" fence OR wall ");
        bmterms.add(" flee OR fleeing ");
        bmterms.add(" lorry OR lorries ");
        bmterms.add(" migrant ");
        bmterms.add(" movement ");
        bmterms.add(" surge ");
        index += bmterms.size();
        // Movement Scale Descriptors
        termTypes.put(i, "Movement Scale Descriptors");
        ArrayList msdterms = new ArrayList();
        allterms.put(i, msdterms);
        i++;
        msdterms.add(" dam has burst ");
        msdterms.add(" flee ");
        msdterms.add(" flood ");
        msdterms.add(" flow ");
        msdterms.add(" influx ");
        msdterms.add(" swarm ");
        msdterms.add(" torrent ");
        msdterms.add(" engulf ");
        msdterms.add(" storm ");
        msdterms.add(" swell ");
        msdterms.add(" swamp ");
        msdterms.add(" tide ");
        msdterms.add(" wave ");
        index += msdterms.size();
        // Miscellaneous Key Terms
        termTypes.put(i, "Miscellaneous Key Terms");
        ArrayList mktterms = new ArrayList();
        allterms.put(i, mktterms);
        i++;
        mktterms.add(" aid ");
        mktterms.add(" asylum ");
        mktterms.add(" attack ");
        mktterms.add(" brexit ");
        mktterms.add(" burdon ");
        mktterms.add(" child OR children ");
        mktterms.add(" chaos ");
        mktterms.add(" crime ");
        mktterms.add(" crisis ");
        mktterms.add(" deal ");
        mktterms.add(" desperate OR desparation ");
        mktterms.add(" drown ");
        mktterms.add(" dying OR dies OR death ");
        mktterms.add(" disease ");
        mktterms.add(" extremism ");
        mktterms.add(" economic ");
        mktterms.add(" famine ");
        mktterms.add(" foreign ");
        mktterms.add(" hate crime ");
        mktterms.add(" hunger OR malnutrition OR malnourished OR starving ");
        mktterms.add(" illegal ");
        mktterms.add(" immigration ");
        mktterms.add(" imperialism ");
        mktterms.add(" islam ");
        mktterms.add(" islamophobia ");
        mktterms.add(" mop OR stem the flow ");
        mktterms.add(" moral ");
        mktterms.add(" moral imperialism ");
        mktterms.add(" open borders OR open border policy");
        mktterms.add(" nationalism OR nation ");
        mktterms.add(" plight ");
        mktterms.add(" relief ");
        mktterms.add(" resettlement ");
        mktterms.add(" security ");
        mktterms.add(" threat ");
        mktterms.add(" threat to security ");
        mktterms.add(" terror ");
        mktterms.add(" terrorism ");
        mktterms.add(" tragedy OR tragic ");
        mktterms.add(" trafficking ");
        mktterms.add(" work ");
        mktterms.add(" war ");
        index += mktterms.size();
        int numberOfTerms = index + 1;

        /**
         * Initialise directories
         */
        String dataDirName;
        dataDirName = System.getProperty("user.dir") + "/data";
        Files = new Text_Files(dataDirName);
        String dirname;
        dirname = "LexisNexis-20171127T155442Z-001";
//        dirname = "LexisNexis-20171122T195223Z-001";
        File inputDir;
        File outDir;
        inputDir = new File(Files.getLexisNexisInputDataDir(),
                dirname + "/LexisNexis");
        System.out.println(inputDir);
        outDir = new File(Files.getLexisNexisOutputDataDir(),
                dirname + "/LexisNexis");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        // Get GuardianAPIKey
        GuardianAPIKey = getGuardianAPIKey();

        /**
         * Declare variables
         */
        ArrayList<DayOfWeek> mondayToSaturday;
        mondayToSaturday = getMondayToSaturday();
        int[] grandTotalTermCounts;
        HashMap<String, TreeMap<DayOfWeek, Integer>> grandTotalTermCountOnDays;
        int[] grandTotalArticleCountsForTerms;
        HashMap<String, TreeMap<DayOfWeek, Integer>> grandTotalArticleCountsForTermsOnDays;
        String term;
        /**
         * Process the data going through each input file. Currently the output
         * is simply printed to std.out.
         */
        String name;
        File outFile;
        PrintWriter pwCounts;
        PrintWriter pwHeadlines = null;
        File[] inputs0;
        File[] inputs1;
        inputs0 = inputDir.listFiles();
        /**
         * Iterate through all the directories in inputDir. It is known that
         * inputDir contains only directories and no files.
         */
        for (File input0 : inputs0) {
            name = input0.getName();
            outFile = new File(outDir, name + "Counts.csv");
            pwCounts = Generic_StaticIO.getPrintWriter(outFile, false);
            if (writeHeadlines) {
                outFile = new File(outDir,
                        name + "HeadlinesForArticlesContaining_Syria.csv");
                pwHeadlines = Generic_StaticIO.getPrintWriter(outFile, false);
                pwHeadlines.println("Date, Section, Length, Title");
            }
            /**
             * Print out the name of the directory/File.
             */
            //System.out.println(input0);
            System.out.println("---------------------------");
            System.out.println(name);
            //pw.println(name);
            System.out.println("---------------------------");
            /**
             * Iterate through all the files in the directory.
             */
            inputs1 = input0.listFiles();

            /**
             * Initialise results.
             */
            grandTotalTermCounts = new int[numberOfTerms];
            grandTotalArticleCountsForTerms = new int[numberOfTerms];
            grandTotalTermCountOnDays = new HashMap<>();
            grandTotalArticleCountsForTermsOnDays = new HashMap<>();
            i = 0;
            Iterator<Integer> ite;
            ArrayList<String> terms;
            Iterator<String> ite2;
            ite = allterms.keySet().iterator();
            while (ite.hasNext()) {
                terms = allterms.get(ite.next());
                ite2 = terms.iterator();
                while (ite2.hasNext()) {
                    term = ite2.next();
                    grandTotalTermCounts[i] = 0;
                    grandTotalArticleCountsForTerms[i] = 0;
                    i++;
                    grandTotalTermCountOnDays.put(term, new TreeMap<>());
                    grandTotalArticleCountsForTermsOnDays.put(term, new TreeMap<>());
                }
            }
            /**
             * Iterate through all the subdirectories in inputDir. It is known
             * that each subdirectory contains a set of HTML files and
             * associated directories. For the purposes of this processing, only
             * the HTML files are processed.
             */
            int[] totalTermCounts;
            int[] totalTermCountsInArticles;
            Object[] results;
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
                    //if (input1.getParentFile().getName().startsWith("LexisNexis - The G")) {
                    results = parseHTML(numberOfTerms, allterms, input1);
                    /**
                     * Combine the results from parsing this file to the overall
                     * results.
                     */
                    // Process counts.
                    totalTermCounts = (int[]) results[0];
                    totalTermCountsInArticles = (int[]) results[1];
                    i = 0;
                    ite = allterms.keySet().iterator();
                    while (ite.hasNext()) {
                        terms = allterms.get(ite.next());
                        ite2 = terms.iterator();
                        while (ite2.hasNext()) {
                            term = ite2.next();
                            grandTotalTermCounts[i] += totalTermCounts[i];
                            grandTotalArticleCountsForTerms[i] += totalTermCountsInArticles[i];
                            i++;
                            addToCount(
                                    ((TreeMap<String, TreeMap<DayOfWeek, Integer>>) results[2]).get(term),
                                    grandTotalTermCountOnDays.get(term));
                            addToCount(
                                    ((TreeMap<String, TreeMap<DayOfWeek, Integer>>) results[3]).get(term),
                                    grandTotalArticleCountsForTermsOnDays.get(term));

                        }
                    }
                    if (writeHeadlines) {
                        // Process dates and headlines writing out a list.
                        TreeSet<DateOutlineDetails> syriaDateHeadlines;
                        syriaDateHeadlines = (TreeSet<DateOutlineDetails>) results[4];
                        Iterator<DateOutlineDetails> ite3;
                        DateOutlineDetails dh;
                        ite3 = syriaDateHeadlines.iterator();
                        while (ite3.hasNext()) {
                            dh = ite3.next();
                            String s;
                            s = dh.LD + ",\"" + dh.Section + "\",\"" + dh.Length + "\",\"" + dh.Headline + "\"";
                            System.out.println(s);
                            pwHeadlines.println(s);
                        }
                    }
                    //}
                }
            }
            /**
             * Write out summaries of counts.
             */
            /**
             * Write header
             */
            String header;
            header = "Term Type, Term, Total Term Count, Total Article Count";
            TreeMap<DayOfWeek, Integer> grandTotalTermCountOnDay;
            TreeMap<DayOfWeek, Integer> grandTotalArticleCountsForTermsOnDay;
            Iterator<DayOfWeek> ite3;
            DayOfWeek day;
            ite3 = mondayToSaturday.iterator();
            while (ite3.hasNext()) {
                day = ite3.next();
                header += ", Term Count On " + day;
            }
            ite3 = mondayToSaturday.iterator();
            while (ite3.hasNext()) {
                day = ite3.next();
                header += ", Article Count On " + day;
            }
            System.out.println(header);
            pwCounts.println(header);
            /**
             * Write lines
             */
            i = 0;
            ite = allterms.keySet().iterator();
            while (ite.hasNext()) {
                int typeInt = ite.next();
                String termType = termTypes.get(typeInt);
                terms = allterms.get(typeInt);
                ite2 = terms.iterator();
                while (ite2.hasNext()) {
                    term = ite2.next();
                    grandTotalTermCountOnDay = grandTotalTermCountOnDays.get(term);
                    grandTotalArticleCountsForTermsOnDay = grandTotalArticleCountsForTermsOnDays.get(term);
//                System.out.println(term + " term count " + grandTotalTermCounts[i]);
//                pwCounts.println(term + " term count " + grandTotalTermCounts[i]);
//                System.out.println(term + " Article count " + grandTotalArticleCountsForTerms[i]);
//                pwCounts.println(term + " Article count " + grandTotalArticleCountsForTerms[i]);
                    System.out.print(termType);
                    pwCounts.print(termType);
                    System.out.print(", " + term);
                    pwCounts.print(", " + term);
                    System.out.print(", " + grandTotalTermCounts[i]);
                    pwCounts.print(", " + grandTotalTermCounts[i]);
                    System.out.print(", " + grandTotalArticleCountsForTerms[i]);
                    pwCounts.print(", " + grandTotalArticleCountsForTerms[i]);
                    i++;
                    printTermCountOnDay(pwCounts, mondayToSaturday, term, grandTotalTermCountOnDays.get(term));
                    printTermCountOnDay(pwCounts, mondayToSaturday, term, grandTotalArticleCountsForTermsOnDays.get(term));
                    System.out.println();
                    pwCounts.println();
                }
            }
            System.out.println("---------------------------");
            pwCounts.close();
            if (writeHeadlines) {
                pwHeadlines.close();
            }
        }
    }

    void printTermCountOnDay(
            PrintWriter pw,
            ArrayList<DayOfWeek> mondayToSaturday,
            String term,
            TreeMap<DayOfWeek, Integer> grandTotalTermCountOnDay) {
        Iterator<DayOfWeek> ite;
        DayOfWeek day;
        Integer i;
        ite = mondayToSaturday.iterator();
        while (ite.hasNext()) {
            day = ite.next();
            i = grandTotalTermCountOnDay.get(day);
            if (i == null) {
                System.out.print(", 0");
                pw.print(", 0");
            } else {
                System.out.print(", " + i);
                pw.print(", " + i);
            }
        }
    }

    ArrayList<DayOfWeek> getMondayToSaturday() {
        ArrayList<DayOfWeek> result;
        result = new ArrayList<>();
        result.add(DayOfWeek.MONDAY);
        result.add(DayOfWeek.TUESDAY);
        result.add(DayOfWeek.WEDNESDAY);
        result.add(DayOfWeek.THURSDAY);
        result.add(DayOfWeek.FRIDAY);
        result.add(DayOfWeek.SATURDAY);
        return result;
    }

    boolean inArticle;
    boolean isExpressArticle;

    /**
     * Iteratively parse through nodes.
     *
     * @param node
     */
    boolean isArticleNode(Node node) {
        //System.out.println(node.toString());
        String nodeName;
        nodeName = node.nodeName();
        //System.out.println("nodeName " + nodeName);
        int nodeAttributeIndex;
        Attributes nodeAttributes;
        Iterator<Attribute> iteA;
        Attribute nodeAttribute;
        String key;
        String value;
        nodeAttributeIndex = 0;
        nodeAttributes = node.attributes();
        iteA = nodeAttributes.iterator();
        while (iteA.hasNext()) {
            //System.out.println("nodeAttributeIndex " + nodeAttributeIndex);
            nodeAttribute = iteA.next();
            key = nodeAttribute.getKey();
            value = nodeAttribute.getValue();
            //System.out.println("key " + key);
            //System.out.println("value " + value);
            if (value.equalsIgnoreCase("The Express") || value.equalsIgnoreCase("The Guardian")) {
                if (value.equalsIgnoreCase("The Guardian")) {
                    isExpressArticle = false;
                } else {
                    isExpressArticle = true;
                }
                return true;
                //parseExpressNode(node);
            }
            nodeAttributeIndex++;
        }
        return false;
    }

    String Date;
    boolean gotDate;
    int returnCount;

    /**
     * @param node
     */
    boolean getDate(Node node) {
        //System.out.println(node.toString());
        String nodeName;
        nodeName = node.nodeName();
        //System.out.println("nodeName " + nodeName);
        int nodeAttributeIndex;
        Attributes nodeAttributes;
        Iterator<Attribute> iteA;
        Attribute nodeAttribute;
        String key;
        String value;
        nodeAttributeIndex = 0;
        nodeAttributes = node.attributes();
        iteA = nodeAttributes.iterator();
        while (iteA.hasNext()) {
            //System.out.println("nodeAttributeIndex " + nodeAttributeIndex);
            nodeAttribute = iteA.next();
            key = nodeAttribute.getKey();
            value = nodeAttribute.getValue();
            //System.out.println("key " + key);
            //System.out.println("value " + value);
            if (key.equalsIgnoreCase("#text")) {
                if (!value.equalsIgnoreCase("\n")) {
                    Date += value;
                    if (isExpressArticle) {
                        if (value.endsWith("day")) {
                            return true;
                        }
                    } else {
                        if (value.endsWith("GMT")) {
                            return true;
                        }
                    }
                }
            }
            nodeAttributeIndex++;
        }
        return false;
    }

    String Title;
    boolean startTitle;
    boolean gotTitle;

    /**
     * @param node
     */
    boolean getTitle(Node node) {
        //System.out.println(node.toString());
        String nodeName;
        nodeName = node.nodeName();
        //System.out.println("nodeName " + nodeName);
        int nodeAttributeIndex;
        Attributes nodeAttributes;
        Iterator<Attribute> iteA;
        Attribute nodeAttribute;
        String key;
        String value;
        nodeAttributeIndex = 0;
        nodeAttributes = node.attributes();
        iteA = nodeAttributes.iterator();
        while (iteA.hasNext()) {
            //System.out.println("nodeAttributeIndex " + nodeAttributeIndex);
            nodeAttribute = iteA.next();
            key = nodeAttribute.getKey();
            value = nodeAttribute.getValue();
            //System.out.println("key " + key);
            //System.out.println("value " + value);
            if (!startTitle) {
                if (value.equalsIgnoreCase("c7")) {
                    startTitle = true;
                }
            } else {
                if (key.equalsIgnoreCase("#text")) {
                    /**
                     * Replace all non alphabetical non numeric characters with
                     * a space. This is to help overcome issues with searching
                     * for terms that might be found in other words. It is not a
                     * perfect solution as some terms made up of several words
                     * might fall across two sentences and not really be terms
                     * at all, but just a set of words in the same order (e.g.
                     * instead of counting "migrant crisis this might count "...
                     * migrant. Crisis ...").
                     */
                    value = value.replaceAll("[^A-Za-z0-9]", " ");
                    Title += value;
                }
                if (value.equalsIgnoreCase("c6")) {
                    // Remove double spaces
                    while (Title.contains("  ")) {
                        Title = Title.replaceAll("  ", " ");
                    }
                    return true;
                }
            }
            nodeAttributeIndex++;
        }
        return false;
    }

    String Section;
    boolean startSection;
    boolean gotSection;

    /**
     * Iteratively parse through nodes.
     *
     * @param node
     */
    boolean getSection(Node node) {
        //System.out.println("Node " + node.toString());
        String nodeName;
        nodeName = node.nodeName();
        //System.out.println("nodeName " + nodeName);
        int nodeAttributeIndex;
        Attributes nodeAttributes;
        Iterator<Attribute> iteA;
        Attribute nodeAttribute;
        String key;
        String value;
        nodeAttributeIndex = 0;
        nodeAttributes = node.attributes();
        iteA = nodeAttributes.iterator();
        while (iteA.hasNext()) {
            //System.out.println("nodeAttributeIndex " + nodeAttributeIndex);
            nodeAttribute = iteA.next();
            key = nodeAttribute.getKey();
            value = nodeAttribute.getValue();
            //System.out.println("key " + key);
            //System.out.println("value " + value);
            if (!startSection) {
                if (value.equalsIgnoreCase("SECTION: ")) {
                    startSection = true;
                }
            } else {
                if (key.equalsIgnoreCase("#text")) {
                    Section += value;
                    return true;
                }
            }
            nodeAttributeIndex++;
        }
        return false;
    }

    String Length;
    boolean startLength;
    boolean gotLength;

    /**
     * Iteratively parse through nodes.
     *
     * @param node
     */
    boolean getLength(Node node) {
        //System.out.println("Node " + node.toString());
        String nodeName;
        nodeName = node.nodeName();
        //System.out.println("nodeName " + nodeName);
        int nodeAttributeIndex;
        Attributes nodeAttributes;
        Iterator<Attribute> iteA;
        Attribute nodeAttribute;
        String key;
        String value;
        nodeAttributeIndex = 0;
        nodeAttributes = node.attributes();
        iteA = nodeAttributes.iterator();
        while (iteA.hasNext()) {
            //System.out.println("nodeAttributeIndex " + nodeAttributeIndex);
            nodeAttribute = iteA.next();
            key = nodeAttribute.getKey();
            value = nodeAttribute.getValue();
            //System.out.println("key " + key);
            //System.out.println("value " + value);
            if (!startLength) {
                if (value.equalsIgnoreCase("LENGTH: ")) {
                    startLength = true;
                }
            } else {
                if (key.equalsIgnoreCase("#text")) {
                    Length += value;
                    return true;
                }
            }
            nodeAttributeIndex++;
        }
        return false;
    }

    String Article;
    boolean startArticle;
    boolean gotArticle;

    /**
     * Iteratively parse through nodes.
     *
     * @param node
     */
    boolean getArticle(Node node) {
        //System.out.println("Node " + node.toString());
        if (node.toString().equalsIgnoreCase("LOAD-DATE ")) {
            return true;
        }
        String nodeName;
        nodeName = node.nodeName();
        //System.out.println("nodeName " + nodeName);
        // end at div
        int nodeAttributeIndex;
        Attributes nodeAttributes;
        Iterator<Attribute> iteA;
        Attribute nodeAttribute;
        String key;
        String value;

//        if (node.childNodeSize() > 0) {
//            List<Node> childNodes;
//            childNodes = node.childNodes();
//            Node childNode;
//            Iterator<Node> ite;
//            ite = childNodes.iterator();
//            while (ite.hasNext()) {
//                childNode = ite.next();
//                System.out.println("Node " + childNode.toString());
////                if (childNode.toString().equalsIgnoreCase("SECTION:")) {
////                    startSection = true;
////                }
//                nodeAttributeIndex = 0;
//                nodeAttributes = node.attributes();
//                iteA = nodeAttributes.iterator();
//                while (iteA.hasNext()) {
//                    System.out.println("nodeAttributeIndex " + nodeAttributeIndex);
//                    nodeAttribute = iteA.next();
//                    key = nodeAttribute.getKey();
//                    value = nodeAttribute.getValue();
//                    System.out.println("key " + key);
//                    System.out.println("value " + value);
//                    if (!startLength) {
//                        if (value.equalsIgnoreCase("c7")) {
//                            startLength = true;
//                        }
//                    } else {
//                        if (key.equalsIgnoreCase("#text")) {
//                            Length += value;
//                            //if (value.endsWith("day")) {
//                            //    return true;
//                            //}
//                        }
////                if (value.equalsIgnoreCase("c6")) {
////                    return true;
////                }
//                    }
//                    nodeAttributeIndex++;
//                }
//
//            }
//        }
        nodeAttributeIndex = 0;
        nodeAttributes = node.attributes();
        iteA = nodeAttributes.iterator();
        while (iteA.hasNext()) {
            //System.out.println("nodeAttributeIndex " + nodeAttributeIndex);
            nodeAttribute = iteA.next();
            key = nodeAttribute.getKey();
            value = nodeAttribute.getValue();
            //System.out.println("key " + key);
            //System.out.println("value " + value);
            if (key.equalsIgnoreCase("#text")) {
                if (!value.equalsIgnoreCase("\n")) {
                    if (value.equalsIgnoreCase("LOAD-DATE: ")) {
                        // Remove double spaces.
                        while (Article.contains("  ")) {
                            Article = Article.replaceAll("  ", " ");
                        }
                        return true;
                    }
                    /**
                     * Replace all non alphabetical non numeric characters with
                     * a space. This is to help overcome issues with searching
                     * for terms that might be found in other words. It is not a
                     * perfect solution as some terms made up of several words
                     * might fall across two sentences and not really be terms
                     * at all, but just a set of words in the same order (e.g.
                     * instead of counting "migrant crisis this might count "...
                     * migrant. Crisis ...").
                     */
                    value = value.replaceAll("[^A-Za-z0-9]", " ");
                    Article += value + " ";
                }
                //return true;
                //if (value.endsWith("day")) {
                //    return true;
                //}
            }
//                if (value.equalsIgnoreCase("c6")) {
//                    return true;
//                }
            nodeAttributeIndex++;
        }
        return false;
    }

    void parseChildNodes(Node node) {
        if (node.childNodeSize() > 0) {
            List<Node> childNodes;
            childNodes = node.childNodes();
            Node childNode;
            Iterator<Node> ite;
            ite = childNodes.iterator();
            while (ite.hasNext()) {
                childNode = ite.next();
                isArticleNode(childNode);
            }
        }
    }

    /**
     * This method parses the HTML file and returns results that are packed into
     * an Object[] result of size 5: result[0] is an int[] of size 3 containing
     * counts of the numbers of mentions of the terms "refugee", "syria" and
     * "brexit" respectively. result[1] is TreeMap with keys that are the
     * DayOfWeek and values that are counts of the number of times the term
     * "refugee" appears on those days. result[2] is TreeMap with keys that are
     * the DayOfWeek and values that are counts of the number of times the term
     * "syria" appears on those days. result[3] is TreeMap with keys that are
     * the DayOfWeek and values that are counts of the number of times the term
     * "brexit" appears on those days. result[4] is a TreeSet of DateHeadlines
     * which provides the dated and headlines of articles that have mention of
     * "syria" in them.
     *
     * @param n
     * @param allterms
     * @param input The input file to be parsed.
     * @return
     */
    public Object[] parseHTML(int n,
            TreeMap<Integer, ArrayList<String>> allterms, File input) {
        inArticle = false;
        gotDate = false;
        gotTitle = false;
        Object[] result = new Object[5];
        TreeSet<DateOutlineDetails> syriaDateHeadlines;
        syriaDateHeadlines = new TreeSet<>();
//        BufferedReader br;
//        br = Generic_StaticIO.getBufferedReader(input);

        Document doc = null;
        try {
            doc = Jsoup.parse(input, "utf-8");
            //String title = doc.title();
            //System.out.println(title);
        } catch (IOException ex) {
            Logger.getLogger(Text_Processor2.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<String> terms;
        int[] totalTermCounts = new int[n];
        result[0] = totalTermCounts;
        int[] totalArticleCountsForTerms = new int[n];
        result[1] = totalArticleCountsForTerms;
        int[] termCounts = new int[n];
        int[] articleCountsForTerms = new int[n];
        TreeMap<String, TreeMap<DayOfWeek, Integer>> totalTermCountByDay;
        totalTermCountByDay = new TreeMap<>();
        TreeMap<String, TreeMap<DayOfWeek, Integer>> totalArticleCountForTermsByDay;
        totalArticleCountForTermsByDay = new TreeMap<>();
        int i;
        String term;
        Iterator<String> ite2;
        i = 0;
        Iterator<Integer> iteB;
        iteB = allterms.keySet().iterator();
        while (iteB.hasNext()) {
            terms = allterms.get(iteB.next());
            ite2 = terms.iterator();
            while (ite2.hasNext()) {
                term = ite2.next();
                totalTermCounts[i] = 0;
                totalArticleCountsForTerms[i] = 0;
                termCounts[i] = 0;
                articleCountsForTerms[i] = 0;
                i++;
                totalTermCountByDay.put(term, new TreeMap<>());
                totalArticleCountForTermsByDay.put(term, new TreeMap<>());
            }
        }

        Elements elements;
        Element element;
        Iterator<Element> ite;
        Attributes elementAttributes;
        Attribute elementAttribute;
        List<Node> nodes;
        Iterator<Node> iteN;
        Node node;
        Attributes nodeAttributes;
        Attribute nodeAttribute;
        Iterator<Attribute> iteA;
        String key;
        String value;

        int elementIndex = 0;
        int elementAttributeIndex;
        int nodeIndex;

        elements = doc.getAllElements();// work from here using jsoup
        //Elements links = doc.getElementsByTag("div");
        ite = elements.iterator();
        while (ite.hasNext()) {

            // if inExpress then do the next thing here...
            //System.out.println("elementIndex " + elementIndex);
            element = ite.next();
            if (element.hasText()) {
                //System.out.println(element.wholeText()); // Prints out entire document
                //System.out.println(element.text()); // Prints out entire document
                //System.out.println(element.nodeName());
            }
            elementAttributeIndex = 0;
            elementAttributes = element.attributes();
            iteA = elementAttributes.iterator();
            while (iteA.hasNext()) {
                //System.out.println("elementAttributeIndex " + elementAttributeIndex);
                nodeAttribute = iteA.next();
                key = nodeAttribute.getKey();
                value = nodeAttribute.getValue();
                //System.out.println("key " + key);
                //System.out.println("value " + value);
                elementAttributeIndex++;
            }
            nodeIndex = 0;
            nodes = element.childNodes();
            iteN = nodes.iterator();
            while (iteN.hasNext()) {
                //System.out.println("nodeIndex " + nodeIndex);
                node = iteN.next();
                if (inArticle) {
                    if (gotDate) {
                        if (gotTitle) {
                            if (gotSection) {
                                if (gotLength) {
                                    gotArticle = getArticle(node);
                                } else {
                                    gotLength = getLength(node);
                                    Article = "";
                                }
                            } else {
                                gotSection = getSection(node);
                                Length = "";
                            }
                        } else {
                            //System.out.println("Got Date");
                            gotTitle = getTitle(node);
                            Section = "";
                        }
                    } else {
                        //System.out.println("In Express Article");
                        gotDate = getDate(node);
                        Title = "";
                    }
                } else {
                    inArticle = isArticleNode(node);
                    Date = "";
                }
                nodeIndex++;
            }
            if (gotArticle) {
                //System.out.println("Got everything needed to process article... Process and reset.");
                //System.out.println("Date " + Date);
                LocalDate ld = parseDate(Date);
                //System.out.println("Title " + Title);
                //System.out.println("Section " + Section);
                //System.out.println("Length " + Length);
                //System.out.println("Article " + Article);
                DayOfWeek day = ld.getDayOfWeek();
                i = 0;
                iteB = allterms.keySet().iterator();
                while (iteB.hasNext()) {
                    terms = allterms.get(iteB.next());
                    ite2 = terms.iterator();
                    while (ite2.hasNext()) {
                        term = ite2.next();
                        termCounts[i] += getTermCount(term, Article);
                        if (termCounts[i] > 0) {
                            totalTermCounts[i] += termCounts[i];
                            totalArticleCountsForTerms[i]++;
                            addToCount(totalArticleCountForTermsByDay.get(term), day, 1);
                        }
                        addToCount(totalTermCountByDay.get(term), day, termCounts[i]);
                        i++;
                    }
                }
                /**
                 * Store DateHeadline's for those articles on Saturdays that
                 * contain the term " syria ".
                 */
                if (termCounts[0] > 0) {
                    if (ld.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                        if (!isExpressArticle) {
                            // Fire off to Guardian Open Data to try to get page number...
                            int code = 1;

                            String title;
                            title = Title.replaceAll(" ", "-");
                            title = title.replaceAll("\\.", "");
                            title = title.replaceAll(";", "");
                            title = title.replaceAll(":", "");
                            title = title.replaceAll("\\?", "");
                            title = title.replaceAll("!", "");

//                            String url;
//                            url = "http://content.guardianapis.com/search?show-fields=newspaperPageNumber%2CnewspaperEditionDate&q="
//                                    + title + "&api-key=" + GuardianAPIKey;
                        }

                        syriaDateHeadlines.add(new DateOutlineDetails(ld,
                                Section, Length, Title));
                    }
                }
                for (i = 0; i < n; i++) {
                    termCounts[i] = 0;
                }
                inArticle = false;
                gotDate = false;
                startTitle = false;
                gotTitle = false;
                startSection = false;
                gotSection = false;
                startLength = false;
                gotLength = false;
                gotArticle = false;
            }
            elementIndex++;
        }

        result[2] = totalTermCountByDay;
        result[3] = totalArticleCountForTermsByDay;
        result[4] = syriaDateHeadlines;
        return result;
    }

    public void addToCount(
            TreeMap<DayOfWeek, Integer> totalTermCountOnDay,
            TreeMap<DayOfWeek, Integer> grandTotalTermCountOnDays) {
        DayOfWeek day;
        int i;
        Iterator<DayOfWeek> ite = totalTermCountOnDay.keySet().iterator();
        while (ite.hasNext()) {
            day = ite.next();
            if (grandTotalTermCountOnDays.containsKey(day)) {
                i = grandTotalTermCountOnDays.get(day);
                i += totalTermCountOnDay.get(day);
            } else {
                i = totalTermCountOnDay.get(day);
            }
            grandTotalTermCountOnDays.put(day, i);
        }
    }

    public void addToCount(
            TreeMap<DayOfWeek, Integer> termCount,
            DayOfWeek day, int count) {
        int i;
        if (termCount.containsKey(day)) {
            i = termCount.get(day);
            i += count;
        } else {
            i = count;
        }
        termCount.put(day, i);
    }

    /**
     * A generalised method that counts the number of times term appears in
     * line. This is done for the case that term is provided in, for term in all
     * upper case and for term with a capitalised/uppercase first letter.
     *
     * @param term
     * @param line
     * @return
     */
    int getTermCount(String term, String line) {
        String lowerCaseLine = Generic_StaticString.getLowerCase(line);
        int result = 0;
        if (term.contains("OR")) {
            String[] split;
            split = term.split("OR");
            for (String split1 : split) {
                result += lowerCaseLine.split(split1).length - 1;
            }
        } else {
            result += lowerCaseLine.split(term).length - 1;
        }
        return result;
    }

    /**
     * For parsing a line. If it is thought to be a Date as it contains some key
     * text that it is assumed all dates have, then the Date is returned
     * otherwise an empty String is returned.
     *
     * @param line
     * @return
     */
    LocalDate parseDate(String s) {
        LocalDate result;
        String month;
        String dayOfMonth;
        String year;
        //String dayOfWeek;
        String[] split;
        split = s.split(", ");
        String[] split2;
        split2 = split[0].split(" ");
        month = split2[0];
        dayOfMonth = split2[1];
        split2 = split[1].split(" ");
        year = split2[0];
        //dayOfWeek = split2[1];
//        String stringDate = "";
//        stringDate += "year " + year;
//        stringDate += " month " + month;
//        stringDate += " dayOfMonth " + dayOfMonth;
//        stringDate += " dayOfWeek " + dayOfWeek;
//        System.out.println(stringDate);
        Month m = Month.valueOf(Generic_StaticString.getUpperCase(month));
        result = LocalDate.of(Integer.valueOf(year), m, Integer.valueOf(dayOfMonth));
        return result;
    }

    String getGuardianAPIKey() {
        String result = "";
        File f;
        File dir;
        dir = new File(Files.getDataDir(), "private");
        f = new File(dir, "GuardianAPIKey.txt");
        BufferedReader br;
        br = Generic_StaticIO.getBufferedReader(f);
        try {
            result = br.readLine();
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(Text_Processor2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * A simple inner class for wrapping a LocalDate and a String such that
     * different instances can be ordered which the are first by Date and then
     * by the String.
     */
    public class DateOutlineDetails implements Comparable<DateOutlineDetails> {

        LocalDate LD;
        String Section;
        String Length;
        String Headline;

        public DateOutlineDetails() {
        }

        public DateOutlineDetails(
                LocalDate ld, String section, String length, String headline) {
            LD = ld;
            Section = section;
            Length = length;
            Headline = headline;
        }

        @Override
        public int compareTo(DateOutlineDetails t) {
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
                        int outlineDetailsComparison;
                        outlineDetailsComparison = (Section + Length + Headline).compareTo(
                                t.Headline + t.Length + t.Headline);
                        return outlineDetailsComparison;
                    }
                }
            } else {
                return dateComparison;
            }
        }
    }
}
