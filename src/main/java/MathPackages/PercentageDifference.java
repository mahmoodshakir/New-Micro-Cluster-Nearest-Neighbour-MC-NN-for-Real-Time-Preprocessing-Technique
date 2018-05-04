/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MathPackages;

/**
 *
 * @author Mahmood
 * Find the difference between 2 values as a percentage
 */
public class PercentageDifference {
   
    public PercentageDifference(){
        
    }
    
    public double compute(double Num1, double Num2){
        
        double Difference = Math.abs(Num1 - Num2);
        double Avg = (Num1 + Num2) / 2;
        double PercentageDiff = (Difference / Avg) * 100;
        
        return PercentageDiff;
    }
    
    public static void main(String[] args) {
  
        double Num1 = 0.021256;
        double Num2 = 0.020442;
        PercentageDifference PD = new PercentageDifference();
        System.out.println(PD.compute(Num1, Num2));
   
    }
}
