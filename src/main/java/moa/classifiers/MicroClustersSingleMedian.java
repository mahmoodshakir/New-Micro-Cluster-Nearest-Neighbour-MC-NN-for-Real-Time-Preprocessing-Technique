/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers;

import java.io.Serializable;
import moa.core.Measurement;
import moa.options.FlagOption;
import moa.options.IntOption;
import weka.core.Instance;
import java.util.*; 
import moa.core.Measurement;
import moa.options.FlagOption;
import moa.options.IntOption;
import weka.core.Instance;
import java.io.*;
//import MicroClustersVisualization.FeatureData_boxplot;
import moa.classifiers.MicroClusterSkipList.SkipList;

/**
 *
 * @author Mahmood
 */
public class MicroClustersSingleMedian extends AbstractClassifier{
    private static final long serialVersionUID = 1L;
    public int i =0;
   
    public IntOption ErrorOption = new IntOption(
		        "ErrorOption",'e', "The number of wrongly classifier to bear before splitting",
		         2, 2, 1000);
    
    public IntOption MAXClusterArraySizeOption = new IntOption(
		        "MAXClusters",'m', "The Maximum number of Micro-Clusters to maintain per Class Label.",
		         500, 1, Integer.MAX_VALUE);
    
    public IntOption RemovePoorlyPerformingClustersOption = new IntOption(
		        "RemoveClusters",'r', "The Minimum Weight for Classifier before Deletion",
		         50, 1, 100);
    
     public FlagOption SplitOnAllAttributesFlagOption = new FlagOption(
		        "AllSplit",'s', "AllSplit");
    
    
    public FlagOption UseGlobalTimeStampsOption = new FlagOption(
		        "GlobalTime",'t', "Use single time stamp for all Class Labels ");
    
    
    public FlagOption SplitClusterGetsNewTimeStampsOption = new FlagOption(
		        "NewTimeStampforSplitClusters",'n', "Upon Split Event Clusters Get New Time Stamp");
    
    
     public FlagOption DangerTheoryFlagOption = new FlagOption(
		        "UndoErrorsasBetterPerformance",'d', "reduce Error number for correct Classifications");
    
     public FlagOption DoublePunishFlagOption = new FlagOption(
		        "DoublePunish",'p', "DoublePunish");
    
     
     
   // public IntOption RemoveSmallClustersOption = new IntOption(/
		        //"SmallClusters",'r', "The Minimum Weight for Classifier before Deletion",
		        // 60, 1, 100);
    
      
    //public  String OutputPath  =   "C:\\Users\\Hazel\\Dropbox\\NETBEANS\\SPARKCutdown\\DATA\\OUTPUT\\MacroClusterDims";
    //public String OutputPath  = "C:\\Users\\xj024374\\Dropbox\\NETBEANS\\SPARKCutdown\\DATA\\OUTPUT\\MacroClusterDims";
    //public String OutputPath  = "/home/shared/Dropbox/NETBEANS/SPARKCutdown/DATA/OUTPUT/MacroClusterDims";
    public String OutputPath  = "C:/MacroClusterDims";
    private double[] Normalize_Max_AttribValu;
    private double[] Normalize_Min_AttribValu;
    private boolean initResetNormalizationOption = true;
    private boolean initial_normalization = true;
    private int RangeMaximumNormalizationOption = 100;
    private int RangeMinimumNormalizationOption = 0;
    
    MicroClusterManagerMedian MM = new MicroClusterManagerMedian();
    
    public boolean b_Initalised  = false;
    public boolean PrintStatsDebug = false;
    
      public static class InstancetoDouble implements Serializable  //scala.
    {
        private double[] DAttributes; 
      
        public double[] Convert(Instance inst)
        {   
            DAttributes = new double[inst.numAttributes()];
          
            for (int i = 0; i < inst.numAttributes(); i++) 
            {
                DAttributes[i] = inst.value(i);
                //System.out.print(inst.value(i));
            }
            //System.out.println(" ");
            return (double[])DAttributes.clone();
        }
    }   
 
