package spellcheck;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;

public class Main {

  static Stream<String> edit(String w) {
    String alphabet = "abcdefghijklmnopqrstuvwxyz";
    int l = w.length();
    Stream<String> deletes = range(0, l).filter(i -> l - i > 1).mapToObj(i -> w.substring(0, i) + w.substring(i + 1));
    Stream<String> transposes = range(0, l).filter(i -> l - i > 2)
            .mapToObj(i -> w.substring(0, i) + w.substring(i + 1, i + 2) + w.substring(i, i + 1) + w.substring(i + 2));
    Stream<String> replaces = range(0, l).filter(i -> l - i > 1).boxed()
            .flatMap(i -> alphabet.chars().mapToObj(c -> w.substring(0, i) + (char)c + w.substring(i + 1)));
    Stream<String> inserts = range(0, l).boxed()
            .flatMap(i -> alphabet.chars().mapToObj(c -> w.substring(0, i) + (char)c + w.substring(i)));
    return concat(deletes, concat(transposes, concat(replaces, inserts)));
  }

  static Stream<String> edits(String word) {
    return edit(word).flatMap(Main::edit);
  }

  static Set<String> known(Stream<String> words) {
    return words.filter(NWORDS::contains).collect(toSet());
  }

  static <T extends Collection<V>, V> T or(T f, Supplier<T> s) {
    return f.isEmpty() ? s.get() : f;
  }

  static Collection<String> correct(String word) {
    return or(known(Stream.of(word)),
            () -> or(known(edit(word)),
                    () -> or(known(edits(word)),
                            () -> asList(word))));
  }

  static Set<String> NWORDS;

  public static void main(String[] args) throws IOException {
    NWORDS = Files.lines(new File(args[0]).toPath())
            .map(String::toLowerCase)
            .filter((s) -> s.matches("[a-z]+"))
            .collect(toSet());

    String line;
    while ((line = System.console().readLine()) != null) {
      System.out.println(correct(line.trim()));
    }
  }
}