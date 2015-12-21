import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.util.Scanner;

/**
 * Main class for parsing the report and initializing the NLP pipeline
 * @author Maxim Serebrianski
 */
class Main {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        File dir = new File("src/main/resources/training/annotations");
        FileFilter fileFilter = new WildcardFileFilter("*.conll");

        System.out.println("Enter '1' to generate unannotated training data from 10-K reports.");
        System.out.println("Enter '2' to generate annotated training data for OpenNLP from exported WebAnno files.");
        System.out.println("Enter '3' to generate clean data for OpenNLP from exported WebAnno files.");
        System.out.println("Enter '4' to generate statistics from exported WebAnno files.");
        System.out.println("Enter '5' to create file for model training.");
        System.out.println("--------------------------------------------------------------------------");

        switch (sc.nextLine()) {
            case "1":
                System.out.println("--------------------------------------------------------------------------");
                processReports("src/main/resources/training/reports/reports.txt", true);
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
                //OpenNLP.createTrainingFile();
                File path = new File("src/main/resources/annotations/folder");
                if (path.isDirectory()) {
                    System.out.println("is directory!");
                }
                File[] f = dir.listFiles();
                System.out.println(f.length);
        }
    }

    private static void processReports(String inPath, boolean trainOnly) {
        String[] split;
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(inPath))) {
            while ((line = br.readLine()) != null) {
                split = line.split(" -> ");
                System.out.println("Processing " + split[1]);
                Document document = Jsoup.parse(new URL(split[1]).openStream(), "UTF-8", split[1]);
                String text = document.body().text();
                if (trainOnly) {
                    OpenNLP.runPipeline(text, new File("src/main/resources/training/" + split[0] + ".txt"), true);
                } else {
                    OpenNLP.runPipeline(text, null, false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}