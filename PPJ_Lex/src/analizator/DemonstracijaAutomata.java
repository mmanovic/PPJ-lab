package analizator;

import java.util.Scanner;

/**
 * Klasa koja sadrzi main metodu gdje u standardni ulaz se upise regularni izraz
 * i zatim metoda ispise automat.
 *
 * @author Mato Manovic
 * @version 1.0
 */
public class DemonstracijaAutomata {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Automat automat = new Automat("ab$ccc");
        // System.out.println(automat);
        System.out.println(automat.isValidInput("ab$ccc").getMatchLength());
        // System.out.println(automat);

        automat = new Automat("ab\\$ccc");
        System.out.println(automat.isValidInput("ab$ccc").getMatchLength());

        automat = new Automat("ab\\\\$ccc");
        System.out.println(automat.isValidInput("ab\\$ccc").getMatchLength());

        System.out.println(automat.isValidInput("ab\\ccc").getMatchLength());

        // System.out.println(automat.isValidInput("caaaaabbbbb").getMatchLength());
        // System.out.println(automat.isValidInput("caaaaab").getMatchLength());
        // System.out.println(automat.isValidInput("cb").getMatchLength());
        // System.out.println(automat.isValidInput("cc").getMatchLength());
        // System.out.println(automat.isValidInput("cd").getMatchLength());
        // System.out.println(automat.isValidInput("c").getMatchLength());
        // System.out.println(automat.isValidInput("caa").getMatchLength());
        // System.out.println(automat.isValidInput("d").getMatchLength());
        // System.out.println(automat.isValidInput("caaaaaaaaaaaaaab").isFullyMatched());

    }
}