     public InstancetoDouble ItoD  = new InstancetoDouble();
    
    
    @Override
    public int measureByteSize() 
    {
        return 0;
    }
      
    
    @Override
    public void getModelDescription(StringBuilder sb, int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
    @Override
    public String getPurposeString() 
    {
      return "MicroCluster Classifier";
    }
//protected AISEventListener LSEvent_CollisionDetection = new AISEventListener();
                    
    @Override
    public double[] getVotesForInstance(Instance inst) 
    {
        if (!b_Initalised)
        {
            MM.SetClassandAttributeCount((int)inst.numClasses() , (int)inst.numAttributes());
            b_Initalised = true;
        }
            
        double[] instD = ItoD.Convert(inst);
        double[] votes = this.MM.test(instD);
            
        //System.out.println("               Begin votes ");
        //System.out.println(inst.classValue());
        
        //for(int q=0; q<votes.length; q++)
        //    System.out.print(votes[q] + " ");
        //System.out.println(" ");
        //System.out.println("               End votes "+" Size = "+ votes.length);
        
            //CorrectPredictTest
        return votes;
    }
    
    @Override
    protected Measurement[] getModelMeasurementsImpl() 
    {
        return null;
    }
	
    @Override
    public void resetLearningImpl() 
    {
        b_Initalised= false; 
        
        /*
        Reset Real Time Normalization
        */
        initResetNormalizationArrays(1);
            
        MM.resetLearningImpl
        (
            ErrorOption.getValue(), 
            MAXClusterArraySizeOption.getValue(),
            RemovePoorlyPerformingClustersOption .getValue(),
            SplitOnAllAttributesFlagOption.isSet(),
            UseGlobalTimeStampsOption.isSet(),
            SplitClusterGetsNewTimeStampsOption.isSet(),
            DangerTheoryFlagOption.isSet(),
            DoublePunishFlagOption.isSet()
        ); 
    }
        
    @Override
    public void trainOnInstanceImpl(Instance inst) 
    {    
           
        if (!b_Initalised)
        {
            MM.SetClassandAttributeCount((int)inst.numClasses() , (int)inst.numAttributes());
            b_Initalised = true;
        }
           //this.MicroCluster_ResetHighVariantsAttributesIndexes();
            
        double[] instD = ItoD.Convert(inst);
        this.MM.train(instD);
    }
          
           	
    @Override
    public boolean isRandomizable() 
    {
        return false;
    }
	
	/**
     * Added by Mahmood
     * calling both test/votes and train in addition to retrieved result from MM
     */
       	
     public double[] SplitDeathAlarm(Instance inst, boolean RealTimeNormalization){
    	 this.getVotesForInstance(inst); // there is no need to call getVotes 
        
         /*
         Apply normalization
         */
         //if(initResetNormalizationOption){
         //    initResetNormalizationOption = false;
         //    initResetNormalizationArrays(inst.numAttributes()-1);
         //}
         
    	this.trainOnInstanceImpl(inst);
 
        this.MicroCluster_VelocitySum();
    	
    	return MM.SplitDeathAlarming();
     }	
    
     public void initResetNormalizationArrays(int AttribNum){
        Normalize_Max_AttribValu = new double[AttribNum];
        Normalize_Min_AttribValu = new double[AttribNum];
            
        for(int AttribIndex = 0 ; AttribIndex < AttribNum ; AttribIndex++){
            Normalize_Max_AttribValu[AttribIndex] = 0;
            Normalize_Min_AttribValu[AttribIndex] = 0;
        }
     }
     
     public Instance RealTimeNormalization_Compute(Instance instCurrent){
        
        double currentAttributeValue = 0;
        double NormalizedValue = 0;
        
        if(initial_normalization){
            
            for(int attribIndex = 0; attribIndex < instCurrent.numAttributes()-1; attribIndex++){
                currentAttributeValue = (double)instCurrent.value(attribIndex);
            
                Normalize_Min_AttribValu[attribIndex] = currentAttributeValue;
                Normalize_Max_AttribValu[attribIndex] = currentAttributeValue;
                
                /*
                Normalization equals 0 because Max - Min = 0 
                */
                instCurrent.setValue(attribIndex, 0);
            }
            
            this.initial_normalization = false;
                
        }else{
            for(int attribIndex = 0; attribIndex < instCurrent.numAttributes()-1; attribIndex++){
                currentAttributeValue = (double)instCurrent.value(attribIndex);
            
                /*
                Chack Minimum attibute value
                */

                if(currentAttributeValue<Normalize_Min_AttribValu[attribIndex] || Normalize_Min_AttribValu[attribIndex]==0){
                    Normalize_Min_AttribValu[attribIndex] = currentAttributeValue;
                }
        
                /*
                Chack Maximum attibute value
                */
            
                if(currentAttributeValue>Normalize_Max_AttribValu[attribIndex] || Normalize_Max_AttribValu[attribIndex]==0){
                    Normalize_Max_AttribValu[attribIndex] = currentAttributeValue;
                }
            
                /*
                Compute Normalization with Range [0 - 100]
                */
            
                if(Normalize_Max_AttribValu[attribIndex] - Normalize_Min_AttribValu[attribIndex]!= 0){
                    NormalizedValue = (double)((currentAttributeValue - Normalize_Min_AttribValu[attribIndex])/(Normalize_Max_AttribValu[attribIndex] - Normalize_Min_AttribValu[attribIndex])) * (RangeMaximumNormalizationOption - RangeMinimumNormalizationOption) + RangeMinimumNormalizationOption;
                    instCurrent.setValue(attribIndex,NormalizedValue);
                }else{
                    instCurrent.setValue(attribIndex, 0);
                }
            }
        }

        return instCurrent;
    }
     
     public void ResetMMOptions(boolean LPF, double alphaLPF , int ErrorOptionD , int MAXClusterD, int RemovePoorlyD , int SplitMultiAttribD, boolean SplitOnAllAttribD, int MaxNodesD, int MaxHeadsD, boolean TriangleOption){
         MM.ResetManagerOptions(LPF, alphaLPF , ErrorOptionD , MAXClusterD, RemovePoorlyD , SplitMultiAttribD, SplitOnAllAttribD, MaxNodesD, MaxHeadsD, TriangleOption);
     }
     
     public void MicroCluster_PrintCentersMCs(){
         MM.MicroCluster_PrintCentersMCs();
     }
     
     public void ResetAllMicroClusters(){
         MM.ResetAllMicroClusters();
     }
     
     public double[] getVelocitySum(){
         return MM.getVelocitySum();
     }
     
     //public double[] getRangeSum(){
     //    return MM.getRangeSum();
     //}
     
     //public double[] getKurtosisSum(){
     //    return MM.getKurtosisSum();
     //}
     
     //public int[] getKurtosisNum(){
     //    return MM.getKurtosisNum();
     //}
     
     //public void printBoxPlot(){
     //    MM.printBoxPlot();
    //}
     public SkipList GenerateSkipList_double(double[] FIFOnodes){
         return MM.GenerateSkipList_double(FIFOnodes);
     }
     
     public SkipList GenerateSkipList(Object[] FIFOnodes){
         return MM.GenerateSkipList(FIFOnodes);
     }
     
     public int[] getVelocityNum(){
         return MM.getVelocityNum();
     }
     
     //public int[] getRangeNum(){
     //    return MM.getRangeNum();
     //}
     public void MicroCluster_SwappingFeatures1And3SeaGeneratorOnlyWithDDM(){
         MM.MicroCluster_SwappingFeatures1And3SeaGeneratorOnlyWithDDM();
     }
     
     public void MicroCluster_VelocitySum(){
         MM.MicroCluster_VelocitySum();
     }
     
     public void MicroCluster_resetVelocitySum(){
         MM.MicroCluster_resetVelocitySum();
     }
     
     public void MicroCluster_resetVelocitySum(int numattrib){
         MM.MicroCluster_resetVelocitySum(numattrib);
     }
     
     public void MicroCluster_resetCF1XsumAndnsum(){
         MM.MicroCluster_resetCF1XsumAndnsum();
     }
     
     public void MicroCluster_resetCF1XsumAndnsum(int numattrib){
         MM.MicroCluster_resetCF1XsumAndnsum(numattrib);
     }
     
     public boolean SplitIsHappened(){
         return MM.SplitIsHappenedMC();
     }
     
     public void MicroCluster_ResetOutliers_Num(int numattrib){
         MM.ResetOutliers_Num(numattrib);
     }
     
     public void MicroCluster_ResetOutliers_Num(){
         MM.ResetOutliers_Num();
     }
     
     public int[] MicroCluster_get_Important_Outliers_Num(){
         return MM.get_Important_Outliers_Num();
     }
     
     public int[] MicroCluster_get_Extreme_Outliers_Num(){
         return MM.get_Extreme_Outliers_Num();
     }
     
     public int[] HighVariantsAttributesIndexes(){
         return MM.HighVariantsAttributesIndexes();
     }
     
     public void MicroCluster_ResetHighVariantsAttributesIndexes(int numattrib){
         MM.ResetHighVariantsAttributesIndexes(numattrib);
     }
     
     public void MicroCluster_ResetHighVariantsAttributesIndexes(){
         MM.ResetHighVariantsAttributesIndexes();
     }
     
     //public void MicroCluster_ComputeInformationGainFullPeriodTime(){
     //    MM.ComputeInformationGainFullPeriodTime();
    // }
     
    //// public void MicroCluster_ResetInformationGainArrayFullPeriodTime(int numattributes){
    //     MM.ResetInformationGainArrayFullPeriodTime(numattributes);
    // }
     
    // public void MicroCluster_ResetInformationGainArrayFullPeriodTime(){
    //     MM.ResetInformationGainArrayFullPeriodTime();
     //}
     
     //public void MicroCluster_InsertInformationGainArrayFullPeriodTime(){
     //    MM.InsertInformationGainArrayFullPeriodTime();
     //}
     
     //public void MicroCluster_ComputeInformationGain(){
     //    MM.ComputeInformationGain();
     //}
     
     //public void MicroCluster_InsertInformationGainArray(Instance instanc, int row){
     //    MM.InsertInformationGainArray(instanc, row);
     //}
     
     //public void MicroCluster_ResestInformationGainArray(int rows, int columns){
     //    MM.ResetInformationGainArray(rows, columns);
     //}
     
     public void MicroCluster_VelocityCalculate(){
         MM.MicroCluster_VelocityCalculate();
     }
     
     public void MicroCluster_ApplyCopyData(){
         MM.MicroCluster_ApplyCopyData();
     }
     
     public void MicroCluster_PrintCenters(){
         MM.MicroCluster_PrintCenters();
     }
     
     public StringBuffer MicroCluster_StringBuffer(){
         return MM.StrBuffer;
     }
     
     public void MicroCluster_StringBuffer_Reset(){
         MM.StringBuffer_Reset();
     }  
     
    //public int getCorrectPredict(){
    //    return MM.getCorrectPredict();
    //}
    
    //public void resetCorrectPredict(){
    //    MM.resetCorrectPredict();
    //}
    
    public int getCorrectPredictTest(){
        return MM.getCorrectPredictTest();
    }
    
    public void resetCorrectPredictTest(){
        MM.resetCorrectPredictTest();
    }
    
    public int[][] getCorrectPredictTestArray(){
        return MM.getCorrectPredictTestArray();
    }
    
    public void resetCorrectPredictTestArray(){
        MM.resetCorrectPredictTestArray();
    }
}