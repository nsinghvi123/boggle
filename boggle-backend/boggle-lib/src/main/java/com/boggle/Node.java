package com.boggle;

import java.util.HashMap;
import java.util.Map;

public class Node {


    private Map<Character, Node> children = new HashMap<Character, Node>();

    public Node(){
    }

    public Map<Character, Node> getChildren(){
        return children;
    }

    public Boolean hasChildren(){
        if (children.isEmpty()){
            return false;
        }
      return true;
    }

    public boolean childHasLetter(char letter){
        return children.containsKey(letter);
    }

    public void createNode(char letter){
        children.put(letter, new Node());
    }


}
