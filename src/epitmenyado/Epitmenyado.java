package epitmenyado;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Epitmenyado {

    public static void main(String[] args) throws IOException {
        new Epitmenyado().make();
    }

    private static class Data {

        String streetName;
        String streetNo;
        char priceCat;
        int squareMeter;

        public Data(String streetName, String streetNo, char priceCat, int squareMeter) {
            this.streetName = streetName;
            this.streetNo = streetNo;
            this.priceCat = priceCat;
            this.squareMeter = squareMeter;
        }
    }

    List<Integer> prices;
    Map<Integer, List<Data>> dataMap = new HashMap<>();

    private void make() throws IOException {
        readData();
        writeNumberOfProperties();
        searchForProperties();
        countLevels();
        writeMixedStreets();
        saveTaxes();
    }

    private void readData() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("utca.txt"));
        String[] firstLine = lines.get(0).trim().split(" ");
        prices = Arrays.stream(firstLine).map(Integer::valueOf).collect(Collectors.toList());
        IntStream.range(1, lines.size()).mapToObj(lines::get).forEach((String line) -> {
            String[] items = line.trim().split(" ");
            Data data = new Data(items[1], items[2], items[3].charAt(0), Integer.valueOf(items[4]));
            dataMap.computeIfAbsent(Integer.valueOf(items[0]), k -> new ArrayList<>()).add(data);
        });
    }

    private void writeNumberOfProperties() {
        int numOfProperties = dataMap.values().stream().mapToInt(List::size).sum();
        System.out.println("2. feladat. A mintában " + numOfProperties + " telek szerepel.");
    }

    private void searchForProperties() {
        System.out.print("3. feladat. Egy tulajdonos adószáma:");
        Scanner in = new Scanner(System.in);
        int input = in.nextInt();
        List<Data> properties = dataMap.get(input);
        if (properties == null) {
            System.out.println("Nem szerepel az adatállományban.");
        } else {
            properties.forEach(p -> System.out.println(p.streetName + " utca " + p.streetNo));
        }
    }

    private int ado(char level, int squareMeter) {
        int ado = prices.get(level - 'A') * squareMeter;
        return ado < 10_000 ? 0 : ado;
    }

    private void countLevels() {
        System.out.println("5. feladat");
        "ABC".chars().mapToObj(c -> String.valueOf((char) c)).forEach(level -> {
            long count = filterByPriceCat(level).count();
            long sum = filterByPriceCat(level).mapToInt(data -> ado(data.priceCat, data.squareMeter)).sum();
            System.out.println(level + " sávba " + count + " telek esik, az adó " + sum + " Ft. ");
        });
    }

    private Stream<Data> filterByPriceCat(String level) {
        return dataMap.values().stream().flatMap(List::stream).filter(d -> d.priceCat == level.charAt(0));
    }

    private void writeMixedStreets() {
        System.out.println("6. feladat. A több sávba sorolt utcák:");
        dataMap.values().stream().flatMap(List::stream).map(d -> d.streetName).distinct().filter(street -> {
            long priceCatsInOneStreet = dataMap.values().stream().flatMap(List::stream)
                    .filter(d -> d.streetName.equals(street)).map(data -> data.priceCat).distinct().count();
            return priceCatsInOneStreet > 1;
        }).forEach(System.out::println);
    }

    private void saveTaxes() throws IOException {
        Stream<String> linesToSave = dataMap.entrySet().stream()
                .map(entry -> entry.getKey() + " " + 
                        entry.getValue().stream().mapToInt(i -> ado(i.priceCat, i.squareMeter)).sum());
        Files.write(Paths.get("fizetendo.txt"), (Iterable<String>)linesToSave::iterator);
    }
}
