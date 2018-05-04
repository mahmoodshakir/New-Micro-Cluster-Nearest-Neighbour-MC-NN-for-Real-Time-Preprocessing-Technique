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
import java.io.*;
import java.math.BigDecimal;
//import MicroClustersVisualization.FeatureData;
import static weka.core.Utils.log2;

/**
 *
 * @author Mark Tennant @ Reading University :2015
 */


public class MicroClusterManager implements Serializable {
    private static final long serialVersionUID = 1L;
   
    private MicroCluster[][] Clusters; 
    private int[] MicroClustersCount; 
    private int[][] MicroClustersErrorCount;
    public double[] Centers_Array;
    public double CF1T_Value=0;
    
    private String AdditionList = ""; 
    private String RemovalList = ""; 
          
    public int ErrorOption = 2; //1000 with RandoomTreeGenerator -- 2 with SEAGenerator -- 50 with covtypeNom and 200 window size in addition to 100000 frquency
    public int MAXClusterArraySizeOption =  100;//1000   
    public int RemovePoorlyPerformingClustersOption = 50; // Range between 0 and 100   
    public double SplitMultiAttributesOption = 50;  // Maximum number of selected attributes with high variant within split method
    public boolean SplitOnAllAttributesFlagOption = true;
    public boolean UseGlobalTimeStampsOption = true; 
    public boolean SplitClusterGetsNewTimeStampsOption =  true; 
    public boolean DangerTheoryFlagOption = true;
    public boolean DoublePunishFlagOption = true; 
    public boolean LowPassFilterOption;
    public double AlphaLowPassFilter;
    
    private int NumClasses = -1;
    private int NumAttributes = -1; 
          
    public String OutputPath  = "c:/";//"/home/shared/Dropbox/NETBEANS/SPARKCutdown/DATA/OUTPUT/MacroClusterDims";
       
    public boolean b_Initalised  = false;
   
    public boolean PrintStatsDebug = false;
    /**
     * Added by Mahmood
     * Saving the last values of Split and Death
     */
    public int last_SplitCounter=0;
    public int last_DeathCounter=0;
    public StringBuffer StrBuffer = new StringBuffer();
    public boolean SplitIsHappened = false;
    /**
     * Added by Mahmood
     * Count split MC-NN
     */
    //public double[] SplitAlarmIndex;
    /**
     * Added by Mahmood
     * Count death MC-NN
     */
    //public double[] DeathAlarmIndex;
    
    /**
     * Added by Mahmood
     * Counter split MC-NN
     */
    //public int SplitCounter;
    
    /**
     * Added by Mahmood
     * Counter death MC-NN
     */
    //public int DeathCounter;
    int trainingcount = 0; 
    
    /**
     * Added by Mahmood
     * Columns = number of attributes + 1 for class label index 
     * Rows = MicroClustersCount all of them where each MC consists of 1 row of attributes (Centers)
     */
    public double[][] InformationGain;
    public String InformationGainClassIndex;
    public double[][] InformationGainFullPeriodTime;
    public String[] InformationGainClassIndexFullPeriodTime;
    public int rowInfoGain = -1;
    //public int[][] HighVariantsAttributes;
    public int[] HighVariantsAttributes;
    public int[] HighVariantsAttributes_Num;
    public double[] CF1XSum;
    public int[] nSum;
    public double[] VelocitySum;
    public int[] VelocityNum;
    // MOVED to the MC for independence testing 
    public double[] StreamSquareTotal; 
    public int[] StreamItemCount; 
    public double[] StreamTotal; 
    public double NumberSelectedFeatures = 0;
    
    public int[] trainNNIndex; 
    public double[] trainNNDistance; 
    public MicroCluster[] trainNNMicroCluster; 
     
    // stastics for De-~Bugging 
    public int SplitCount = 0;
    public int InsertCount = 0;
    public int DeleteCount = 0;
    //public int CorrectPredict = 0; 
    public int CorrectPredictTest = 0; 
    public int[][] CorrectPredictTestArray;
    
   public static class TMeasure
    {
    public double TValue = -1; 
    public int TrainingIndex = -1; 
    }
    
    public static class MyTMeasureComp implements Comparator<TMeasure>
    {
    @Override
        public int compare(TMeasure e1, TMeasure e2) 
        {
            if (e1.TValue==e2.TValue)
            {
                return 0; 
            }else{
            if(e1.TValue < e2.TValue)
            {
                return -1;
            } else {
                return 1;
            }
            }
        }
    }

