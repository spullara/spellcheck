package spellcheck;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.concat;

public class Main {

  static String alphabet = "abcdefghijklmnopqrstuvwxyz";

  static Stream<String> edits(String word) {
    List<String[]> splits = IntStream.range(0, word.length())
            .mapToObj(i -> new String[]{word.substring(0, i), word.substring(i)}).collect(toList());
    return concat(splits.stream().filter(p -> p[1].length() > 1).map(p -> p[0] + p[1].substring(1)), // deletes
            concat(splits.stream().filter(p -> p[1].length() > 2)
                            .map(p -> p[0] + p[1].substring(1, 2) + p[1].substring(0, 1) + p[1].substring(2)), // inserts
                    concat(splits.stream().filter(p -> p[1].length() > 1)
                                    .flatMap(p -> alphabet.chars().mapToObj(c -> p[0] + (char) c + p[1].substring(1))), // transposes
                            splits.stream().flatMap(p -> alphabet.chars().mapToObj(c -> p[0] + (char) c + p[1]))))); //inserts
  }

  static Stream<String> edits2(String word) { return edits(word).flatMap(Main::edits); }

  static Set<String> known(Stream<String> words) { return words.filter(NWORDS::containsKey).collect(toSet()); }

  static <T extends Collection<V>, V> T or(T first, Supplier<T> second) { return first.isEmpty() ? second.get() : first; }

  static Collection<String> correct(String word) {
    Set<String> suggestions = new TreeSet<>((s1, s2) -> s1.length() - s2.length());
    suggestions.addAll(or(known(Stream.of(word)),
            () -> or(known(edits(word)),
                    () -> or(known(edits2(word)),
                            () -> asList(word)))));
    return suggestions;
  }

  static Map<String, Integer> NWORDS;

  public static void main(String[] args) throws IOException {
    NWORDS = Files.lines(new File(args[0]).toPath())
            .map(String::toLowerCase)
            .filter((s) -> s.matches("[a-z]+"))
            .collect(toMap(s -> s, s -> 1, (s1, s2) -> 1));

    String line;
    while ((line = System.console().readLine()) != null) {
      System.out.println(correct(line.trim()));
    }
  }
}