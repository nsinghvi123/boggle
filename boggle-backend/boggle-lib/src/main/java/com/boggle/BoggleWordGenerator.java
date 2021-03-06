package com.boggle;

import java.io.IOException;
import java.util.*;

public class BoggleWordGenerator {

    public static HashSet<String> generateWords(char[][] board) throws IOException {
        HashSet<String> finalWordSet = new HashSet<>();
        List<String> finalWordList = new ArrayList<>();
        List<String> wordsCreatedList = new ArrayList<>();
        Trie newTrie = Trie.createTrie("./boggle-lib/src/main/java/com/boggle/popularWords.txt");
        FullWordCalculator newFullWordCalculator = new FullWordCalculator("./boggle-lib/src/main/java/com/boggle/popularWords.txt");
        for (int i = 0; i < 5; i++){
            for (int a = 0; a < 5; a++){
                if (board[i][a] != '\0'){
                    BoggleLetter newBogLetter = new BoggleLetter(i, a, board[i][a]);
                    wordsCreatedList = explore(newBogLetter, "", board, wordsCreatedList, newFullWordCalculator, newTrie);
                    finalWordList.addAll(wordsCreatedList);
                }
            }
        }
        finalWordSet.addAll(finalWordList);
        return finalWordSet;
    }



    public static List<String> explore(BoggleLetter boggleLetter, String path, char board[][], List<String> wordsCreatedList, FullWordCalculator fullWordCalculator, Trie newTrie) {
        // when i start, i explore the current letter
        path += boggleLetter.getVal();
        // figures out all the accessible options to the current letter
        List<BoggleLetter> lettersToExplore = boggleLetter.adjacentLetters(board);
        // checks if the word is a prefix
        Boolean isPrefix = newTrie.isPrefix(path);
        // checks if the word is a full word
        Boolean isWordFull = fullWordCalculator.checkIsWordFull(path);

        if (isWordFull){
            wordsCreatedList.add(path);
        }
        if (isPrefix) {
            for (int i = 0; i < lettersToExplore.size(); i++) {
                explore(lettersToExplore.get(i), path, board, wordsCreatedList, fullWordCalculator, newTrie);
            }
        }
        return wordsCreatedList;
    }
}

    // base case: it not a prefix, not call explore and return something
    // 2nd case: if it is a full word, then print it out (refer to that function)
    // 3rd case: find all the adjacent letters and call explore