     public static class MyMicroClusterNComp implements Comparator<MicroCluster>
     {
    @Override
        public int compare(MicroCluster e1, MicroCluster e2) 
        {
            if (e1.N==e2.N)
            {
                return 0; 
            }else{
            if(e1.N < e2.N)
            {
                return -1;
            } else {
                return 1;
            }
            }
        }
    }
  
  
 
                
      private boolean AllNumericTrainingData = true; 
        //  public IntOption KValueOption = new IntOption("KValue",'k', "The number of instances nearest to use (Make odd).",3, 1, Integer.MAX_VALUE);
        //  public IntOption MaxTrainingItemsOption = new IntOption("MAXTrainingItems",'x', "The max number of instances keep.",-1, -1, Integer.MAX_VALUE);
        //  public FlagOption KMeanNoiseSupressionOption = new FlagOption("KMean_Training_of_Data",'e', "The number of instances nearest to use (Make odd).");   
        public double[] getclassIndexByMinDistance(double[][] distances)
        {           
            
            // DIST 
            // [DIST][INDEX of MC]
            
            
            double MinIndex[] = new double[2];
            //MinIndex[0]  = -1; // Inital ERROR Value.... 
            MinIndex[0]  = 0; // Inital ERROR Value.... 
            double MinVal = Double.MAX_VALUE;
            //Different OPTSD #       
            
            for (int i = 0; i < distances.length; i++) 
            {
                if (distances[i][0] < MinVal ) 
                {
                    MinVal = distances[i][0];
                    MinIndex[0] = i;
                    MinIndex[1] = distances[i][1];
                }
            }            
            
            // [ClassID][INDEXinArray]
            return MinIndex;
        }
    
      
        public void SetClassandAttributeCount(int ClassCount , int AttributeCount)
        {
            NumClasses=ClassCount;
            NumAttributes=AttributeCount; 
            
            ResetHighVariantsAttributesIndexes();
            SplitIsHappened = false;
        }
        
        
        private double[][] getAllDistances(int numClasses, double[] InstD)  //called by test and train
        {
                double[][] distances = new double[numClasses][2];
                
                for (int i = 0; i < numClasses; i++) 
                {
                    distances[i] =  getNearsetMCDistance(i,InstD);
                }
                return distances;
        }
      
        
	public double[] test(double[] InstD) 
        {
            int ClassIndex = (int)InstD[InstD.length-1];
            
            double[] votes = new double[NumClasses];    
            int WinningIndex = 0; 
            if (this.b_Initalised)
            {
                WinningIndex = (int)getclassIndexByMinDistance(getAllDistances(NumClasses,InstD))[0];
                
                if (WinningIndex < 0 ||  WinningIndex > 10)
                {
                    int n =0; 
                }
                
                
                votes[WinningIndex] = 1;
            }
            
            //if(ClassIndex == WinningIndex)
            //    CorrectPredictTest++;
            
            if(!b_Initalised){
                CorrectPredictTestArray = new int[NumClasses][NumClasses];
                for(int i=0; i<NumClasses; i++){
                    for(int j=0; j<NumClasses; j++){
                        CorrectPredictTestArray[i][j] = 0;
                    }
                }
            }
        
            CorrectPredictTestArray[ClassIndex][WinningIndex]++;
            
            return votes; 
	}
        
        //@Override
	public void resetLearningImpl(
            int ErrorOptionD, 
            int MAXClusterArraySizeOptionD,
            int RemovePoorlyPerformingClustersOptionD,
            boolean SplitOnAllAttributesFlagOptionD, 
            boolean UseGlobalTimeStampsOptionD, 
            boolean SplitClusterGetsNewTimeStampsOptionD,
            boolean DangerTheoryFlagOptionD, 
            boolean DoublePunishFlagOptionD
            ) 
        {
                b_Initalised= false; 
                
                ErrorOption = ErrorOptionD;
                MAXClusterArraySizeOption=MAXClusterArraySizeOptionD;
                RemovePoorlyPerformingClustersOption=RemovePoorlyPerformingClustersOptionD;
                SplitOnAllAttributesFlagOption=SplitOnAllAttributesFlagOptionD;
                UseGlobalTimeStampsOption=UseGlobalTimeStampsOptionD;
                SplitClusterGetsNewTimeStampsOption=SplitClusterGetsNewTimeStampsOptionD;
                DangerTheoryFlagOption=DangerTheoryFlagOptionD;
                DoublePunishFlagOption=DoublePunishFlagOptionD;
	}

        public String PrintClusters()
        {
            StringBuffer SB  =  new StringBuffer();
            SB.append("{");
             
                    for (int i = 0; i < this.Clusters.length; i++) 
                    {
                        for (int j = 0; j < this.MicroClustersCount[i] ; j++) 
                        {
                            // PRINT THE CENTERS 
                            MicroCluster MC = Clusters[i][j];
                            // SB.append("CENTERS>>");
                            for (int k = 0; k < MC.Centers.length; k++) 
                            {
                                SB.append(",");
                                SB.append("[");
                                SB.append(i);
                                SB.append("][");
                                SB.append(j);
                                SB.append("]{");
                                SB.append(k);
                                SB.append("}");
                                
                                SB.append(MC.Centers[k]);
                            } 
                        }
                       SB.append("}");
                    }
                   
                return SB.toString();
        }
        
        
          public void PrintInsertDeleteCount(String Header) throws UnsupportedEncodingException, IOException
        { 
            
            String OutPutFile = OutputPath +  "\\InsertDelete.csv";
            StringBuffer SB  =  new StringBuffer();
             
            
            try {   
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(OutPutFile, true), "UTF-8"));
                    //writer.newLine();
                    //writer.append(Header);
                    //writer.newLine();
                    
                    //for (int i = 0; i < this.Clusters.length; i++) 
                    //{
                        SB.append("Insert / Split / Delete,");
                        
                        SB.append(this.InsertCount);
                        SB.append(",");
                        SB.append(this.SplitCount);
                        SB.append(",");
                        SB.append(this.DeleteCount);
                        SB.append(",");
                        SB.append(",");
                        SB.append(",");
                        
                        int TOTAL = 0; 
                        for (int i = 0; i < this.Clusters.length; i++) 
                        {
                            int Num  = this.MicroClustersCount[i];
                            SB.append(",");    
                            SB.append(Num);
                            TOTAL = TOTAL + Num;
                        }
                        
                        SB.append(",");  
                        SB.append(TOTAL);
                        
