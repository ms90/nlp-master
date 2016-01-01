import opennlp.tools.util.eval.FMeasure;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

/**
 * Main class for parsing the report and initializing the NLP pipeline
 * @author Maxim Serebrianski
 */
class Main {
    private static int foldCounter = 1;
    private static final FileFilter directoryFilter = File::isDirectory;

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        File dir = new File("src/main/resources/training/conll");
        FileFilter fileFilter = new WildcardFileFilter("*.conll");
        File path;
        File[] dirs;
        File[][] files;
        ArrayList<File> trainFiles;
        int i = 0;
        ArrayList<FMeasure> measures;
        Double pre = 0.0;
        Double rec = 0.0;
        Double fm = 0.0;

        System.out.println("Enter '1' to generate unannotated training data from 10-K reports.");
        System.out.println("Enter '2' to generate annotated training data for OpenNLP from exported WebAnno files.");
        System.out.println("Enter '3' to generate statistics from exported WebAnno files.");
        System.out.println("Enter '4' to copy annotation files into stratified folds (for OpenNLP).");
        System.out.println("Enter '5' to evaluate OpenNLP model in global setting.");
        System.out.println("Enter '6' to evaluate OpenNLP model per sector.");
        System.out.println("--------------------------------------------------------------------------");

        switch (sc.nextLine()) {
            case "1":
                System.out.println("--------------------------------------------------------------------------");
                processReports();
                System.out.println("Done!");
                break;

            case "2":
                System.out.println("--------------------------------------------------------------------------");
                File[] f1 = dir.listFiles(fileFilter);
                for (File f : f1) {
                    WebAnno.genOpenNlp(f);
                }
                System.out.println("Done!");
                break;

            case "3":
                System.out.println("--------------------------------------------------------------------------");
                File[] f3 = dir.listFiles(fileFilter);
                for (File f : f3) {
                    WebAnno.runStatistics(f);
                }
                System.out.println("Done!");
                break;

            case "4":
                System.out.println("--------------------------------------------------------------------------");
                System.out.println("Copying files to folds...");
                createFolds();
                System.out.println("Done!");
                break;

            case "5":
                System.out.println("--------------------------------------------------------------------------");
                path = new File("src/main/resources/training/onlp/folds");
                dirs = path.listFiles(directoryFilter);
                files = new File[10][];
                measures = new ArrayList<>();

                for (File folder : dirs) {
                    files[i] = folder.listFiles();
                    i++;
                }

                for (int j=0; j<10; j++) {
                    trainFiles = new ArrayList<>();
                    for (File[] f : files) {
                        if (!Arrays.equals(f, files[j])){
                            Collections.addAll(trainFiles, f);
                        }
                    }
                    Collections.addAll(measures, OpenNLP.evaluate(files[j], trainFiles.toArray(new File[trainFiles.size()]), j+1, true));
                    System.out.println("--------------------------------------------------------------------------");
                }

                for (FMeasure m : measures) {
                    pre += m.getPrecisionScore();
                    rec += m.getRecallScore();
                    fm += m.getFMeasure();
                }

                System.out.println("Global Precision: " + pre/measures.size());
                System.out.println("Global Recall: " + rec/measures.size());
                System.out.println("Global F-Measure: " + fm/measures.size());
                System.out.println("--------------------------------------------------------------------------");
                System.out.println("Done!");
                break;

            //NOT FUNCTIONAL YET!
            case "6":
                System.out.println("--------------------------------------------------------------------------");
                path = new File("src/main/resources/training/onlp/annotated");
                dirs = path.listFiles(directoryFilter);
                files = new File[10][];
                measures = new ArrayList<>();
                ArrayList<File> evalFiles;

                for (File folder : dirs) {
                    files[i] = folder.listFiles();
                    i++;
                }

                for (File[] f : files) {
                    trainFiles = new ArrayList<>();
                    evalFiles = new ArrayList<>();
                    if (f.length>=10) {
                        //TODO
                        Collections.addAll(measures, OpenNLP.evaluate(evalFiles.toArray(new File[evalFiles.size()]), trainFiles.toArray(new File[trainFiles.size()]), 0, false));
                    }
                }

                for (FMeasure m : measures) {
                    pre += m.getPrecisionScore();
                    rec += m.getRecallScore();
                    fm += m.getFMeasure();
                }

                System.out.println("Sector Precision: " + pre/measures.size());
                System.out.println("Sector Recall: " + rec/measures.size());
                System.out.println("Sector F-Measure: " + fm/measures.size());
                System.out.println("--------------------------------------------------------------------------");
                System.out.println("Done!");
                break;

        }
    }

    private static void processReports() {
        String[] split;
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/training/reports.txt"))) {
            while ((line = br.readLine()) != null) {
                split = line.split(" -> ");
                System.out.println("Processing " + split[1]);
                Document document = Jsoup.parse(new URL(split[1]).openStream(), "UTF-8", split[1]);
                String text = document.body().text();
                OpenNLP.preProcess(text, new File("src/main/resources/training/" + split[0] + ".txt"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFolds() throws IOException {
        File path = new File("src/main/resources/training/onlp/annotated");
        File[] dirs = path.listFiles(directoryFilter);
        File[][] files = new File[10][];
        int i = 0;

        for (File dir : dirs) {
            files[i] = dir.listFiles();
            i++;
        }

        Arrays.sort(files, (left, right) -> Integer.compare(left.length, right.length));

        for (File[] f : files) {
            foldCounter = 1;
            for (File file : f) {
                findNextFold();
                Files.copy(file.toPath(), new File("src/main/resources/training/onlp/folds/" + foldCounter + "/" + file.getName()).toPath());
                //System.out.println("Copy " + file.getName() + " to Fold " + foldCounter);
                if (foldCounter <10) {
                    foldCounter++;
                } else {
                    foldCounter = 1;
                }
            }
        }
    }

    private static void findNextFold() {
        if (new File("src/main/resources/training/onlp/folds/" + foldCounter).list().length >= 10) {
            if (foldCounter <10) {
                foldCounter++;
            } else {
                foldCounter = 1;
            }
            findNextFold();
        }
    }
}