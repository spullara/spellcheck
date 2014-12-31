package spellcheck;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

public class Main {

  static Map<String, Integer> NWORDS;

  static String alphabet = "abcdefghijklmnopqrstuvwxyz";

  static Stream<String> edits(String word) {
    List<String[]> splits = new ArrayList<>();
    for (int i = 0; i < word.length(); i++) {
      splits.add(new String[]{word.substring(0, i), word.substring(i)});
    }
    Stream<String> deletes = splits.stream().filter(p -> p[1].length() > 1).map(p -> p[0] + p[1].substring(1));
    Stream<String> transposes = splits.stream().filter(p -> p[1].length() > 2)
            .map(p -> p[0] + p[1].substring(1, 2) + p[1].substring(0, 1) + p[1].substring(2));
    Stream<String> replaces = splits.stream().filter(p -> p[1].length() > 1)
            .flatMap(p -> alphabet.chars().mapToObj(c -> p[0] + (char) c + p[1].substring(1)));
    Stream<String> inserts = splits.stream().flatMap(p -> alphabet.chars().mapToObj(c -> p[0] + (char) c + p[1]));
    return concat(deletes, concat(transposes, concat(replaces, inserts)));
  }

  static Stream<String> edits2(String word) {
    return edits(word).flatMap(Main::edits);
  }

  static Set<String> known(Stream<String> words) {
    return words.peek(System.out::println).filter(NWORDS::containsKey).collect(toSet());
  }

  private static <T extends Collection<V>, V> T or(T first, Supplier<T> second) {
    return first.isEmpty() ? second.get() : first;
  }

  static Collection<String> correct(String word) {
    Set<String> suggestions = new TreeSet<>((s1, s2) -> s1.length() - s2.length());
    suggestions.addAll(or(known(Stream.of(word)),
            () -> or(known(edits(word)),
                    () -> or(known(edits2(word)),
                            () -> asList(word)))));
    return suggestions;
  }

  public static void main(String[] args) throws IOException {
    NWORDS = Files.lines(new File(args[0]).toPath())
            .map(String::toLowerCase)
            .filter((s) -> s.matches("[a-z]+"))
            .collect(toMap((s) -> s, (s) -> 1, (s1, s2) -> 1));

    String line;
    while ((line = System.console().readLine()) != null) {
      System.out.println(correct(line.trim()));
    }
  }
}