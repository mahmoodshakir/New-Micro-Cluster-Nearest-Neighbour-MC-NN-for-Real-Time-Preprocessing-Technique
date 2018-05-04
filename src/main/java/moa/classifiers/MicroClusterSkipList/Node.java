/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers.MicroClusterSkipList;

/**
 *
 * @author Mahmood Shakir
 * University of Reading
 * 2016
 */
public class Node {
    int HeadLevel;
    Node down;
    Node up;
    Node left;
    Node right;
    Node Tail;
    Node MedianRight;
    Node MedianLeft;
    Node MedianQ1;
    Node MedianQ3;
    double DataCurrent;
    long TimeStamp = Long.MAX_VALUE; // To be used for analyzing outliers
            
    public Node(double data, int timestamp){
        HeadLevel = 0;
        down = null;
        up = null;
        left = null;
        right = null;
        MedianRight = null;
        MedianLeft = null;
        MedianQ1 = null;
        MedianQ3 = null;
        Tail = null;
        DataCurrent = data;
        TimeStamp = timestamp; // Feature Index // default (-1)
    }
    
    public void setHeadLevel(int level){
        HeadLevel = level;
    }
    
    public int getHeadLevel(){
        return HeadLevel;
    }
    
    public void setMedianQ1(Node n){
       MedianQ1 = n; 
    }
    
    public void setMedianQ3(Node n){
       MedianQ3 = n; 
    }
    
    public void setDown(Node n){
       down = n; 
    }
    
    public void setUp(Node n){
       up = n; 
    }
    
    public void setLeft(Node n){
       left = n; 
    }
    
    public void setRight(Node n){
       right = n; 
    }
    
    public void setMedianRight(Node n){
       MedianRight = n; 
    }
    
    public void setMedianLeft(Node n){
       MedianLeft = n; 
    }
    
    public void setTail(Node n){
       Tail = n; 
    }
    
    public Node getTail(){
        return Tail;
    }
    
    public Node getMedianRight(){
        return MedianRight;
    }
    
    public Node getMedianLeft(){
        return MedianLeft;
    }
    
    public Node getDown(){
        return down;
    }
    
    public Node getUp(){
        return up;
    }
    
    public Node getLeft(){
        return left;
    }
    
    public Node getRight(){
        return right;
    }
    
    public Node getMedianQ1(){
        return MedianQ1;
    }
    
    public Node getMedianQ3(){
        return MedianQ3;
    }
    
    public double getData(){
        return DataCurrent;
    }
    
    public int getTimeStamp(){
        return (int)TimeStamp;
    }
}