                        writer.write(SB.toString());
                        SB = new StringBuffer();
                        writer.newLine();
                        writer.flush();
                   // }
                   
                } catch (FileNotFoundException ex) 
                {
                  //  Logger.getLogger(MicroClustersMytosis.class.getName()).log(Level.INFO, null, ex);
                }
            
            //System.out.println("Split count: "+this.SplitCount + " Death count: "+ this.DeleteCount);
            
            this.InsertCount = 0; 
            this.DeleteCount=0; 
            this.SplitCount=0; 
            
        }
        
        
	
        
        public void SetUPClusterArrays()
        {
        	/**
             * Added by Mahmood
             */
            //this.SplitAlarmIndex=new double[MAXClusterArraySizeOption];
            /**
             * Added by Mahmood
             */
            //this.DeathAlarmIndex=new double[MAXClusterArraySizeOption];
            /**
             * Added by Mahmood
             */
            //this.SplitCounter=0;
            
            /**
             * Added by Mahmood
             */
            //this.DeathCounter=0;
            ///**
             //* Added by Mahmood - from Mark algorithm
             //*/
            //System.out.println("Split count: "+this.SplitCount + " Death count: "+ this.DeleteCount);
            //this.InsertCount = 0; 
            //this.DeleteCount=0; 
            //this.SplitCount=0; 
            
            // choose the attribute from high to low based on the ratio given in SplitMultiAttributesOption
            NumberSelectedFeatures = Math.round((NumAttributes-1) * (SplitMultiAttributesOption / (float)100));
            
            this.Clusters = new MicroCluster[NumClasses][MAXClusterArraySizeOption];
            MicroClustersCount = new int[NumClasses];
            MicroClustersErrorCount = new int[NumClasses][MAXClusterArraySizeOption];
            
            StreamSquareTotal = new double[NumClasses];
            StreamItemCount = new int[NumClasses];
            StreamTotal = new double[NumClasses];
            
            trainNNIndex = new int[NumClasses]; 
            trainNNMicroCluster = new MicroCluster[NumClasses];  
            trainNNDistance = new double[NumClasses];
            
            /*
            Added by Mahmood
            */
            Centers_Array = new double[NumAttributes-1];
            
            for (int i = 0; i < NumClasses; i++) 
            {
                for (int j = 0; j < MAXClusterArraySizeOption; j++) 
                {
                    Clusters[i][j] = new MicroCluster(i,NumAttributes-1, this.StreamItemCount[i],LowPassFilterOption,AlphaLowPassFilter);
                    MicroClustersErrorCount[i][j] = 0;  
                }
            }
            
            for (int i = 0; i < NumClasses; i++) 
            {
                MicroClustersCount[i] = 1; 
            }
            
            b_Initalised = true;
        }
    
        
        private int GetStreamCount(int ClassIndex)
        {
            
            if (this.UseGlobalTimeStampsOption)
            {
                return this.StreamItemCount[0];
            }
            else
            {
                return this.StreamItemCount[ClassIndex];
            }
        }
        
        public void CheckAndRemoveClusters(int ClassIndex) throws IOException
        {            
            if (this.MicroClustersCount[ClassIndex] > 1)
            {
                for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++)             
                {
                    MicroCluster MC = this.Clusters[ClassIndex][i];
                    double ClusterWeight = MC.CalculateMyBigWeight(GetStreamCount(ClassIndex));
                    
                    //System.out.println("ClassIndex: "+ClassIndex+" Participation Number: "+MC.NumberParticipations + " ClusterWeight: "+ ClusterWeight);
                    
                    if (RemovePoorlyPerformingClustersOption > ClusterWeight )
                    {
                        if (this.trainingcount > 40000)
                        {
                            int nn =0 ;
                        }   
                        //double dd = MC.CalculateMyBigWeight(GetStreamCount(ClassIndex));
                        this.removeMicroCluster(ClassIndex, i);
                        //System.out.println(ClusterWeight);
                        //System.out.println(" MC Removed ");
                        
                        //PrintClusters("<<<<<< REMOVAL");
                        
                        break;
                    }
                }
            }
        }
        
        
        public void UpdateMicroClusterTimeStemaps(int ClassIndex)
        {
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++) 
            {
                MicroCluster MC = this.Clusters[ClassIndex][i];
                MC.IncrementTestingAges();
            }
        }
        
        
        private void IncrementStreamCount(int ClassIndex)
        {
            if (this.UseGlobalTimeStampsOption)
            {
                this.StreamItemCount[0]++;
            }
            else
            {
                this.StreamItemCount[ClassIndex]++;
                
            }
            
            
        }
        
        
	public void train(double[] InstD) 
        {    
         
            int ClassIndex = (int)InstD[InstD.length-1];
            
            if (!b_Initalised)
            {
                SetUPClusterArrays();
            }
              
            
            trainingcount++;
            IncrementStreamCount(ClassIndex);
            
            //remove Poorly Performing MicroCLusters
            
            //UpdateMicroClusterTimeStemaps(ClassIndex);        
          for (int i = 0; i < this.Clusters.length; i++) 
         {
             try
             {
                 CheckAndRemoveClusters(i);
             } 
             catch (IOException ex) 
             {
                //Logger.getLogger(MicroClustersMytosis.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
          
            //  NOT *****  not all Classes  need to be checked ---  as just httis class's class time incremnent has occurred!!  
            int MinClassIndex = Integer.MAX_VALUE;
            double MinClassDistnace = Double.MAX_VALUE;
            
            double[][] AllDistancesbyClass = getAllDistances(NumClasses,InstD);
                        
            for (int i = 0; i < this.Clusters.length; i++) 
            {
                //try
                 // {
                    //CheckAndRemoveClusters(i);
                    double[] ClassIndexValues  = AllDistancesbyClass[i];
                    trainNNIndex[i] = (int)ClassIndexValues[1];
                    trainNNDistance[i] = ClassIndexValues[0];
                    trainNNMicroCluster[i] =  this.Clusters[(ClassIndex)][trainNNIndex[i]];

                    if (MinClassDistnace  >= trainNNDistance[i])
                    {
                        MinClassDistnace = trainNNDistance[i];
                        MinClassIndex = i; 
                    }

                //} 
                //catch (IOException ex) 
               // {
               // Logger.getLogger(MicroClustersMytosis.class.getName()).log(Level.SEVERE, null, ex);
               //}
           }
            
            // Should we be adding this or a New Concept Cluster???? 
            trainNNMicroCluster[ClassIndex].IncrementCluster(InstD,this.GetStreamCount(ClassIndex));
            //trainNNMicroCluster[ClassIndex].ParticipationIncrement();
            
            if (MinClassIndex != ClassIndex)
            {
                // this Class Pubnished for not Knowing this data....
                this.MicroClustersErrorCount[ClassIndex][trainNNIndex[ClassIndex]]++;
                
                //Increment false positive participation
                trainNNMicroCluster[ClassIndex].ParticipationFalsePositiveIncrement();
                
                if (trainNNMicroCluster[ClassIndex].N > 2)
                {
                    AttemptSplitsforMicroCluster(ClassIndex,trainNNIndex[ClassIndex]);   // split
                }else 
                {
                    if (DoublePunishFlagOption)
                    {
                        // Undo Prevoius Insert as it's not going to make a difference. 
                        this.removeMicroCluster(ClassIndex,trainNNIndex[ClassIndex]);
                        InsertNewMicroCluster(ClassIndex,InstD);
                    }
                }
            
                if (MinClassIndex < 0 || MinClassIndex > 10)
                {
                    int n =0; 
                }
                
                
                //just this Class NUM-NUTS 
                this.MicroClustersErrorCount[MinClassIndex][trainNNIndex[MinClassIndex]]++;
                AttemptSplitsforMicroCluster(MinClassIndex,trainNNIndex[MinClassIndex]);      //split 
                 
            }
            else
            {   /**
                 * Added by Mahmood
                 */
            	//this.SplitDeathAlarmIndex=0;
                
                /*
                Tentative accuracy
                */
                //CorrectPredict++;
                
               if (DangerTheoryFlagOption)
               {
                    //reduce Error Count. 
                   if (this.MicroClustersErrorCount[ClassIndex][trainNNIndex[ClassIndex]] > 0)
                    {
                          this.MicroClustersErrorCount[ClassIndex][trainNNIndex[ClassIndex]]--;
                          
                          MicroCluster MC_TruePositive = this.Clusters[ClassIndex][trainNNIndex[ClassIndex]];         
                          if(MC_TruePositive.NumberParticipationsFalsePositive>0){
                                MC_TruePositive.ParticipationFalsePositiveDecrement();
                        //System.out.println(" Decremented "+ MC_TruePositive.NumberParticipationsFalsePositive);
                        }
                    }
               }
            }
        }  //end train
        
      
        public void AttemptSplitsforMicroCluster(int ClassIndex,int  MCIndex )
        {
            
             MicroCluster MC = this.Clusters[ClassIndex][MCIndex];
             
             
             if (MC.N==0)
             {
                 // PROBLEM 
                 // WTF
                 
                 int i =0; 
             }
             
             
             if (MC.N > 1)
             {
                 try
                 {
                    if (this.MicroClustersErrorCount[ClassIndex][MCIndex] > ErrorOption)
                    {
                    	/**
                         * Added by Mahmood
                         */
                        //this.SplitAlarmIndex[this.SplitCounter]=1;
                        //this.SplitCounter++;
                        
                        SplitMicroCluster(MCIndex, ClassIndex);    //split
                    }else{
                    	/**
                         * Added by Mahmood
                         */
                        //this.SplitAlarmIndex=0;
                        //System.out.println(" No Split");
                    }
                 }
                            catch(Exception e)
                            {
                            }
             } 
             else
             {
                 
                 //Faster an worse. 
             //   if (this.MicroClustersCount[ClassIndex] > 1)
             //   {
             //       this.removeMicroCluster(ClassIndex, MCIndex);
             //   }
             }
          }
            
        
        public void removeMicroCluster(int ClassIndex, int MCIndex)
        {
            //[][x][][][][][][L]
            //[][L][][][][][][L]
            //[][L][][][][][]
            // if MCIndex = 2 ----- Overwrite removal object with last in array and decrement pointer ...
        	
        	/**
        	 * Added by Mahmood
        	 * Death rate - Alarming
        	 */
            //this.DeathAlarmIndex[this.DeathCounter]=1;
            //this.DeathCounter++;
            
            this.DeleteCount++;
            Clusters[ClassIndex][MCIndex] = Clusters[ClassIndex][this.MicroClustersCount[ClassIndex]-1];          
            MicroClustersErrorCount[ClassIndex][MCIndex] = MicroClustersErrorCount[ClassIndex][this.MicroClustersCount[ClassIndex]-1]; 
            
            this.MicroClustersCount[ClassIndex]--; 
        }
        
        public void reshuffleClusterArray(int CLassIndex)
        {
            // find Least USED 
            
            double LeastCount = Double.MAX_VALUE; 
            int IndexLeast  = 0; 
            
            for (int i = 0; i < this.MAXClusterArraySizeOption-2; i++) 
            {
                //going to Remove the Oldest... smallest (sum / N) 
                MicroCluster MC = Clusters[CLassIndex][i];
                double CheckValue  = MC.CalculateMyBigWeight(this.MicroClustersCount[CLassIndex]);
                
                if ((CheckValue )< LeastCount)
                {
                    IndexLeast = i; 
                    LeastCount = CheckValue;
                }
            }
            MicroCluster TMP = Clusters[CLassIndex][IndexLeast];
            Clusters[CLassIndex][IndexLeast] = Clusters[CLassIndex][this.MAXClusterArraySizeOption-1];
            Clusters[CLassIndex][this.MAXClusterArraySizeOption-1] = TMP;
            
            // NOW RESET the Counters 
            MicroClustersErrorCount[CLassIndex][IndexLeast] = MicroClustersErrorCount[CLassIndex][this.MAXClusterArraySizeOption-1]; 
            MicroClustersErrorCount[CLassIndex][this.MAXClusterArraySizeOption-1] = 0; 
            
        }
        
        
        public void InsertNewMicroCluster(int ClassIndex, double[] instD)
        {
            
            InsertCount++;
             if (MicroClustersCount[ClassIndex] <  MAXClusterArraySizeOption)
            {
                MicroClustersCount[ClassIndex]++;
            } else 
            {
                //need to re-shuffle array so least used is on the end             
                reshuffleClusterArray(ClassIndex);
            }
             
             
             MicroCluster MCNew = Clusters[ClassIndex][MicroClustersCount[ClassIndex]-1];
             MCNew.ResetCluster();

             MCNew.IncrementCluster(instD,this.GetStreamCount(ClassIndex));
             
             Clusters[ClassIndex][MicroClustersCount[ClassIndex]-1] = MCNew;
        }
        
        public void SplitMicroCluster(int MCIndex, int ClassIndex) throws IOException
        {
            
            int AttributeIndex  = 0;
            double AttributeValue = 0;
            
            this.SplitIsHappened = true;
            this.SplitCount++;  //split
            
            if (MicroClustersCount[ClassIndex] <  MAXClusterArraySizeOption)
            {
                MicroClustersCount[ClassIndex]++;
            } else 
            {
                //need to re-shuffle array so least used is on the end             
                reshuffleClusterArray(ClassIndex);
            }
            
            MicroCluster MCSplit = Clusters[ClassIndex][MCIndex];
            //MicroCluster MCNew = Clusters[ClassIndex][MicroClustersCount[ClassIndex]-1];
            
            MicroCluster MCNew = new MicroCluster(ClassIndex  , MCSplit.NumAttributes , 0 , LowPassFilterOption, AlphaLowPassFilter); //It has been updated by Mark on 8/3/2016
            
            //if(!SplitOnAllAttributesFlagOption){
                
                // new is always the end array item.     
                AttributeIndex  =  MCSplit.findMaxVariantArrtibute();
                AttributeValue = MCSplit.findMaxVariantArrtibureValue();
            
                /*
                History of HighVariantsAttributes
                */
            
                HighVariantsAttributes[AttributeIndex]++;
            //}
            
                double[] Variants = MCSplit.getVariants();
            // now rescale for N = 1
            for (int i = 0; i < MCSplit.CF1X.length; i++) 
            {
                MCSplit.CF1X[i] = MCSplit.CF1X[i] / MCSplit.N;
            }
            
            double[] instDLow = MCSplit.CF1X.clone();
            double[] instDHigh = MCSplit.CF1X.clone();
                      
            if(SplitOnAllAttributesFlagOption){
                
                int size = Variants.length;//(int)NumberSelectedFeatures;
                //int[] AttributesIndexes_SelectedHighToLow = new int[size];
                //double[] VariantsValues_SelectedHighToLow = new double[size];
                //System.arraycopy(MCSplit.findMaxVariantArrtibute_AttributesIndexesHightoLow(), 0, AttributesIndexes_SelectedHighToLow, 0, size);
                //System.arraycopy(MCSplit.findMaxVariantArrtibute_AttributesValuesHightoLow(), 0, VariantsValues_SelectedHighToLow, 0, size);
                
                // Revers for loop which stop at number attributes - number selected attributes
                
                for(int i=0; i<size; i++){//for(int i=MCSplit.NumAttributes-1; i>=MCSplit.NumAttributes-(int)NumberSelectedFeatures;i--){
                    
                    //int IndexAttribSelected = AttributesIndexes_SelectedHighToLow[i];
                    double ValueAttribSelected = Variants[i];//VariantsValues_SelectedHighToLow[i];
                    
                    //System.out.println("IndexAttribSelected: "+IndexAttribSelected);
                    
                    instDLow[i] = instDLow[i] - (ValueAttribSelected);
                    instDHigh[i] = instDHigh[i] + (ValueAttribSelected);
                    
                    /*
                    History of HighVariantsAttributes
                    */
                
                    //HighVariantsAttributes[IndexAttribSelected]++;
                
                }
                
            }else{
                instDLow[AttributeIndex] = instDLow[AttributeIndex] - (AttributeValue);
                instDHigh[AttributeIndex] = instDHigh[AttributeIndex] + (AttributeValue);
            }
            
            int OldTime = (int)(MCSplit.CF1T / MCSplit.N);
            
            MCSplit.ResetCluster();
            MCNew.ResetCluster();
            
            // NEW Time STAMP 
            if (SplitClusterGetsNewTimeStampsOption)
            {
                MCSplit.IncrementCluster(instDLow,this.GetStreamCount(ClassIndex));
                MCNew.IncrementCluster(instDHigh,this.GetStreamCount(ClassIndex));
            }
            else
            {
                MCSplit.IncrementCluster(instDLow,OldTime);
                MCNew.IncrementCluster(instDHigh,OldTime);
            }
            
            /*
            Added by Mahmood
            Copy data for velocity
            */
            //MCSplit.CopyData();
            //MCNew.CopyData();
            
            Clusters[ClassIndex][MCIndex] = MCSplit;
            Clusters[ClassIndex][MicroClustersCount[ClassIndex]-1] = MCNew;
            
            
            //this.PrintClusters(">>>>>> NEW INSERT");
        }
        
        public MicroCluster MicroCluster_MaxError_visualization ()
    {        
        MicroCluster MCmaxerror = null;
        int MaxError=0;
        
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = this.Clusters[ClassIndex][i]; 
                if(this.MicroClustersErrorCount[ClassIndex][i] > MaxError){//ErrorOption
                    MaxError = this.MicroClustersErrorCount[ClassIndex][i];
                    MCmaxerror = MC;
                    }
                }   
            }
            return MCmaxerror;
        }
        
        public MicroCluster CheckOlderClusters_visualization() 
        {    
        MicroCluster MColder = null;
        
        int MaxFalsePositives = 0;
        //int Max_i = 0;
        int MaxDistanceTime = 0;
        
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = this.Clusters[ClassIndex][i]; 
                int FalsePositives = MC.NumberParticipationsFalsePositive;
                
                if(FalsePositives > 0){ // > MC.NumberParticipationsTruePositive
                    int ActualTimeStamp = (int)(MC.CF1T / MC.N);
                    int CurrentTimeStamp = this.GetStreamCount(ClassIndex);
                    int DistanceTime = CurrentTimeStamp - ActualTimeStamp; //Difference between current and actual time stamp
                    // Try the opposite way: Check distance firstly and FalsePositive secondly
                    if(DistanceTime > MaxDistanceTime){
                        if(FalsePositives > MaxFalsePositives){ //
                            MaxFalsePositives = FalsePositives;
                            MaxDistanceTime = DistanceTime;
                            //Max_i = i;
                            MColder = MC;
                        }
                    }
                }
            }
        }
        return MColder;
        }
        
        public Integer getNearsetMCIndex(int classIndex, double[] instd)
        {
            double iDist = Double.MAX_VALUE;
            int iIndex = 0; 
                        
            for (int i = 0; i < this.MicroClustersCount[classIndex]; i++) 
            {
                MicroCluster MC = Clusters[classIndex][i]; 
                
                double tmpECLDist = MC.EcludianDistancefromCentroid(instd);
                double TmpWeight = MC.CalculateMyWeight(this.GetStreamCount(classIndex)); 
                
                // Want low distance and High Weight. 
                // Therefor use division. 
                
                double TmpDist = (tmpECLDist/TmpWeight);
                
                if (TmpDist < iDist)
                {
                    iIndex = i; 
                    iDist = TmpDist;
                }
            }
            return iIndex;
        }
        
         public double[] getNearsetMCDistance(int classIndex, double[] instd)
        {   
            
            // RETURN [DIST][INDEX of MC]
            double[] iDist = new double[2];
            iDist[0] = Double.MAX_VALUE;
            int iIndex = 0; 
                        
            for (int i = 0; i < this.MicroClustersCount[classIndex]; i++) 
            {
                MicroCluster MC = Clusters[classIndex][i]; 
                
                double TmpDist = MC.EcludianDistancefromCentroid(instd);
                
                if (TmpDist < iDist[0])
                {
                    iIndex = i; 
                    iDist[0] = TmpDist;
                    iDist[1] = i;
                }
            }
            return iDist;
        }
         
    /*
    Added by Mahmood
    */
    public void ResetManagerOptions(boolean LPF, double alphaLPF , int ErrorOptionD , int MAXClusterD, int RemovePoorlyD , int SplitMultiAttribD, boolean SplitOnAllAttribD){
        LowPassFilterOption = LPF;
        AlphaLowPassFilter = alphaLPF;
        ErrorOption = ErrorOptionD;
        MAXClusterArraySizeOption = MAXClusterD;
        RemovePoorlyPerformingClustersOption = RemovePoorlyD;
        SplitMultiAttributesOption = SplitMultiAttribD;
        SplitOnAllAttributesFlagOption = SplitOnAllAttribD;
        
    }
    
    public void MicroCluster_SwappingFeatures1And3SeaGeneratorOnlyWithDDM(){
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = Clusters[ClassIndex][i];
                
                double swapCF1X = MC.CF1X[0];
                MC.CF1X[0] = MC.CF1X[2];
                MC.CF1X[2]= swapCF1X;
                
                double swapCF2X = MC.CF2X[0];
                MC.CF2X[0] = MC.CF2X[2];
                MC.CF2X[2]= swapCF2X;
                
                double swapCenter = MC.Centers[0];
                MC.Centers[0] = MC.Centers[2];
                MC.Centers[2]= swapCenter;
            } 
        }
    }
    
    //public int getCorrectPredict(){
    //    return CorrectPredict;
    //}
    
    //public void resetCorrectPredict(){
    //    CorrectPredict = 0;
    //}
    
    public int getCorrectPredictTest(){
        return CorrectPredictTest;
    }
    
    public void resetCorrectPredictTest(){
        CorrectPredictTest = 0;
    }
    
    public void ResetHighVariantsAttributesIndexes(int numattrib){
        //HighVariantsAttributes = new int[numclass][numattrib];
        HighVariantsAttributes = new int[numattrib];
        HighVariantsAttributes_Num = new int[numattrib];
        
        this.SplitIsHappened=false;
        for(int i =0;i<numattrib;i++){
            HighVariantsAttributes[i] = 0;
            HighVariantsAttributes_Num[i] = 0;
        }
    }
    
    public void ResetHighVariantsAttributesIndexes(){
        HighVariantsAttributes = new int[this.NumAttributes-1];
        HighVariantsAttributes_Num = new int[this.NumAttributes-1];
        
        this.SplitIsHappened=false;
       for(int i =0;i<this.NumAttributes-1;i++){
            HighVariantsAttributes[i] = 0;
            HighVariantsAttributes_Num[i] = 0;
        }
    }
         
    public int[] HighVariantsAttributesIndexes(){
        return HighVariantsAttributes; 
    }  
    
    public int[] getHighVariantsAttributes_Num(){
        return HighVariantsAttributes_Num;
    }
    
    public boolean SplitIsHappenedMC(){
        return this.SplitIsHappened;
    }
    
    public void MicroCluster_resetCF1XsumAndnsum(int numattrib){
        CF1XSum = new double[numattrib];
        nSum = new int[numattrib];
    }
    
    public void MicroCluster_resetCF1XsumAndnsum(){
        CF1XSum = new double[this.NumAttributes-1];
        nSum = new int[this.NumAttributes-1];
    }
    
    public double[] SplitDeathAlarming(){
       	 double[] resultSplitDeath=new double[2];
        	
       	 resultSplitDeath[0]=this.SplitCount;
       	 resultSplitDeath[1]=this.DeleteCount;
         
      	 return resultSplitDeath;
    }

    public void MicroCluster_resetVelocitySum(int numattrib){
        VelocitySum = new double[numattrib];
        VelocityNum = new int[numattrib];
    }
    
    public void MicroCluster_resetVelocitySum(){
        VelocitySum = new double[this.NumAttributes-1];
        VelocityNum = new int[this.NumAttributes-1];
    }
    
    public void MicroCluster_VelocitySum(){
        double Velocity_Value=0;
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = Clusters[ClassIndex][i];
                for(int j=0; j<this.NumAttributes-1; j++){
                    Velocity_Value = (double)MC.Centers[j] - MC.Centers_Old[j];
                    VelocitySum[j] += (double)Velocity_Value;
                    VelocityNum[j] ++;
                }
                MC.CopyData();
            }
        }
    }
    
    public double[] getVelocitySum(){
        return VelocitySum;
    }
    
    public int[] getVelocityNum(){
        return VelocityNum;
    }
     
    public void ResetAllMicroClusters(){
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = Clusters[ClassIndex][i];
                MC.ResetCluster();
            }
        }
    }
    
    public void MicroCluster_VelocityCalculate(){
        double Velocity_Value=0;
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            
            //System.out.println("ClassIndex: "+ClassIndex);
            
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = Clusters[ClassIndex][i];
                
                int zero = 0;
                String str = "";
                //System.out.println(" ");
                double[] Velocity_Value_Array = new double[this.NumAttributes-1];
                for(int j=0; j<this.NumAttributes-1; j++){
                    //Velocity_Value = (MC.Centers[j] - MC.Centers_Old[j])/(MC.CF1T - MC.CF1T_Old);
                    Velocity_Value = MC.Centers[j] - MC.Centers_Old[j];
                    
                    if(Velocity_Value==0)
                        zero++;
                    
                    Velocity_Value_Array[j]= Velocity_Value;
                
                }
                
                if(zero != this.NumAttributes-1){
                    for(int j=0; j<this.NumAttributes-1; j++){
                        StrBuffer.append(Velocity_Value_Array[j]);
                        StrBuffer.append(" ");
                    }
                    
                    StrBuffer.append(this.SplitCount);
                    StrBuffer.append(" ");
                    StrBuffer.append(this.DeleteCount);
                    StrBuffer.append(" ");
                    
                    StrBuffer.append("\n");
                }
                    
                MC.CopyData();
            }
        }
    }
    
    public void StringBuffer_Reset(){
        StrBuffer = new StringBuffer();
    }
    
    public void MicroCluster_ApplyCopyData(){
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = Clusters[ClassIndex][i];
                MC.CopyData();
            }
        }
    }
    
    public void MicroCluster_PrintCentersMCs(){
        System.out.println("  ");
        System.out.println(" Centers ");
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = Clusters[ClassIndex][i];
                for(int j = 0; j < MC.Centers.length; j++){
                    System.out.print((double)MC.Centers[j]+ " , ");
                }
                System.out.println(" ");
            }
        }
    }
    
    public void ResetInformationGainArrayFullPeriodTime(int numattributes){
        //if(rowInfoGain>0){
        //    InformationGainFullPeriodTime = new double[rowInfoGain+1][numattributes]; 
        //    InformationGainClassIndexFullPeriodTime = new String[rowInfoGain+1];
        //}else{
            InformationGainFullPeriodTime = new double[10000][numattributes]; 
            InformationGainClassIndexFullPeriodTime = new String[10000];
        //} 
        
        rowInfoGain = -1;
    }
    
    public void ResetInformationGainArrayFullPeriodTime(){
        //if(rowInfoGain>0){
        //    InformationGainFullPeriodTime = new double[rowInfoGain+1][this.NumAttributes]; 
        //    InformationGainClassIndexFullPeriodTime = new String[rowInfoGain+1];
        //}else{
            InformationGainFullPeriodTime = new double[10000][this.NumAttributes]; 
            InformationGainClassIndexFullPeriodTime = new String[10000];
        //} 
        rowInfoGain = -1;
    }
    
    public void InsertInformationGainArrayFullPeriodTime(){
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                System.out.println(" ");
                
                MicroCluster MC = Clusters[ClassIndex][i];
                rowInfoGain++;
                InformationGainFullPeriodTime[rowInfoGain][this.NumAttributes-1] = ClassIndex;
                InformationGainClassIndexFullPeriodTime[rowInfoGain] = String.valueOf(ClassIndex);
                
                for(int column=0; column<this.NumAttributes-1; column++){
                    InformationGainFullPeriodTime[rowInfoGain][column]=(double)MC.Centers[column];
                    System.out.print((double)MC.Centers[column]+" ");
                }
                System.out.print(String.valueOf(ClassIndex));
            }
        }
    }
    
    public void ComputeInformationGainFullPeriodTime(){
        /*
        It is used to compute IG after each training instance
        */
        double EntropyBefore = EntropyValueFullPeriodTime(InformationGainClassIndexFullPeriodTime);
        
        int row = rowInfoGain+1;//InformationGainFullPeriodTime.length;
        int column = InformationGainFullPeriodTime[0].length;
        double ClassIndexLabel = 0;
        double FeatureValue = 0;
        
        //System.out.println(" ");
        for(int j = 0; j<column-1; j++){
            Map<Double, String> MapFeatures = new HashMap<>();
            double EntropyAfter = 0;
            
            for(int i=0; i<row; i++){
                FeatureValue =  (double)InformationGainFullPeriodTime[i][j];
                ClassIndexLabel = InformationGainFullPeriodTime[i][column-1];
                
                //System.out.println(FeatureValue + " " + (int)ClassIndexLabel);
                
                if (MapFeatures.containsKey(FeatureValue)) {
                    MapFeatures.put(FeatureValue, MapFeatures.get(FeatureValue) + (int)ClassIndexLabel);
                } else {
                    MapFeatures.put(FeatureValue, String.valueOf((int)ClassIndexLabel));
                }
            }
            
            for (Map.Entry<Double, String> entry : MapFeatures.entrySet()) {
                int length = entry.getValue().length();
                String s = entry.getValue();
                double entropy = EntropyValue(s);
                //double e = entropy * (length/row);
                //System.out.print(" s: "+ s + " entropy: "+ entropy + " lenght: "+ length + " row: "+ row);
                EntropyAfter = EntropyAfter + (entropy * (length/row));
            }
            
            double InformationGainValue = EntropyBefore - EntropyAfter ;
            
            System.out.println(" EntropyBefore: " + EntropyBefore + " EntropyAfter: "+ EntropyAfter + "  InformationGainValue: "+InformationGainValue+ " ");
            
        }
        
        //System.out.println(InformationGainClassIndex + "    EntropyBefore: "+EntropyBefore);
    }
    
    public double EntropyValueFullPeriodTime(String[] features){
        String[] feature_data=features;
        double entropy;
        //int n = 0;
        
        Map<String, Integer> MapFeatures = new HashMap<>();
 
        for (int index = 0; index < feature_data.length; ++index) {
            String cx = feature_data[index];
            if (MapFeatures.containsKey(cx)) {
                MapFeatures.put(cx, MapFeatures.get(cx) + 1);
            } else {
                MapFeatures.put(cx, 1);
            }
            //++n;
        }
 
        double e = 0.0;
        for (Map.Entry<String, Integer> entry : MapFeatures.entrySet()) {
            //char cx = entry.getKey();
            double p = (double) entry.getValue() / feature_data.length; //using feature_data.length() instead of n
            e += p * log2(p);
        }
        entropy= -e;
    
        return entropy;
    }
    
    public void ResetInformationGainArray(){
        /*
        It is used to reset array after each training instance
        */
        InformationGainClassIndex = "";
        
        int rows = 0;
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            rows = rows + this.MicroClustersCount[ClassIndex];
        }    
        InformationGain = new double[rows][this.NumAttributes];  
    }
    
    public void InsertInformationGainArray(){
        /*
        It is used to save centers array after each training instance
        */
        //System.out.println(" ");
        
        int row = -1;
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = Clusters[ClassIndex][i];
                row++;
                InformationGain[row][this.NumAttributes-1] = ClassIndex;
                
                /*
                InformationGainClassIndex
                To be used for calculate entropy before (class indexes)
                */
                InformationGainClassIndex += ClassIndex;
                
                for(int column=0; column<this.NumAttributes-1; column++){
                    InformationGain[row][column]=(double)MC.Centers[column];
                    //System.out.print((double)MC.Centers[column]+" ");
                }
                //System.out.print(ClassIndex);
                //System.out.println(" ");
            }
        }
        
        //System.out.println(" ");
        //System.out.println(" ");
        //for(int i =0; i<row+1; i++){
        //    for(int j=0; j<this.NumAttributes; j++)
        //        System.out.print(InformationGain[i][j]+" ");
        //    System.out.println(" ");
        //}
    }
    
    public void ComputeInformationGain(){
        /*
        It is used to compute IG after each training instance
        */
        double EntropyBefore = EntropyValue(InformationGainClassIndex);
        
        int row = InformationGain.length;
        int column = InformationGain[0].length;
        double ClassIndexLabel = 0;
        double FeatureValue = 0;
        
        //System.out.println(" ");
        for(int j = 0; j<column-1; j++){
            Map<Double, String> MapFeatures = new HashMap<>();
            double EntropyAfter = 0;
            
            for(int i=0; i<row; i++){
                FeatureValue =  (double)InformationGain[i][j];
                ClassIndexLabel = InformationGain[i][column-1];
                
                //System.out.println(FeatureValue + " " + (int)ClassIndexLabel);
                
                if (MapFeatures.containsKey(FeatureValue)) {
                    MapFeatures.put(FeatureValue, MapFeatures.get(FeatureValue) + (int)ClassIndexLabel);
                } else {
                    MapFeatures.put(FeatureValue, String.valueOf((int)ClassIndexLabel));
                }
            }
            
            for (Map.Entry<Double, String> entry : MapFeatures.entrySet()) {
                int length = entry.getValue().length();
                String s = entry.getValue();
                double entropy = EntropyValue(s);
                //double e = entropy * (length/row);
                //System.out.print(" s: "+ s + " entropy: "+ entropy + " lenght: "+ length + " row: "+ row);
                EntropyAfter = EntropyAfter + (entropy * (length/row));
            }
            
            double InformationGainValue = EntropyBefore - EntropyAfter ;
            
            //System.out.println(" EntropyBefore: " + EntropyBefore + " EntropyAfter: "+ EntropyAfter + "  InformationGainValue: "+InformationGainValue+ " ");
            
        }
        
        //System.out.println(InformationGainClassIndex + "    EntropyBefore: "+EntropyBefore);
    }
    
    public double EntropyValue(String features){
        String feature_data=features;
        double entropy;
        //int n = 0;
        
        Map<Character, Integer> MapFeatures = new HashMap<>();
 
        for (int index = 0; index < feature_data.length(); ++index) {
            char cx = feature_data.charAt(index);
            if (MapFeatures.containsKey(cx)) {
                MapFeatures.put(cx, MapFeatures.get(cx) + 1);
            } else {
                MapFeatures.put(cx, 1);
            }
            //++n;
        }
 
        double e = 0.0;
        for (Map.Entry<Character, Integer> entry : MapFeatures.entrySet()) {
            //char cx = entry.getKey();
            double p = (double) entry.getValue() / feature_data.length(); //using feature_data.length() instead of n
            e += p * log2(p);
        }
        entropy= -e;
    
        return entropy;
    }
    
    public void MicroCluster_PrintCenters(){
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            System.out.println("ClassIndex: "+ClassIndex);
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroCluster MC = Clusters[ClassIndex][i];
                int MCID = MC.MicroClusterID;
                //String uuid = MC.uuid.toString();
                
                //System.out.println("          MicroClusterID: "+MCID);
                //System.out.println("                      UUID: "+uuid);
                
                String str = "";
                String str2 = "";
                for(int j=0; j<this.NumAttributes-1; j++){
                    str = str + String.valueOf(MC.Centers_Old[j])+" ";
                    str2 = str2 + String.valueOf(MC.Centers[j])+" ";
                }
                System.out.println("                         Old:"+str);
                System.out.println("                         New:"+str2);
            }
        }
    }
    
    public int[][] getCorrectPredictTestArray(){
        return CorrectPredictTestArray;
    }
    
    public void resetCorrectPredictTestArray(){
        CorrectPredictTestArray = new int[NumClasses][NumClasses];
    }
    
}