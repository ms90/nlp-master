import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Main class for parsing the report and initializing the NLP pipeline
 * @author Maxim Serebrianski
 */
class Main {
    static int foldCounter = 1;

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        File dir = new File("src/main/resources/training/exported");
        FileFilter fileFilter = new WildcardFileFilter("*.conll");

        System.out.println("Enter '1' to generate unannotated training data from 10-K reports.");
        System.out.println("Enter '2' to generate annotated training data for OpenNLP from exported WebAnno files.");
        System.out.println("Enter '3' to generate clean data for OpenNLP from exported WebAnno files.");
        System.out.println("Enter '4' to generate statistics from exported WebAnno files.");
        System.out.println("Enter '5' to copy annotation files into stratified folds.");
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
                    WebAnno.genOpenNlp(f, true);
                }
                System.out.println("Done!");
                break;

            case "3":
                System.out.println("--------------------------------------------------------------------------");
                File[] f2 = dir.listFiles(fileFilter);
                for (File f : f2) {
                    WebAnno.genOpenNlp(f, false);
                }
                System.out.println("Done!");
                break;

            case "4":
                System.out.println("--------------------------------------------------------------------------");
                File[] f3 = dir.listFiles(fileFilter);
                for (File f : f3) {
                    WebAnno.runStatistics(f);
                }
                System.out.println("Done!");
                break;

            case "5":
                System.out.println("--------------------------------------------------------------------------");
                createFolds();
                System.out.println("Done!");
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
        File path = new File("src/main/resources/training/annotated");

        FileFilter directoryFilter = File::isDirectory;

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
                Files.copy(file.toPath(), new File("src/main/resources/training/folds/" + foldCounter + "/" + file.getName()).toPath());
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
        //System.out.println("----------------");
        //System.out.println("Fold: " + new File("src/main/resources/training/folds/" + foldCounter).getName()
        //        + " Size: " + new File("src/main/resources/training/folds/" + foldCounter).list().length);
        if (new File("src/main/resources/training/folds/" + foldCounter).list().length >= 10) {
            //System.out.println("Fold " + foldCounter + " full!");
            if (foldCounter <10) {
                foldCounter++;
            } else {
                foldCounter = 1;
            }
            findNextFold();
        }
    }
}