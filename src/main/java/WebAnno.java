import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

/**
 * WebAnno Extractor for CoNLL 2002 format
 * @author Maxim Serebrianski
 */
class WebAnno {

    public static void genOpenNlp(File in, boolean annotate) {

        String line;
        String[] splitLine;
        String[] annoSplit;
        boolean annotation = false;
        String fileName;
        File out;
        if (annotate) {
            fileName = in.getName().replace("%26", "&").replace("%2520", " ").replace(".conll", ".an");
            out = new File("src/main/resources/training/annotations/annotated/" + fileName);
        } else {
            fileName = in.getName().replace("%26", "&").replace("%2520", " ").replace(".conll", ".cl");
            out = new File("src/main/resources/training/annotations/clean/" + fileName);
        }
        StringBuilder sb = new StringBuilder();

        System.out.println("Processing " + "\"" + in.getName() + "\"");

        try (BufferedReader br = new BufferedReader(new FileReader(in)); BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    bw.write(sb.toString().trim());
                    sb.setLength(0);
                    bw.newLine();
                } else {
                    splitLine = line.split(" ");
                    if (annotate) {
                        if (splitLine[1].equals("O")) {
                            if (annotation) {
                                sb.append("<END> ");
                                annotation = false;
                            }
                            sb.append(splitLine[0]).append(" ");
                        } else {
                            annoSplit = splitLine[1].split("-");
                            if (annoSplit[0].equals("B")) {
                                annotation = true;
                                sb.append("<START:").append(annoSplit[1]).append("> ").append(splitLine[0]).append(" ");
                            }
                            if (annoSplit[0].equals("I")) {
                                annotation = true;
                                sb.append(splitLine[0]).append(" ");
                            }
                        }
                    } else {
                        sb.append(splitLine[0]).append(" ");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        postProcessFile(out, annotate);  //Trims file to Part I only (as best as possible) and corrects some punctuation errors
        System.out.println("-----------------------------------------------");
    }

    public static void genStanfordNlp(File in, boolean annotate) {
        //TODO
    }

    private static void postProcessFile(File in, boolean annotate) {
        //System.out.println(in.getPath());
        String line;
        boolean start = false;
        int lineCounter = 0;
        String fileName;
        File out;
        if (annotate) {
            fileName = in.getName().replace(".an", "-p1.an");
            out = new File("src/main/resources/training/annotations/annotated/" + fileName);
        } else {
            fileName = in.getName().replace(".cl", "-p1.cl");
            out = new File("src/main/resources/training/annotations/clean/" + fileName);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(in)); BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
            while ((line = br.readLine()) != null) {
                lineCounter++;
                if (line.equals(".")) {
                    continue;
                }
                if (line.toLowerCase().contains("item 1")){
                    start = true;
                }
                if (start) {
                    bw.write(line);
                    bw.newLine();
                }
                if (line.toLowerCase().contains("mine safety disclosures") && lineCounter > 150) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.delete(Paths.get(in.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runStatistics(File in) {
        String line;
        int tokens = 0;
        int lines = 0;
        int annotations = 0;
        int goods = 0;
        int assets = 0;
        int services = 0;
        String[] splitLine;
        String[] annoSplit;

        System.out.println("Processing " + in.getName());
        System.out.println();

        try (BufferedReader br = new BufferedReader(new FileReader(in))) {
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    tokens += 1;
                    splitLine = line.split(" ");
                    annoSplit = splitLine[1].split("-");
                    if (annoSplit[0].equals("B")) {
                        annotations += 1;
                        switch (annoSplit[1]) {
                            case "GOODS":
                                goods += 1;
                                break;
                            case "ASSET":
                                assets += 1;
                                break;
                            case "SERVICE":
                                services += 1;
                                break;
                        }
                    }
                } else {
                    lines += 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Tokens: " + tokens);
        System.out.println("Lines: " + lines);
        System.out.println("Annotations (total): " + annotations);
        if (annotations != 0) {
            System.out.println("Goods: " + goods + " (" + Double.parseDouble(new DecimalFormat("##.##").format((double) goods / annotations * 100)) + "%)");
            System.out.println("Assets: " + assets + " (" + Double.parseDouble(new DecimalFormat("##.##").format((double) assets / annotations * 100)) + "%)");
            System.out.println("Services: " + services + " (" + Double.parseDouble(new DecimalFormat("##.##").format((double) services / annotations * 100)) + "%)");
        }
        System.out.println("-----------------------------------------------");
    }
}