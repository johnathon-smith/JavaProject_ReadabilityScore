package readability;

import java.util.Scanner;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors

public class Main {
    public static void main(String[] args) {

        String text = "";

        try (Scanner scanner = new Scanner(new File(args[0]))) {
            while(scanner.hasNext()) {
                text = text + scanner.nextLine();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }

        //Split the string into sentences and count them
        double numSentences = text.split("[.?!]").length;

        //Split the string into words and count them
        String[] words = text.split(" ");
        double numWords = words.length;

        //Count the number of syllables
        double numSyllables = countSyllables(words);

        //Syllables per word
        double syllablesPerWord = numSyllables / numWords;

        //Count the number of Polysyllables
        double numPolysyllables = countPolysyllables(words);

        //Count the characters
        double numChars = text.replaceAll("[ \t\n]", "").length();

        //Words per Sentence
        double wordsPerSentence = numWords / numSentences;

        //Characters per word
        double charsPerWord = numChars / numWords;

        //average number of chars per 100 words
        double charsPer100Words = numChars / numWords * 100;

        //average number of sentences per 100 words
        double sentencesPer100Words = numSentences / numWords * 100;

        //Print out necessary info
        System.out.printf("Words: %d\n", (int) numWords);
        System.out.printf("Sentences: %d\n", (int) numSentences);
        System.out.printf("Characters: %d\n", (int) numChars);
        System.out.printf("Syllables: %d\n", (int) numSyllables);
        System.out.printf("Polysyllables: %d\n", (int) numPolysyllables);

        //Determine which score the user wants to see
        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
        Scanner scanner = new Scanner(System.in);
        String userChoice = scanner.nextLine();
        System.out.println("\n");

        if (userChoice.equals("ARI")) {
            double ARI_score = getAriScore(charsPerWord, wordsPerSentence);
            long upperAge = getUpperAge(ARI_score);
            System.out.printf("Automated Readability Index: %.2f (about %d-year-olds).\n", ARI_score, upperAge);
        }

        if (userChoice.equals("FK")) {
            double FK_score = getFleschKincaidScore(wordsPerSentence, syllablesPerWord);
            long upperAge = getUpperAge(FK_score);
            System.out.printf("Flesch-Kincaid Readability tests: %.2f (about %d-year-olds).\n", FK_score, upperAge);
        }

        if (userChoice.equals("SMOG")) {
            double SMOG_score = getSmogScore(numPolysyllables, numSentences);
            long upperAge = getUpperAge(SMOG_score);
            System.out.printf("Simple Measure of Gobbledygook: %.2f (about %d-year-olds).\n", SMOG_score, upperAge);
        }

        if (userChoice.equals("CL")) {
            double CL_score = getColemanLiauScore(charsPer100Words, sentencesPer100Words);
            long upperAge = getUpperAge(CL_score);
            System.out.printf("Coleman-Liau index: %.2f (about %d-year-olds).\n", CL_score, upperAge);
        }

        if (userChoice.equals("all")) {
            double ARI_score = getAriScore(charsPerWord, wordsPerSentence);
            long ariUpperAge = getUpperAge(ARI_score);
            System.out.printf("Automated Readability Index: %.2f (about %d-year-olds).\n", ARI_score, ariUpperAge);

            double FK_score = getFleschKincaidScore(wordsPerSentence, syllablesPerWord);
            long fkUpperAge = getUpperAge(FK_score);
            System.out.printf("Flesch-Kincaid Readability tests: %.2f (about %d-year-olds).\n", FK_score, fkUpperAge);

            double SMOG_score = getSmogScore(numPolysyllables, numSentences);
            long smogUpperAge = getUpperAge(SMOG_score);
            System.out.printf("Simple Measure of Gobbledygook: %.2f (about %d-year-olds).\n", SMOG_score, smogUpperAge);

            double CL_score = getColemanLiauScore(charsPer100Words, sentencesPer100Words);
            long clUpperAge = getUpperAge(CL_score);
            System.out.printf("Coleman-Liau index: %.2f (about %d-year-olds).\n", CL_score, clUpperAge);

            //Get the average age and leave final message
            double averageAge = (ariUpperAge + fkUpperAge + smogUpperAge + clUpperAge) / 4.0;
            System.out.printf("This text should be understood in average by %.2f-year-olds.", averageAge);
        }

    }

    public static double getAriScore(double charsPerWord, double wordsPerSentence) {
        return 4.71 * charsPerWord + 0.5 * wordsPerSentence - 21.43;
    }

    public static double getFleschKincaidScore(double wordsPerSentence, double syllablesPerWord) {
        return 0.39 * wordsPerSentence + 11.8 * syllablesPerWord - 15.59;
    }

    public static double getSmogScore(double numPolysyllables, double numSentences) {
        return 1.043 * Math.sqrt(numPolysyllables * 30 / numSentences) + 3.1291;
    }

    public static double getColemanLiauScore(double charsPer100Words, double sentencesPer100Words) {
        return 0.0588 * charsPer100Words - 0.296 * sentencesPer100Words - 15.8;
    }

    public static long getUpperAge(double score) {
        //create vars for age bracket
        //long lowerAge = 0;
        long upperAge = 0;

        //Get the rounded score for the next part
        int roundedScore = (int) Math.ceil(score);

        //Find the appropriate age bracket
        if (roundedScore < 14 && roundedScore != 11) {
            //lowerAge = roundedScore + 4;
            upperAge = roundedScore + 5;
        } else {
            //lowerAge = 18;
            upperAge = 22;
        }

        return upperAge;
    }

    public static double countSyllables(String[] words) {
        /* Use the following rules to count the number of syllables in each word
            - Add one for each vowel, except 'y'
            - Add one every time a word ends with 'y'
            - Subtract one every time two vowels are next to each other ('aa', 'ou', 'ee', etc)
            - Subtract one for each silent vowel, like the 'e' in 'move'
            - If the word has no vowels, consider it one syllable.
        */

        double totalSyllables = 0;
        double syllablesInWord;

        //Loop through each word in the array and count the number of syllables based on the rules above
        for (String word : words) {
            //reset syllablesInWord
            syllablesInWord = 0;
            //Don't check the last char in the loop
            for (int i = 0; i < word.length(); i++) {
                //check if the current char is a vowel, add 1
                if (isVowel(word.toLowerCase().charAt(i))) {
                    syllablesInWord++;
                    //if the next char is also a vowel, subtract 1
                    //Skip if this is the last letter of the word
                    if (i != word.length() - 1 && isVowel(word.toLowerCase().charAt(i + 1))) {
                        syllablesInWord--;
                    }
                }
            }

            //if last letter is 'e', preceded by a consonant, subtract 1
            if (word.toLowerCase().endsWith("e") && !isVowel(word.toLowerCase().charAt(word.length() - 2))) {
                syllablesInWord--;
            }

            //if last letter is 'y', preceded by a consonant, add 1
            if (word.toLowerCase().endsWith("y") && !isVowel(word.toLowerCase().charAt(word.length() - 2))) {
                syllablesInWord++;
            }

            //If there are no syllables, consider it to have just one.
            if (syllablesInWord == 0) {
                syllablesInWord = 1;
            }

            totalSyllables += syllablesInWord;
        }

        return totalSyllables;
    }

    public static double countPolysyllables(String[] words) {
        /* Use the following rules to count the number of syllables in each word
            - Add one for each vowel, except 'y'
            - Add one every time a word ends with 'y'
            - Subtract one every time two vowels are next to each other ('aa', 'ou', 'ee', etc)
            - Subtract one for each silent vowel, like the 'e' in 'move'
            - If the word has no vowels, consider it one syllable.

           Return the count of words that have more than two syllables
        */

        double totalPolysyllables = 0;
        double syllablesInWord;

        //Loop through each word in the array and count the number of syllables based on the rules above
        for (String word : words) {
            //reset syllablesInWord
            syllablesInWord = 0;
            //Don't check the last char in the loop
            for (int i = 0; i < word.length(); i++) {
                //check if the current char is a vowel, add 1
                if (isVowel(word.toLowerCase().charAt(i))) {
                    syllablesInWord++;
                    //if the next char is also a vowel, subtract 1
                    //Skip if this is the last letter of the word
                    if (i != word.length() - 1 && isVowel(word.toLowerCase().charAt(i + 1))) {
                        syllablesInWord--;
                    }
                }
            }

            //if last letter is 'e', preceded by a consonant, subtract 1
            if (word.toLowerCase().endsWith("e") && !isVowel(word.toLowerCase().charAt(word.length() - 2))) {
                syllablesInWord--;
            }

            //if last letter is 'y', preceded by a consonant, add 1
            if (word.toLowerCase().endsWith("y") && !isVowel(word.toLowerCase().charAt(word.length() - 2))) {
                syllablesInWord++;
            }

            //If there are no syllables, consider it to have just one.
            if (syllablesInWord == 0) {
                syllablesInWord = 1;
            }

            if (syllablesInWord > 2) {
                totalPolysyllables++;
            }
        }

        return totalPolysyllables;
    }

    public static boolean isVowel(char letter) {
        //Don't include 'y' as a vowel in this case
        if (letter == 'a' || letter == 'e' || letter == 'i' || letter == 'o' || letter == 'u') {
            return true;
        }
        return false;
    }
}
