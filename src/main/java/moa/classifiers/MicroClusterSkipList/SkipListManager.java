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
public class SkipListManager {
    public static void main(String args[]) {
        int MaximumNodes = 10;
        int MaximumHeads = 15;
        
        double[] featureValue1 = {60.4, 20.1, 9.9, 30.2, 14.5 , 88.80, 1.34, 15.12 , 4, 7, 10 , 15 , 11 , 100 , 27 , 56 , 19 , 57, 2,0.60, 0.20, 0.9, 0.030, 0.014 , 0.88, 0.1, 0.15 , 0.4, 0.7, 0.10 , 0.15 , 0.11 , 0.1010 , 0.27 , 0.56 , 0.19 , 0.57, 0.21,9, 1, 8, 2, 7 , 3, 6, 5 , 16, 22, 10 , 15 , 11 , 99 , 17 , 36 , 59 , 77, 19};
        double[] featureValue2 = {9,3,16,7,8,6,1,4,5,10,0.5, 2,13,11,4.5,2.5,14,1.1,7.5,0.6,15,0.5,0.6,1.1,7.5,8.5,17,18,12};
        double[] featureValue3 = {0.60, 0.20, 0.9, 0.030, 0.014 , 0.88, 0.1, 0.15 , 0.4, 0.7, 0.16 , 0.15 , 0.11 , 0.1015, 0.27 , 0.56 , 0.19 ,10,1,12,3,14,5,2,7,18,13,1,6,34,89,100,22,44,66,88,230,120,678}; //
        double[] featureValue4 =  {7.1, 2.1, 3.5, 5.5, 9.3,10.9, 4.4, 8.7, 0.2, 0.92};
        double[] featureValue5 = {6,1.11,8,1,0,5,3,10,2,8,4,10.1,10,1.001,5,7,0,7.01,1,1,3,8,7,9,11,10,7.02,1,13,5,5,3,3,6,10,2,8,23};
        
        double[] featureValue6 = {1,2,3,4,5};
        
        SkipList[] SL = new SkipList[3];
        SL[0] = new SkipList(MaximumNodes, MaximumHeads);
        SL[1] = new SkipList(MaximumNodes, MaximumHeads);
        SL[2] = new SkipList(MaximumNodes, MaximumHeads);
        
        for(int i = 0; i < featureValue4.length; i++){
            SL[0].addNode(featureValue4[i],i);
            //SL[1].addNode(featureValue2[i], i);
            //SL[2].addNode(featureValue3[i], i);
        }
        
        double[] Nodes = SL[0].RetrieveNodes();
        //int[] TimeStamp = SL[0].RetrieveTimeStampNodes();
        
        System.out.println(".................-------- Feature 1");
        
        for(int i = 0 ; i < Nodes.length; i++)
            System.out.print(Nodes[i]+ " >> ");
        
        System.out.println("");
        
        //for(int i = 0 ; i < TimeStamp.length; i++)
        //    System.out.print(TimeStamp[i]+ " >> ");
        
        SL[0].printHeads();
        System.out.println("Median: "+SL[0].getMedian()+" Size:"+ SL[0].getNumberNodesHead(0)+" Middle: "+(int)SL[0].getNumberNodesHead(0)/2);
        //System.out.println("Median Node Value: " + SL[0].getMedianNodeValue());
        
        //SL[0].printLowerUpperMedian();
        System.out.println(" -------- ");
        System.out.println(" Q1: "+ SL[0].getMedianQ1Q3()[0]+" Q3: "+ SL[0].getMedianQ1Q3()[1]);
        
        //SL[0].printMedianQ1Q3();
        //double[] infoIQR = SL[0].getIQR(Nodes).clone();
        
        //Node[] LowerUpperMedian = SL[0].getLowerUpperMedian();
        
        //SL[0].resetHead();
        //SL[0].resetTail();
        //SL[0].resetUpperMedian(LowerUpperMedian);
        //SL[0].resetLowerMedian(LowerUpperMedian);
        //SL[0].printHeads();
        //System.out.println("Median Node Value: " + SL[0].getMedianNodeValue());
                
        int numOutliers = 0;
        int index = Nodes.length-1;
            
        //while(Nodes[index] > infoIQR[4]){
        //    System.out.println(" Upper Boundary Outlier: "+Nodes[index]);
        //    index--;
        //    numOutliers++;
        //}
            
        //index = 0;
        //while(Nodes[index] < infoIQR[3]){
        //    System.out.println(" Lower Boundary Outlier: "+Nodes[index]);
        //    index++;
        //    numOutliers++;
        //}
        
        //System.out.println(" Number of outliers: "+ numOutliers);
        
        
        //Nodes = SL[1].RetrieveNodes();
        
        //System.out.println(" ");
        //System.out.println(".................-------- Feature 2 ");
        
        //for(int i = 0 ; i < Nodes.length; i++)
        //    System.out.print(Nodes[i]+ " = ");
        
        //SL[1].printHeads();
        
        //Nodes = SL[2].RetrieveNodes();
        
        //System.out.println(" ");
        //System.out.println(".................-------- Feature 3 ");
        
        //for(int i = 0 ; i < Nodes.length; i++)
        //    System.out.print(Nodes[i]+ " = ");
        
        //SL[2].printHeads();
        
        /*
        Delete a node
        */
        
        //SL[0].deleteNode(9);
        //SL[0].deleteNode(15);
        //SL[0].deleteNode(20);
        //SL[0].deleteNode(27);
        //SL[0].deleteNode(4);
        //SL[0].deleteNode(1);
        //SL[0].printHeads();
        //SL[0].addNode(200, 25);
        //System.out.println("TimeStamp: "+SL[0].OlderNode_timeStamp+" Data: "+SL[0].OlderNode_value);
        
        //System.out.println(" Interquartile Range ");
        //double[] infoIQR = SL[0].getIQR(SL[0].RetrieveNodes()).clone();
        //System.out.println("Q1: "+ infoIQR[0]);
        //System.out.println("Q2: "+ infoIQR[1]);
        //System.out.println("Q3: "+ infoIQR[2]);
        //System.out.println("MinRange: "+ infoIQR[3]);
        //System.out.println("MaxRange: "+ infoIQR[4]);
        //System.out.println("IQR: "+ infoIQR[5]);
        
    }
}
