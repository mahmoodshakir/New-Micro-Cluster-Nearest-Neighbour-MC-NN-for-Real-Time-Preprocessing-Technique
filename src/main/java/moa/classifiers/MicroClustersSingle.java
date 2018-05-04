/*
 *    RuleClassifier.java
 *    Copyright (C) 2012 
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 *    
 */

package moa.classifiers;

import java.util.*; 
import moa.core.Measurement;
import moa.options.FlagOption;
import moa.options.IntOption;
import weka.core.Instance;
import java.io.*;
//import MicroClustersVisualization.FeatureData;


/**
 *
 * @author Mark Tennant @ Reading University :2015
 */

public class MicroClustersSingle extends AbstractClassifier{
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
    
    MicroClusterManager MM = new MicroClusterManager();
    
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
            //for(int q=0; q<votes.length; q++)
            //	System.out.print(votes[q] + " ");
            //System.out.println(" ");
           // System.out.println("               End votes "+" Size = "+ votes.length);
            
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
             
             //System.out.println(" resetLearningImpl " + LowPassFilterOption + alphaLowPassFilterOption);
             
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
       	
     public double[] SplitDeathAlarm(Instance inst){
    	 this.getVotesForInstance(inst); // there is no need to call getVotes 
        //int classindex = (int) inst.classValue();
        
        //this.MicroCluster_ResetHighVariantsAttributesIndexes(inst.numClasses(),inst.numAttributes());
        
    	this.trainOnInstanceImpl(inst);
 
         /*
         Velocity Process - Feature Analysis - Feature Selection
         */
         //this.MicroCluster_ResestInformationGainArray();
         //this.MicroCluster_InsertInformationGainArray();
         //this.MicroCluster_ComputeInformationGain();
         
         //this.MicroCluster_InsertInformationGainArrayFullPeriodTime();
         
         //this.MicroCluster_VelocityCalculate();
        //this.MicroCluster_VelocitySumCF1XandN();
         this.MicroCluster_VelocitySum();
    	 //System.out.println(MM.splitAlarm());
         
        //this.MicroCluster_ApplyCopyData();
        
    	return MM.SplitDeathAlarming();
     }	
     public void ResetMMOptions(boolean LPF, double alphaLPF , int ErrorOptionD , int MAXClusterD, int RemovePoorlyD , int SplitMultiAttribD, boolean SplitOnAllAttribD){
         MM.ResetManagerOptions(LPF, alphaLPF , ErrorOptionD , MAXClusterD, RemovePoorlyD , SplitMultiAttribD, SplitOnAllAttribD);
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
     
     public int[] getVelocityNum(){
         return MM.getVelocityNum();
     }
     
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
     
     public int[] HighVariantsAttributesIndexes(){
         return MM.HighVariantsAttributesIndexes();
     }
     
     public void MicroCluster_ResetHighVariantsAttributesIndexes(int numattrib){
         MM.ResetHighVariantsAttributesIndexes(numattrib);
     }
     
     public void MicroCluster_ResetHighVariantsAttributesIndexes(){
         MM.ResetHighVariantsAttributesIndexes();
     }
     
     public void MicroCluster_ComputeInformationGainFullPeriodTime(){
         MM.ComputeInformationGainFullPeriodTime();
     }
     
     public void MicroCluster_ResetInformationGainArrayFullPeriodTime(int numattributes){
         MM.ResetInformationGainArrayFullPeriodTime(numattributes);
     }
     
     public void MicroCluster_ResetInformationGainArrayFullPeriodTime(){
         MM.ResetInformationGainArrayFullPeriodTime();
     }
     
     public void MicroCluster_InsertInformationGainArrayFullPeriodTime(){
         MM.InsertInformationGainArrayFullPeriodTime();
     }
     
     public void MicroCluster_ComputeInformationGain(){
         MM.ComputeInformationGain();
     }
     
     public void MicroCluster_InsertInformationGainArray(){
         MM.InsertInformationGainArray();
     }
         
     public void MicroCluster_ResestInformationGainArray(){
         MM.ResetInformationGainArray();
     }
     
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
