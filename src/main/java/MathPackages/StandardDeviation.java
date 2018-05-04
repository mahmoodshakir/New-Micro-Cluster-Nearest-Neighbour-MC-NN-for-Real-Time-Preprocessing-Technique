/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MathPackages;

/**
 *
 * @author Mahmood
 */
public class StandardDeviation {
    public StandardDeviation(){
        
    }
    
    public static double compute(double[] list) {
        double mean=0;
        double variance=0;
        double standard_dev=0;
        double sum = 0;
        /**
        * mean
        */
        for (int i = 0; i < list.length; i++){
            sum = sum + list[i];
        }
    
        mean=sum/list.length;
        /**
        * variance
        */
        sum=0;
        for (int i = 0; i < list.length; i++){
            sum=sum + Math.pow((list[i]-mean),2.0);
        }
        variance=sum/list.length;
        /**
        * standard deviation
        */
        standard_dev= Math.sqrt(variance);
        return standard_dev;
    }
    
    public static double[][] compute2D(double[][] list, int numInstances, int numAttributes) {
        /*
        MeanVarianceSTD always with 3 rows
        First row = Mean
        Second row = Variance
        Third row = STD
        */
        double[][] MeanVarianceSTD = new double[3][numAttributes];
        /**
        * mean
        */
        for(int column = 0; column<numAttributes; column++){ 
            double sum =0;
            for(int row = 0; row<numInstances; row++){  //
                sum = sum + list[row][column];
            }
            MeanVarianceSTD[0][column] = sum / numInstances;
        }
    
        /**
        * variance and standard deviation
        */
        for(int column = 0; column<numAttributes; column++){ 
            /**
            * variance only
            */
            double sum =0;
            for(int row = 0; row<numInstances; row++){
                double x = list[row][column];
                double y = MeanVarianceSTD[0][column]; //Mean
                sum = sum + Math.pow(x-y,2.0);
            }
            MeanVarianceSTD[1][column] = sum / numInstances; 
            /**
            * standard deviation only
            */
            MeanVarianceSTD[2][column] = Math.sqrt(MeanVarianceSTD[1][column]);
        }
        
        return MeanVarianceSTD;
    }
    
    public static double compute_mean(double[] list) {
        
        double sum = 0;
        /**
        * mean
        */
        for (int i = 0; i < list.length; i++){
            sum = sum + list[i];
        }
    
        return sum/list.length;
    }
    
    public static double compute_StandardError_mean(double[] list,double StandardDeviation) {
        return StandardDeviation/(Math.sqrt(list.length));
    }
    
    public static double compute_withLength(int length, double[] list) {
        double mean=0;
        double variance=0;
        double standard_dev=0;
        double sum = 0;
        /**
        * mean
        */
        for (int i = 0; i < length; i++){
            sum = sum + list[i];
        }
    
        mean=sum/length;
        /**
        * variance
        */
        sum=0;
        for (int i = 0; i < length; i++){
            sum=sum + Math.pow((list[i]-mean),2.0);
        }
        variance=sum/length;
        /**
        * standard deviation
        */
        standard_dev= Math.sqrt(variance);
        return standard_dev;
    }
    
    public static double compute_mean_withLength(int length, double[] list) {
        
        double sum = 0;
        /**
        * mean
        */
        for (int i = 0; i < length; i++){
            sum = sum + list[i];
        }
    
        return sum/length;
    }
    
    public static void main(String[] args){
            
        //double[] list_num = new double[2];
        //list_num[0]=400;
        //list_num[1]=100;
        
        //System.out.println("The standard deviation is: " + compute(list_num));
        //System.out.println("The mean is: " + compute_mean(list_num));
        //System.out.println(list_num.length);
        //System.out.println("The Standard Error of mean is: " + compute_StandardError_mean(list_num,compute(list_num)));
        
        double[][] list_num = new double[3][2];
        list_num[0][0]=400;
        list_num[0][1]=400;
        list_num[1][0]=100;
        list_num[1][1]=100;
        list_num[2][0]=250;
        list_num[2][1]=100;
        
        for(int x=0; x<3; x++){
            for(int y=0; y<2; y++)
                System.out.print(list_num[x][y] + " ");
            System.out.println("");
        }
        
        /*
        MeanVarianceSTD always with 3 rows
        First row = Mean
        Second row = Variance
        Third row = STD
        */
        double[][] MeanVarianceSTD = compute2D(list_num,3,2);
        
        System.out.println("Mean & Variance & STD");
        for(int x=0; x<3; x++){
            for(int y=0; y<2; y++)
                System.out.print(MeanVarianceSTD[x][y] + " ");
            System.out.println("");
        }
        
        //System.out.println("The standard deviation is: " + compute(list_num));
        //System.out.println("The mean is: " + compute_mean(list_num));
        //System.out.println(list_num.length);
        //System.out.println("The Standard Error of mean is: " + compute_StandardError_mean(list_num,compute(list_num)));
        
	}
}