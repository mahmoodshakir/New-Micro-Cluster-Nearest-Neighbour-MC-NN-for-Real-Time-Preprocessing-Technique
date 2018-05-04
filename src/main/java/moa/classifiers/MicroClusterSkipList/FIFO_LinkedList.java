/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers.MicroClusterSkipList;
import java.util.LinkedList;

/**
 *
 * @author Mahmood Shakir
 * University of Reading
 * 2016
 */
public class FIFO_LinkedList {
    LinkedList<Double> linkedlist = new LinkedList<Double>();
    
    public FIFO_LinkedList(){
        resetFIFO();
    }
    
    public void resetFIFO(){
        linkedlist.clear();
    }
    
    public void addNode(double data){
        linkedlist.add(data);
    }
    
    public void deleteFirstNode(){
        linkedlist.removeFirst();
    }
    
    public double getFirstNode(){
        return (double)linkedlist.getFirst();
    }
    
    public int getSize(){
        return (int)linkedlist.size();
    }
    
    public void printNodes(){
        System.out.println(" ");
        for(int i=0; i<linkedlist.size(); i++)
            System.out.print(linkedlist.get(i)+" ");
    }
    
    public Object[] getNodes(){
        Object[] array = linkedlist.toArray(new Object[linkedlist.size()]);
        return (Object[])array;
    }
    
    public double[] getNodes_double(){
        
        double[] array = new double[linkedlist.size()];
        
         for(int i=0; i<linkedlist.size(); i++)
            array[i] = (double)linkedlist.get(i);
         
        return array;
    }
    
    public static void main(String args[]) {
        FIFO_LinkedList FIFO = new FIFO_LinkedList();
        double[] featureValue = {9,1,5,1,8,2,6,3,9,7,0,4,6};
        int totalSize = 5;
        for(int i=0; i<featureValue.length; i++){
            //if(FIFO.getSize()>totalSize)
            //    FIFO.deleteFirstNode();
            FIFO.addNode(featureValue[i]);
            //FIFO.printNodes();
        }
        
        Object[] array = FIFO.getNodes();
        for(int i=0; i<array.length; i++)
            System.out.print((double)array[i]+" ");
    }
}
