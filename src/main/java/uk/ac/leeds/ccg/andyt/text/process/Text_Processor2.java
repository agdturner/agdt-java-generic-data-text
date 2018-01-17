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
 * articles included the words "refugee" or "brexit" or both.
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

        boolean writeHeadlines;
        writeHeadlines = true;
        //writeHeadlines = false;

        /**
         * Initialise words
         */
        ArrayList words = new ArrayList();
        words.add("refugee");
        words.add("syria");
        words.add("brexit");
        words.add("migrant crisis");
        words.add("steven woolfe");
        words.add("refugee crisis");
        words.add("crisis");
        words.add("immigration crisis");
        words.add("illegal immigrant");

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
        File outputDir;
        inputDir = new File(
                Files.getLexisNexisInputDataDir(),
                dirname + "/LexisNexis");
        System.out.println(inputDir);
        outputDir = new File(
                Files.getLexisNexisOutputDataDir(),
                dirname + "/LexisNexis");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Get GuardianAPIKey
        GuardianAPIKey = getGuardianAPIKey();

        /**
         * Declare variables
         */
        ArrayList<DayOfWeek> mondayToSaturday;
        mondayToSaturday = getMondayToSaturday();
        int[] grandTotalWordCounts;
        HashMap<String, TreeMap<DayOfWeek, Integer>> grandTotalWordCountOnDays;
        int[] grandTotalArticleCountsForWords;
        HashMap<String, TreeMap<DayOfWeek, Integer>> grandTotalArticleCountsForWordsOnDays;
        int i;
        String word;
        Iterator<String> ite;
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
            outFile = Generic_StaticIO.createNewFile(outputDir, name + "Counts.csv");
            pwCounts = Generic_StaticIO.getPrintWriter(outFile, false);
            if (writeHeadlines) {
                outFile = Generic_StaticIO.createNewFile(
                        outputDir,
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
            grandTotalWordCounts = new int[words.size()];
            grandTotalArticleCountsForWords = new int[words.size()];
            grandTotalWordCountOnDays = new HashMap<>();
            grandTotalArticleCountsForWordsOnDays = new HashMap<>();
            i = 0;
            ite = words.iterator();
            while (ite.hasNext()) {
                word = ite.next();
                grandTotalWordCounts[i] = 0;
                grandTotalArticleCountsForWords[i] = 0;
                i++;
                grandTotalWordCountOnDays.put(word, new TreeMap<>());
                grandTotalArticleCountsForWordsOnDays.put(word, new TreeMap<>());
            }
            /**
             * Iterate through all the subdirectories in inputDir. It is known
             * that each subdirectory contains a set of HTML files and
             * associated directories. For the purposes of this processing, only
             * the HTML files are processed.
             */
            int[] totalWordCounts;
            int[] totalWordCountsInArticles;
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
                    results = parseHTML(words, input1);
                    /**
                     * Combine the results from parsing this file to the overall
                     * results.
                     */
                    // Process counts.
                    totalWordCounts = (int[]) results[0];
                    totalWordCountsInArticles = (int[]) results[1];
                    i = 0;
                    ite = words.iterator();
                    while (ite.hasNext()) {
                        word = ite.next();
                        grandTotalWordCounts[i] += totalWordCounts[i];
                        grandTotalArticleCountsForWords[i] += totalWordCountsInArticles[i];
                        i++;
                        addToCount(
                                ((TreeMap<String, TreeMap<DayOfWeek, Integer>>) results[2]).get(word),
                                grandTotalWordCountOnDays.get(word));
                        addToCount(
                                ((TreeMap<String, TreeMap<DayOfWeek, Integer>>) results[3]).get(word),
                                grandTotalArticleCountsForWordsOnDays.get(word));

                    }
                    if (writeHeadlines) {
                        // Process dates and headlines writing out a list.
                        TreeSet<DateOutlineDetails> syriaDateHeadlines;
                        syriaDateHeadlines = (TreeSet<DateOutlineDetails>) results[4];
                        Iterator<DateOutlineDetails> ite2;
                        DateOutlineDetails dh;
                        ite2 = syriaDateHeadlines.iterator();
                        while (ite2.hasNext()) {
                            dh = ite2.next();
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
            System.out.print("term, total word count, total article count");
            pwCounts.print("term, total word count, total article count");
//                System.out.println(word + " word count on " + day + " " + i);
//                pw.println(word + " word count on " + day + " " + i);
            TreeMap<DayOfWeek, Integer> grandTotalWordCountOnDay;
            TreeMap<DayOfWeek, Integer> grandTotalArticleCountsForWordsOnDay;
            Iterator<DayOfWeek> ite2;
            DayOfWeek day;
            ite2 = mondayToSaturday.iterator();
            while (ite2.hasNext()) {
                day = ite2.next();
                System.out.print(", word count on " + day);
                pwCounts.print(", word count on " + day);
            }
            ite2 = mondayToSaturday.iterator();
            while (ite2.hasNext()) {
                day = ite2.next();
                System.out.print(", article count on " + day);
                pwCounts.print(", article count on " + day);
            }
            System.out.println();
            pwCounts.println();
            /**
             * Write lines
             */
            i = 0;
            ite = words.iterator();
            while (ite.hasNext()) {
                word = ite.next();
                grandTotalWordCountOnDay = grandTotalWordCountOnDays.get(word);
                grandTotalArticleCountsForWordsOnDay = grandTotalArticleCountsForWordsOnDays.get(word);
//                System.out.println(word + " word count " + grandTotalWordCounts[i]);
//                pwCounts.println(word + " word count " + grandTotalWordCounts[i]);
//                System.out.println(word + " Article count " + grandTotalArticleCountsForWords[i]);
//                pwCounts.println(word + " Article count " + grandTotalArticleCountsForWords[i]);
                System.out.print(word);
                pwCounts.print(word);
                System.out.print(", " + grandTotalWordCounts[i]);
                pwCounts.print(", " + grandTotalWordCounts[i]);
                System.out.print(", " + grandTotalArticleCountsForWords[i]);
                pwCounts.print(", " + grandTotalArticleCountsForWords[i]);
                i++;
                printWordCountOnDay(pwCounts, mondayToSaturday, word, grandTotalWordCountOnDays.get(word));
                printWordCountOnDay(pwCounts, mondayToSaturday, word, grandTotalArticleCountsForWordsOnDays.get(word));
                System.out.println();
                pwCounts.println();
            }
            System.out.println("---------------------------");
            pwCounts.close();
            if (writeHeadlines) {
                pwHeadlines.close();
            }
        }
    }

    void printWordCountOnDay(
            PrintWriter pw,
            ArrayList<DayOfWeek> mondayToSaturday,
            String word,
            TreeMap<DayOfWeek, Integer> grandTotalWordCountOnDay) {
        Iterator<DayOfWeek> ite;
        DayOfWeek day;
        Integer i;
        ite = mondayToSaturday.iterator();
        while (ite.hasNext()) {
            day = ite.next();
            i = grandTotalWordCountOnDay.get(day);
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
                    Title += value;
                }
                if (value.equalsIgnoreCase("c6")) {
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
                        return true;
                    }
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
     * DayOfWeek and values that are counts of the number of times the word
     * "refugee" appears on those days. result[2] is TreeMap with keys that are
     * the DayOfWeek and values that are counts of the number of times the word
     * "syria" appears on those days. result[3] is TreeMap with keys that are
     * the DayOfWeek and values that are counts of the number of times the word
     * "brexit" appears on those days. result[4] is a TreeSet of DateHeadlines
     * which provides the dated and headlines of articles that have mention of
     * "syria" in them.
     *
     * @param words
     * @param input The input file to be parsed.
     * @return
     */
    public Object[] parseHTML(ArrayList<String> words, File input) {
        inArticle = false;
        gotDate = false;
        gotTitle = false;
        Object[] result = new Object[5];
        TreeSet<DateOutlineDetails> syriaDateHeadlines;
        syriaDateHeadlines = new TreeSet<>();
        BufferedReader br;
        br = Generic_StaticIO.getBufferedReader(input);

        Document doc = null;
        try {
            doc = Jsoup.parse(input, "utf-8");
            String title = doc.title();
            //System.out.println(title);
        } catch (IOException ex) {
            Logger.getLogger(Text_Processor2.class.getName()).log(Level.SEVERE, null, ex);
        }

        int n;
        n = words.size();
        int[] totalWordCounts = new int[n];
        result[0] = totalWordCounts;
        int[] totalArticleCountsForWords = new int[n];
        result[1] = totalArticleCountsForWords;
        int[] wordCounts = new int[n];
        int[] articleCountsForWords = new int[n];
        TreeMap<String, TreeMap<DayOfWeek, Integer>> totalWordCountByDay;
        totalWordCountByDay = new TreeMap<>();
        TreeMap<String, TreeMap<DayOfWeek, Integer>> totalArticleCountForWordsByDay;
        totalArticleCountForWordsByDay = new TreeMap<>();
        int i;
        String word;
        Iterator<String> ite2;
        i = 0;
        ite2 = words.iterator();
        while (ite2.hasNext()) {
            word = ite2.next();
            totalWordCounts[i] = 0;
            totalArticleCountsForWords[i] = 0;
            wordCounts[i] = 0;
            articleCountsForWords[i] = 0;
            i++;
            totalWordCountByDay.put(word, new TreeMap<>());
            totalArticleCountForWordsByDay.put(word, new TreeMap<>());
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
                ite2 = words.iterator();
                while (ite2.hasNext()) {
                    word = ite2.next();
                    wordCounts[i] += getWordCount(word, Article);
                    if (wordCounts[i] > 0) {
                        totalArticleCountsForWords[i]++;
                        addToCount(totalArticleCountForWordsByDay.get(word), day, 1);
                    }
                    addToCount(totalWordCountByDay.get(word), day, wordCounts[i]);
                    i++;
                }
                /**
                 * Store DateHeadline's for those articles on Saturdays that
                 * contain the word "syria".
                 */
                if (wordCounts[words.indexOf("syria")] > 0) {
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

                            String url;
                            url = "http://content.guardianapis.com/search?show-fields=newspaperPageNumber%2CnewspaperEditionDate&q="
                                    + title + "&api-key=" + GuardianAPIKey;

                        }

                        syriaDateHeadlines.add(
                                new DateOutlineDetails(
                                        ld,
                                        Section, Length, Title));
                    }
                }
                for (i = 0; i < n; i++) {
                    wordCounts[i] = 0;
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
        result[2] = totalWordCountByDay;
        result[3] = totalArticleCountForWordsByDay;
        result[4] = syriaDateHeadlines;
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
     * For parsing a line. If it is thought to be a Date as it contains some key
     * text that it is assumed all dates have, then the Date is returned
     * otherwise an empty String is returned.
     *
     * @param line
     * @return
     */
    LocalDate parseDate(String s) {
        LocalDate result = null;
        String month;
        String dayOfMonth;
        String year;
        String dayOfWeek;
        String[] split;
        split = s.split(", ");
        String[] split2;
        split2 = split[0].split(" ");
        month = split2[0];
        dayOfMonth = split2[1];
        split2 = split[1].split(" ");
        year = split2[0];
        dayOfWeek = split2[1];
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
                        outlineDetailsComparison = (Section + Length + Headline).compareTo(t.Headline + t.Length + t.Headline);
                        return outlineDetailsComparison;
                    }
                }
            } else {
                return dateComparison;
            }
        }
    }
}
