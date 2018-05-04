/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers;
import java.util.*; 
import java.io.*;
import java.math.BigDecimal;
//import moa.classifiers.MicroClusterSkipList.BoxPlotFeature;
import moa.classifiers.MicroClusterSkipList.Node;
import moa.classifiers.MicroClusterSkipList.SkipList;
//import MicroClustersVisualization.FeatureData_boxplot;
import weka.core.Instance;
import static weka.core.Utils.log2;
/**
 *
 * @author Mahmood Shakir
 * University of Reading
 * 2016
 */
public class MicroClusterManagerMedian implements Serializable {
    private static final long serialVersionUID = 1L;

    private MicroClusterMedian[][] Clusters; 
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
    public boolean DoublePunishFlagOption = true;  // true as default
    public boolean TriangleNumberApplyOption;
    public boolean LowPassFilterOption;
    public double AlphaLowPassFilter;
    public int MaxNodesOption;
    public int MaxHeadsOption;
    private int NumClasses = -1;
    private int NumAttributes = -1; 
    public String OutputPath  = "c:/";//"/home/shared/Dropbox/NETBEANS/SPARKCutdown/DATA/OUTPUT/MacroClusterDims";
    public boolean b_Initalised  = false;  
    public boolean PrintStatsDebug = false;
    //Kurtosis rate
    //public boolean CalculateKurtosisSum = true;
    //public int Max_FP_DIstanceTime = -1;
    /**
     * Added by Mahmood
     * Saving the last values of Split and Death
     */
    public int last_SplitCounter=0;
    public int last_DeathCounter=0;
    public StringBuffer StrBuffer = new StringBuffer();
    public boolean SplitIsHappened = false;
    int trainingcount = 0; 
    //public String[][] InformationGain;
    //public String InformationGainClassIndex;
    //public double[][] InformationGainFullPeriodTime;
    //public String[] InformationGainClassIndexFullPeriodTime;
    //public int rowInfoGain = -1;
    //public int[][] HighVariantsAttributes;
    public int[] HighVariantsAttributes;
    public int[] HighVariantsAttributes_Num;
    //
    public int[] Important_Outliers_Num;
    public int[] Extreme_Outliers_Num;
    //
    public double[] CF1XSum;
    public int[] nSum;
    //BoxPlot
    //public BoxPlotFeature[] BoxPlot_Features;
    //Range Rate
    //public double[] RangeSum;
    //public int[] RangeNum;
    public double[] VelocitySum;
    public int[] VelocityNum;
    // MOVED to the MC for independence testing 
    public double[] StreamSquareTotal; 
    public int[] StreamItemCount; 
    public double[] StreamTotal; 
    public double NumberSelectedFeatures = 0;
    public int[] trainNNIndex; 
    public double[] trainNNDistance; 
    public MicroClusterMedian[] trainNNMicroCluster; 
    // stastics for De-~Bugging 
    public int SplitCount = 0;
    public int InsertCount = 0;
    public int DeleteCount = 0;
    //public int CorrectPredict = 0;
    public int CorrectPredictTest = 0;
    public int[][] CorrectPredictTestArray;
    // Timer to print centers
    public int TimerWindow =0;
    
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
           
        double MinIndex[] = new double[2];
        //MinIndex[0]  = -1; // Inital ERROR Value.... 
        MinIndex[0]  = 0; // Inital ERROR Value.... 
        double MinVal = Double.MAX_VALUE;
        //Different OPTSD #       
            
        for (int i = 0; i < distances.length; i++) {
            if (distances[i][0] < MinVal ) {
                MinVal = distances[i][0];
                MinIndex[0] = i;
                MinIndex[1] = distances[i][1];
            }
        }            
            
            // [ClassID][INDEXinArray]
        return MinIndex;
    }
      
    public void SetClassandAttributeCount(int ClassCount , int AttributeCount){
        NumClasses=ClassCount;
        NumAttributes=AttributeCount; 
            
        ResetHighVariantsAttributesIndexes();
        SplitIsHappened = false;
    }
            
    private double[][] getAllDistances(int numClasses, double[] InstD)  //called by test and train
    {
        double[][] distances = new double[numClasses][2];
                
        for (int i = 0; i < numClasses; i++) {
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
    
    public void SetUPClusterArrays()
    {
        
        // choose the attribute from high to low based on the ratio given in SplitMultiAttributesOption
        NumberSelectedFeatures = Math.round((NumAttributes-1) * (SplitMultiAttributesOption / (float)100));        
        this.Clusters = new MicroClusterMedian[NumClasses][MAXClusterArraySizeOption];
        MicroClustersCount = new int[NumClasses];
        MicroClustersErrorCount = new int[NumClasses][MAXClusterArraySizeOption];    
        StreamSquareTotal = new double[NumClasses];
        StreamItemCount = new int[NumClasses];
        StreamTotal = new double[NumClasses];    
        trainNNIndex = new int[NumClasses]; 
        trainNNMicroCluster = new MicroClusterMedian[NumClasses];  
        trainNNDistance = new double[NumClasses];
        Centers_Array = new double[NumAttributes-1];
        
        for (int i = 0; i < NumClasses; i++) 
        {
            for (int j = 0; j < MAXClusterArraySizeOption; j++) 
            {
                Clusters[i][j] = new MicroClusterMedian(i,NumAttributes-1, this.StreamItemCount[i],LowPassFilterOption,AlphaLowPassFilter, MaxNodesOption, MaxHeadsOption);
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
    
    public MicroClusterMedian CheckOlderClusters_visualization() 
    {    
        MicroClusterMedian MColder = null;
        
        int MaxFalsePositives = 0;
        //int Max_i = 0;
        int MaxDistanceTime = 0;
        
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroClusterMedian MC = this.Clusters[ClassIndex][i]; 
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
    
    public void CheckAndRemoveClusters(int ClassIndex) throws IOException
    {            
        if (this.MicroClustersCount[ClassIndex] > 1)
        {
            //
            //System.out.println(" ------------------------- "+ this.MicroClustersCount[ClassIndex]);//(int)(MC.CF1T / MC.N)
            //
            //  for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++)             
            //{
            //  MicroClusterMedian MC = this.Clusters[ClassIndex][i];  
            //  //double ClusterWeight = MC.CalculateMyBigWeight(GetStreamCount(ClassIndex));
            //  //System.out.println("Current Time: "+ GetStreamCount(ClassIndex)+ "Intitial Time: "+ MC.CF1TInitial + " Last Timestamp: "+ MC.LastUpdateStreamCount + " Avg Time: "+ (int)(MC.CF1T / MC.N)+ " ClusterWeight: "+ClusterWeight);//
            //  System.out.println(" False Positive: "+MC.NumberParticipationsFalsePositive);
            //}  
              
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++)             
            {
                MicroClusterMedian MC = this.Clusters[ClassIndex][i]; 
                double ClusterWeight = MC.CalculateMyBigWeight(GetStreamCount(ClassIndex));
                //
                //System.out.println(" Last Timestamp: "+ MC.LastUpdateStreamCount + " ClassIndex: "+ ClassIndex);//(int)(MC.CF1T / MC.N)
                //
                if (RemovePoorlyPerformingClustersOption > ClusterWeight )
                {
                    if (this.trainingcount > 40000)
                    {
                        int nn =0 ;
                    }   
                        //double dd = MC.CalculateMyBigWeight(GetStreamCount(ClassIndex));
                    this.removeMicroCluster(ClassIndex, i);
                    //
                    //System.out.println("  ----- Removed ----- ");
                    //
                        //PrintClusters("<<<<<< REMOVAL");
                        
                    break;
                }
            }
        }
    }
    
    public void CheckAndRemoveClusters_FalsePositiveParticipation(int ClassIndex, int CurrentTimeStamp) throws IOException
    {            
        int MaxFalsePositives = 0;
        int Max_i = 0;
        int MaxDistanceTime = 0;
        
        if (this.MicroClustersCount[ClassIndex] > 1)
        {
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++)             
            {
                MicroClusterMedian MC = this.Clusters[ClassIndex][i]; 
                int FalsePositives = MC.NumberParticipationsFalsePositive;
                
                if(FalsePositives > 0){ // > MC.NumberParticipationsTruePositive
                    int ActualTimeStamp = (int)(MC.CF1T / MC.N);
                    int DistanceTime = CurrentTimeStamp - ActualTimeStamp; //Difference between current and actual time stamp
                    // Try the opposite way: Check distance firstly and FalsePositive secondly
                    if(DistanceTime > MaxDistanceTime){
                        if(FalsePositives > MaxFalsePositives){ //
                            MaxFalsePositives = FalsePositives;
                            MaxDistanceTime = DistanceTime;
                            Max_i = i;
                        }
                    }
                }
            }
            
            if(MaxFalsePositives > 0){
                this.removeMicroCluster(ClassIndex, Max_i);
            }
        }
    }
    
    //public void CheckAndRemoveClusters_FalsePositiveParticipation(int ClassIndex) throws IOException
    //{            
    //    int MaxFalsePositives = 0;
    //    int Max_i = 0;
    //    
    //    if (this.MicroClustersCount[ClassIndex] > 1)
    //    {
    //        for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++)             
    //        {
    //            MicroClusterMedian MC = this.Clusters[ClassIndex][i]; 
    //            int FalsePositives = MC.NumberParticipationsFalsePositive;
    //            
    //            if(FalsePositives > 0){ // > MC.NumberParticipationsTruePositive
    //                // Try the opposite way: Check distance firstly and FalsePositive secondly
    //                    if(FalsePositives > MaxFalsePositives){ //
    //                        MaxFalsePositives = FalsePositives;
    //                        Max_i = i;
    //                }
    //            }
    //        }
    //        
    //        if(MaxFalsePositives > 0){
    //            this.removeMicroCluster(ClassIndex, Max_i);
    //        }
    //    }
    //}
    
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
        
        for (int i = 0; i < this.Clusters.length; i++) 
        {
            try
            {
                if(TriangleNumberApplyOption){
                    CheckAndRemoveClusters(i);
                    //System.out.println("Tri Angle");
                }
                else
                    CheckAndRemoveClusters_FalsePositiveParticipation(i,this.GetStreamCount(ClassIndex));
                //
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
            double[] ClassIndexValues  = AllDistancesbyClass[i];
            trainNNIndex[i] = (int)ClassIndexValues[1];
            trainNNDistance[i] = ClassIndexValues[0];
            trainNNMicroCluster[i] =  this.Clusters[(ClassIndex)][trainNNIndex[i]];

            if (MinClassDistnace  >= trainNNDistance[i])
            {
                MinClassDistnace = trainNNDistance[i];
                MinClassIndex = i; 
            }
        }
            
            // Should we be adding this or a New Concept Cluster???? 
        //System.out.println(" ");
        //System.out.println(" trainingcount: " + trainingcount);
        trainNNMicroCluster[ClassIndex].IncrementCluster(InstD,this.GetStreamCount(ClassIndex),TriangleNumberApplyOption);
        
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
                    //System.out.println(" Remove Cluster and Insert New One ");
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
                 
        }else{ 
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
                    MicroClusterMedian MC_TruePositive = this.Clusters[ClassIndex][trainNNIndex[ClassIndex]];         
                    if(MC_TruePositive.NumberParticipationsFalsePositive>0){
                        MC_TruePositive.ParticipationFalsePositiveDecrement();
                        //System.out.println(" Decremented "+ MC_TruePositive.NumberParticipationsFalsePositive);
                    }
                        
                    //MC_TruePositive.ParticipationTruePositiveIncrement();
                }
            }
        }
    }  //end train

    public MicroClusterMedian MicroCluster_MaxError_visualization ()
    {        
        MicroClusterMedian MCmaxerror = null;
        int MaxError=0;
        
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroClusterMedian MC = this.Clusters[ClassIndex][i]; 
                if(this.MicroClustersErrorCount[ClassIndex][i] > MaxError){//ErrorOption
                    MaxError = this.MicroClustersErrorCount[ClassIndex][i];
                    MCmaxerror = MC;
                }
            }   
        }
        return MCmaxerror;
    }
        
    public void AttemptSplitsforMicroCluster(int ClassIndex,int  MCIndex )
    {        
        MicroClusterMedian MC = this.Clusters[ClassIndex][MCIndex];         
        if (MC.N==0)
        {
            int i =0; 
        }
                  
        if (MC.N > 1)
        {
            try
            {
                if (this.MicroClustersErrorCount[ClassIndex][MCIndex] > ErrorOption)
                {
                    SplitMicroCluster(MCIndex, ClassIndex);    //split
                }else{
                    //this.SplitAlarmIndex=0;
                    //System.out.println(" No Split");
                }
            }
            catch(Exception e)
            {
            }
        }else{
        
        }
    }
                
    public void removeMicroCluster(int ClassIndex, int MCIndex)
    {        
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
            MicroClusterMedian MC = Clusters[CLassIndex][i];
            double CheckValue  = MC.CalculateMyBigWeight(this.MicroClustersCount[CLassIndex]);
                
            if ((CheckValue )< LeastCount)
            {
                IndexLeast = i; 
                LeastCount = CheckValue;
            }
        }
        MicroClusterMedian TMP = Clusters[CLassIndex][IndexLeast];
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
                 
        MicroClusterMedian MCNew = Clusters[ClassIndex][MicroClustersCount[ClassIndex]-1];
        MCNew.ResetCluster();

        MCNew.IncrementCluster(instD,this.GetStreamCount(ClassIndex),TriangleNumberApplyOption);
             
        Clusters[ClassIndex][MicroClustersCount[ClassIndex]-1] = MCNew;
    }
        
    public void SplitMicroCluster(int MCIndex, int ClassIndex) throws IOException
    {        
        //int AttributeIndex  = 0;
        //double AttributeValue = 0;        
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
            
        MicroClusterMedian MCSplit = Clusters[ClassIndex][MCIndex];
        //MicroClusterMedian MCTemp = Clusters[ClassIndex][MCIndex];
        MicroClusterMedian MCNew = Clusters[ClassIndex][MCIndex];//new MicroClusterMedian(ClassIndex  , MCSplit.NumAttributes , 0 , LowPassFilterOption, AlphaLowPassFilter, MaxNodesOption, MaxHeadsOption); //It has been updated by Mark on 8/3/2016
               
        //SkipList NumberOutliersSorted = new SkipList(MCSplit.NumAttributes, MaxHeadsOption);
        
        //SkipList IQRsorted = new SkipList(MCSplit.NumAttributes+1, MaxHeadsOption);
        double[] Q1array = new double[MCSplit.NumAttributes];
        double[] Q3array = new double[MCSplit.NumAttributes];
        double maxIQR = -1;
        int maxFeatureIQR = 0;
        
        double maxOutliers_Important = -1;
        int maxFeature_Important = 0;
        
        double maxOutliers_Extreme = -1;
        int maxFeature_Extreme = 0;
        
        double MaxValue_Important_Last = 0;
        double MinValue_Important_Last = 0;
        
        double MaxValue_Extreme_Last = 0;
        double MinValue_Extreme_Last = 0;
        
        double median = 0;
        
        for(int feature=0; feature<MCSplit.NumAttributes; feature++){
            
            SkipList featureSkipList = GenerateSkipList(MCSplit.getFIFOnodes(feature));
            int numberNodes = featureSkipList.getNumberNodesHead(0);
            
            //if(numberNodes>0 & numberNodes<=3)
            //    System.out.println(numberNodes);
            
            //double[] FullSortedNodes = MCSplit.SkipList[feature].RetrieveNodes().clone();
            //for(int i=0; i<FullSortedNodes.length; i++)
            //    System.out.print(FullSortedNodes[i]+" ");
            //System.out.println();
            
            //int middle = FullSortedNodes.length/2;
            
            //double[] LowerBoundary = new double[middle];
            //System.arraycopy(FullSortedNodes, 0, LowerBoundary, 0, middle);
            
            //double[] UpperBoundary = new double[middle];
            //if (FullSortedNodes.length%2 == 1) {
            //    System.arraycopy(FullSortedNodes, middle+1, UpperBoundary, 0, middle);
            //}else{
            //    System.arraycopy(FullSortedNodes, middle, UpperBoundary, 0, middle);
            //}
            
            if(numberNodes > 1){
                
                double Q1;// = (double)getMedianQuartile(LowerBoundary);
                //double Q2 = (double)getMedianQuartile(FullSortedNodes);
                double Q3;// = (double)getMedianQuartile(UpperBoundary);
                //Node[] LowerUpperMedian = featureSkipList.getLowerUpperMedian();
            
                //if(LowerUpperMedian.length==2){
                //    Q1 = (double)LowerUpperMedian[0].getData(); // Q1 Lower Median
                //    Q3 = (double)LowerUpperMedian[1].getData(); // Q3 upper Median
                //    //System.out.println("Q1: "+ (double)LowerUpperMedian[0].getData()+" Median: "+MCSplit.SkipList[feature].getMedianNodeValue()+" Q3: "+(double)LowerUpperMedian[1].getData());
                //}else{
                //    Q1 = (double)(LowerUpperMedian[0].getData()+LowerUpperMedian[1].getData())/2; // Q1 Lower Median
                //    Q3 = (double)(LowerUpperMedian[2].getData()+LowerUpperMedian[3].getData())/2; // Q1 Lower Median
                //    //System.out.println("Q1: "+ ((double)(LowerUpperMedian[0].getData()+LowerUpperMedian[1].getData())/2)+" Median: "+MCSplit.SkipList[feature].getMedianNodeValue()+" Q3: "+((double)(LowerUpperMedian[2].getData()+LowerUpperMedian[3].getData())/2));
                //}
            
                double[] LowerUpperMedian = featureSkipList.getMedianQ1Q3();             
                Q1 = (double)LowerUpperMedian[0]; // Q1 Lower Median
                Q3 = (double)LowerUpperMedian[1]; // Q3 upper Median
                Q1array[feature] = (double)Q1;
                Q3array[feature] = (double)Q3;
                double IQR = (double) Q3 - Q1;
                
                if(IQR > maxIQR){
                    maxIQR = IQR;
                    maxFeatureIQR = feature;
                }
                
                //IQRsorted.addNode(IQR, feature);
                
                double MinValue = featureSkipList.getMinValue();
                double MaxValue = featureSkipList.getMaxValue();
                
                //BoxPlot
                
                //double[] arrayNodes = featureSkipList.RetrieveNodes();
                //median = featureSkipList.getMedian(arrayNodes);
                
                //BoxPlot_Features[feature].addQuartiles(MinValue, Q1, median, Q3, MaxValue);
                
                
                double MinLimit_Important = (double) Q1 - (1.5 * IQR); //weak outlier (1.5* IQR), strong outlier (3 * IQR)
                double MaxLimit_Important = (double) Q3 + (1.5 * IQR); //weak outlier (1.5* IQR), strong outlier (3 * IQR)
            
                double MinLimit_Extreme = (double) Q1 - (3 * IQR);
                double MaxLimit_Extreme = (double) Q3 + (3 * IQR);
                
                /*
                Analyse IQR
                */
                
                //boolean checkOutilers_Important = featureSkipList.checkOutliers(MaxLimit_Important, MinLimit_Important, MaxValue, MinValue);
                
                //Important Limit
                
                if(MaxValue > MaxLimit_Important || MinValue < MinLimit_Important){
                    //if((MaxValue_Important_Last > MaxValue & MinValue_Important_Last < MinValue) || MaxValue_Important_Last ==0){
                        
                        //MaxValue_Important_Last = MaxValue;
                        //MinValue_Important_Last = MinValue;
                        
                        //maxFeature_Important = feature;
                    //}
                    Important_Outliers_Num[feature]++;
        
                }
                
                //Extreme Limit
                
                if(MaxValue > MaxLimit_Extreme || MinValue < MinLimit_Extreme ){
                    //if((MaxValue_Extreme_Last > MaxValue & MinValue_Extreme_Last < MinValue) || MaxValue_Extreme_Last ==0){
                        
                        //MaxValue_Extreme_Last = MaxValue;
                        //MinValue_Extreme_Last = MinValue;
                        
                        //maxFeature_Extreme = feature;
                    //}
                    Extreme_Outliers_Num[feature]++;
                }
                
                //if(checkOutilers)
                //    HighVariantsAttributes[feature]++;
                
                //boolean applySplit = false;
            
                //if(SplitOnAllAttributesFlagOption || checkOutilers)//numOutliers > 0
                //    applySplit = true;
            
                if(SplitOnAllAttributesFlagOption){
                
                    /*
                    Reset MC Mean, FIFO, and CF1X of a feature
                    */
                    MCSplit.Centers[feature] = Q1; // Negative MC
                    MCNew.Centers[feature] = Q3; // Positive MC
               
                    MCSplit.FIFO[feature].resetFIFO();
                    MCNew.FIFO[feature].resetFIFO();
                    
                    MCSplit.FIFO[feature].addNode(Q1);
                    MCNew.FIFO[feature].addNode(Q3);
                    
                    MCSplit.CF1X[feature] = Q1;
                    MCNew.CF1X[feature] = Q3;
                    
                }
            
                /*
                Feature Selection can be applied for this feature with an outlier
                */
                //if(checkOutilers){ //numOutliers > 0
                //    // Feature Selectio here ....................
                //}
            }
        }
        
        double Q1,Q3;
        HighVariantsAttributes[maxFeatureIQR]++;
        //Important_Outliers_Num[maxFeature_Important]++;
        //Extreme_Outliers_Num[maxFeature_Extreme]++;
        
        if(!SplitOnAllAttributesFlagOption){
            /*
            Reset MC Mean, FIFO, and CF1X of a feature
            */
            Q1 = Q1array[maxFeatureIQR];
            Q3 = Q3array[maxFeatureIQR];
            
            MCSplit.Centers[maxFeatureIQR] = Q1; // Negative MC
            MCNew.Centers[maxFeatureIQR] = Q3; // Positive MC
               
            MCSplit.FIFO[maxFeatureIQR].resetFIFO();
            MCNew.FIFO[maxFeatureIQR].resetFIFO();
                    
            MCSplit.FIFO[maxFeatureIQR].addNode(Q1);
            MCNew.FIFO[maxFeatureIQR].addNode(Q3);
                    
            MCSplit.CF1X[maxFeatureIQR] = Q1;
            MCNew.CF1X[maxFeatureIQR] = Q3;
        }
        
        //int featureIndex=0;
        //double Q1,Q3;
        //int[] featureIndex_half = IQRsorted.RetrieveFeatureIndex(50); // 50 means half or Median
        //for(int i=0; i<featureIndex_half.length; i++){
        //    featureIndex = featureIndex_half[i];
        //    
        //    HighVariantsAttributes[featureIndex]++;
        //    
        //    if(!SplitOnAllAttributesFlagOption){
        //        /*
        //        Reset MC Mean, FIFO, and CF1X of a feature
        //        */
        //        Q1 = Q1array[featureIndex];
        //        Q3 = Q3array[featureIndex];
        //    
        //        MCSplit.Centers[featureIndex] = Q1; // Negative MC
        //        MCNew.Centers[featureIndex] = Q3; // Positive MC
        //       
        //        MCSplit.FIFO[featureIndex].resetFIFO();
        //        MCNew.FIFO[featureIndex].resetFIFO();
        //            
        //        MCSplit.FIFO[featureIndex].addNode(Q1);
        //        MCNew.FIFO[featureIndex].addNode(Q3);
        //            
        //        MCSplit.CF1X[featureIndex] = Q1;
        //        MCNew.CF1X[featureIndex] = Q3;
        //    }
        //}
            
        /*
        Retrieve number of outliers (sorted Low to High) for each feature 
        */
        //double[] NumberOutliersSorted_result = NumberOutliersSorted.RetrieveNodes();
        //int[] featureIndex_Nodes = NumberOutliersSorted.RetrieveTimeStampNodes();
        
        //MicroCluster MCNew = Clusters[ClassIndex][MicroClustersCount[ClassIndex]-1];
        
        int OldTime =0;
        
        if(TriangleNumberApplyOption){
            OldTime = (int)(MCSplit.CF1T / MCSplit.N);
        }
        
        MCSplit.ResetClusterSplit();
        MCNew.ResetClusterSplit();
           
        // NEW Time STAMP 
        //Updated with SkipList
        if(TriangleNumberApplyOption){
            if (SplitClusterGetsNewTimeStampsOption)
            {
                MCSplit.IncrementClusterTimeStamp(this.GetStreamCount(ClassIndex));
                MCNew.IncrementClusterTimeStamp(this.GetStreamCount(ClassIndex));
            }
            else
            {
                MCSplit.IncrementClusterTimeStamp(OldTime);
                MCNew.IncrementClusterTimeStamp(OldTime);
            }
        }
           
        Clusters[ClassIndex][MCIndex] = MCSplit;
        Clusters[ClassIndex][MicroClustersCount[ClassIndex]-1] = MCNew;
            
        //this.PrintClusters(">>>>>> NEW INSERT");
    }
    
    public SkipList GenerateSkipList(Object[] FIFOnodes)
    {
        SkipList featureSkipList = new SkipList(MaxNodesOption, MaxHeadsOption);
        
        for(int i=0; i<FIFOnodes.length; i++)
            featureSkipList.addNode((double)FIFOnodes[i],-1);
        
        return featureSkipList;
    }
    
    public SkipList GenerateSkipList_double(double[] FIFOnodes)
    {
        SkipList featureSkipList = new SkipList(MaxNodesOption, MaxHeadsOption);
        
        for(int i=0; i<FIFOnodes.length; i++)
            featureSkipList.addNode((double)FIFOnodes[i],-1);
        
        return featureSkipList;
    }
    
    public double getMedianQuartile(double[] array){
        int middle = array.length/2;
        if (array.length%2 == 1) {
            return (double)array[middle];
        } else {
            return (double)(array[middle-1] + array[middle]) / 2.0;
        }   
    }
    
    public Integer getNearsetMCIndex(int classIndex, double[] instd)
    {
        double iDist = Double.MAX_VALUE;
        int iIndex = 0; 
                        
        for (int i = 0; i < this.MicroClustersCount[classIndex]; i++) 
        {
            MicroClusterMedian MC = Clusters[classIndex][i];             
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
            MicroClusterMedian MC = Clusters[classIndex][i];             
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
    public void ResetManagerOptions(boolean LPF, double alphaLPF , int ErrorOptionD , int MAXClusterD, int RemovePoorlyD , int SplitMultiAttribD, boolean SplitOnAllAttribD, int MaxNodes, int MaxHeads, boolean TriangleOption){
        LowPassFilterOption = LPF;
        AlphaLowPassFilter = alphaLPF;
        ErrorOption = ErrorOptionD;
        MAXClusterArraySizeOption = MAXClusterD;
        RemovePoorlyPerformingClustersOption = RemovePoorlyD;
        SplitMultiAttributesOption = SplitMultiAttribD;
        SplitOnAllAttributesFlagOption = SplitOnAllAttribD;
        MaxNodesOption = MaxNodes;
        MaxHeadsOption = MaxHeads;
        TriangleNumberApplyOption = TriangleOption;
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
    
    public int[][] getCorrectPredictTestArray(){
        return CorrectPredictTestArray;
    }
    
    public void resetCorrectPredictTestArray(){
        CorrectPredictTestArray = new int[NumClasses][NumClasses];
    }
    
    public void MicroCluster_SwappingFeatures1And3SeaGeneratorOnlyWithDDM(){
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroClusterMedian MC = Clusters[ClassIndex][i];
                
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
    
    public void ResetOutliers_Num(int numattrib){
        Important_Outliers_Num = new int[numattrib];
        Extreme_Outliers_Num = new int[numattrib];
        
        for(int i =0;i<numattrib;i++){
            Important_Outliers_Num[i] = 0;
            Extreme_Outliers_Num[i] = 0;
        }    
    }
    
    public void ResetOutliers_Num(){
        Important_Outliers_Num = new int[this.NumAttributes-1];
        Extreme_Outliers_Num = new int[this.NumAttributes-1];
        
        for(int i =0;i<this.NumAttributes-1;i++){
            Important_Outliers_Num[i] = 0;
            Extreme_Outliers_Num[i] = 0;
        }    
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
    
    public int[] get_Important_Outliers_Num(){
        return Important_Outliers_Num;
    }
    
    public int[] get_Extreme_Outliers_Num(){
        return Extreme_Outliers_Num;
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
         
         //System.out.println("this.SplitCount: "+ this.SplitCount + " this.DeleteCount: "+this.DeleteCount);
         
      	 return resultSplitDeath;
    }

    public void MicroCluster_resetVelocitySum(int numattrib){
        //KurtosisSum = new double[numattrib];
        //KurtosisNum = new int[numattrib];
        //BoxPlot_Features = new BoxPlotFeature[numattrib];
        
        //for(int i=0; i<numattrib; i++)
        //    BoxPlot_Features[i] = new BoxPlotFeature();
        
        //RangeSum = new double[numattrib];
        //RangeNum = new int[numattrib];
        VelocitySum = new double[numattrib];
        VelocityNum = new int[numattrib];
    }
    
    public void MicroCluster_resetVelocitySum(){
        //KurtosisSum = new double[this.NumAttributes-1];
        //KurtosisNum = new int[this.NumAttributes-1];
        //BoxPlot_Features = new BoxPlotFeature[this.NumAttributes-1];
        
        //for(int i=0; i<this.NumAttributes-1; i++)
        //    BoxPlot_Features[i] = new BoxPlotFeature();
        
        //RangeSum = new double[this.NumAttributes-1];
        //RangeNum = new int[this.NumAttributes-1];
        VelocitySum = new double[this.NumAttributes-1];
        VelocityNum = new int[this.NumAttributes-1];
    }
   
    public int visualization_MaxIQR_FeatureIndex(MicroClusterMedian MC_maxError){
        int MaxIndex = 0;
        double maxIQR = 0;
        int num = 0;
        num = (int)MC_maxError.N;
        if(num>3){
            for(int j=0; j<MC_maxError.NumAttributes; j++){
                SkipList skiplistFeature = GenerateSkipList(MC_maxError.getFIFOnodes(j));
                double[] LowerUpperMedian = skiplistFeature.getMedianQ1Q3(); 
                double Q1Value = (double)LowerUpperMedian[0]; // Q1 Lower Median
                double Q3Value = (double)LowerUpperMedian[1]; // Q3 upper Median
                double IQR = Q3Value - Q1Value;
            
                if(IQR > maxIQR){
                    maxIQR = IQR;
                    MaxIndex = j;
                }
            }
        }
        return MaxIndex;
    }
       
    public void MicroCluster_VelocitySum(){
        
        double Velocity_Value=0;
        double Range_Value=0;
        
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroClusterMedian MC = Clusters[ClassIndex][i];
                
                //if(TriangleNumberApplyOption){
                //    int FalsePositives = MC.NumberParticipationsFalsePositive;
                //    if(FalsePositives > 0){ // > MC.NumberParticipationsTruePositive
                //        int ActualTimeStamp = (int)(MC.CF1T / MC.N);
                //        int CurrentTimeStamp = this.GetStreamCount(ClassIndex);
                //        int DistanceTime = CurrentTimeStamp - ActualTimeStamp; //Difference between current and actual time stamp
                //        // Try the opposite way: Check distance firstly and FalsePositive secondly
                //        if(DistanceTime > MaxDistanceTime){
                //            if(FalsePositives > MaxFalsePositives){ //
                //                MaxFalsePositives = FalsePositives;
                //                MaxDistanceTime = DistanceTime;
                //                Max_FP_DIstanceTime = i;
                //            }
                //        }
                //    }
                //}
                
                for(int j=0; j<this.NumAttributes-1; j++){
                    Range_Value = (double)MC.MaximumValue[j] - MC.MinimumValue[j];
                    Velocity_Value = (double)MC.Centers[j] - MC.Centers_Old[j];
                    
                    VelocitySum[j] += (double)Velocity_Value;
                    VelocityNum[j] ++;
                    
                    //RangeSum[j] += (double)Range_Value;
                    //RangeNum[j] ++;
                }
                
                MC.CopyData();
                
            }
        }
        
        //if(CalculateKurtosisSum){
        //    MicroCluster_KurtosisSum();
        //}
    }
    
    //public void MicroCluster_KurtosisSum(){
    //    
    //    //double MCcentroid_Value=0;
    //    
    //    for(int j=0; j<this.NumAttributes-1; j++){
    //        
    //        ArrayList<Double> MCcentroid_list=new ArrayList<Double>();//Creating arraylist
    //        
    //        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
    //            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
    //                MicroClusterMedian MC = Clusters[ClassIndex][i];
    //                MCcentroid_list.add((double)MC.Centers[j]);
    //            }
    //        }
    //        
    //        double STD = computeSTD(MCcentroid_list);
    //        double Mean = compute_mean(MCcentroid_list);
    //        double Kurtosis_SumVal =0;
    //        
    //        Iterator itr=MCcentroid_list.iterator();  
    //        while(itr.hasNext()){ 
    //            Kurtosis_SumVal = Kurtosis_SumVal + Math.pow(((double)itr.next() - Mean),4.0);
    //        }
    //        
    //        KurtosisSum[j] += (double)((Kurtosis_SumVal / MCcentroid_list.size())/Math.pow((STD),4.0)) - 3;
    //        KurtosisNum[j] ++;
    //        
    //        double kurva = (double)((Kurtosis_SumVal / MCcentroid_list.size())/Math.pow((STD),4.0)) - 3;
    //        System.out.println("J= "+j+ " Kurtosis_SumVal: "+Kurtosis_SumVal+" Kurtosis: "+ kurva);
    //    }      
    //}
    
    //public double computeSTD(ArrayList<Double> list) {
    //    double mean=0;
    //    double variance=0;
    //    double standard_dev=0;
    //    double sum = 0;
    //    /**
    //    * mean
    //    */
    //    
    //    Iterator itr_mean=list.iterator();  
    //        
    //    while(itr_mean.hasNext()){  
    //        sum = sum + (double)itr_mean.next();
    //    }
    //  
    //    mean=sum/list.size();
    //    /**
    //    * variance
    //    */
    //    sum=0;
    //    
    //    Iterator itr_variance=list.iterator();  
    //        
    //    while(itr_variance.hasNext()){  
    //        sum=sum + Math.pow(((double)itr_variance.next()-mean),2.0);
    //    }
    //   
    //    variance=sum/list.size();
    //    /**
    //    * standard deviation
    //    */
    //    standard_dev= Math.sqrt(variance);
    //    return standard_dev;
    // }
    
    //public double compute_mean(ArrayList<Double> list) {
    //    
    //    double sum = 0;
    //    /**
    //    * mean
    //    */
    //    
    //    Iterator itr_mean=list.iterator();  
    //        
    //    while(itr_mean.hasNext()){  
    //        sum = sum + (double)itr_mean.next();
    //    }
    // 
    //    return sum/list.size();
    //}
    
    //public double[] getKurtosisSum(){
    //    return KurtosisSum;
    //}
    
    //public int[] getKurtosisNum(){
    //    return KurtosisNum;
    //}
    
    //public void printBoxPlot(){
    //    for(int i=0; i<BoxPlot_Features.length; i++)
    //        BoxPlot_Features[i].printQuqrtiles();
    //}
    
    //public double[] getRangeSum(){
    //    return RangeSum;
    //}
    
    //public int[] getRangeNum(){
    //    return RangeNum;
    //}
    
    public double[] getVelocitySum(){
        return VelocitySum;
    }
    
    public int[] getVelocityNum(){
        return VelocityNum;
    }
     
    public void ResetAllMicroClusters(){
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroClusterMedian MC = Clusters[ClassIndex][i];
                MC.ResetCluster();
            }
        }
    }
    
    public void MicroCluster_VelocityCalculate(){
        double Velocity_Value=0;
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            
            //System.out.println("ClassIndex: "+ClassIndex);
            
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroClusterMedian MC = Clusters[ClassIndex][i];
                
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
                MicroClusterMedian MC = Clusters[ClassIndex][i];
                MC.CopyData();
            }
        }
    }
    
    public void MicroCluster_PrintCentersMCs(){
        TimerWindow ++;
        System.out.println();
        System.out.println(" Centers "+TimerWindow);
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroClusterMedian MC = Clusters[ClassIndex][i];
                for(int j = 0; j < MC.Centers.length; j++){
                    System.out.print((double)MC.Centers[j]+ " ");
                    
                }
                System.out.println(" ");
            }
        }
    }
    
    //public void ResetInformationGainArrayFullPeriodTime(int numattributes){
    //    //if(rowInfoGain>0){
    //    //    InformationGainFullPeriodTime = new double[rowInfoGain+1][numattributes]; 
    //    //    InformationGainClassIndexFullPeriodTime = new String[rowInfoGain+1];
    //    //}else{
    //        InformationGainFullPeriodTime = new double[10000][numattributes]; 
    //        InformationGainClassIndexFullPeriodTime = new String[10000];
    //    //} 
    //    
    //    rowInfoGain = -1;
    //}
    
    //public void ResetInformationGainArrayFullPeriodTime(){
    //    //if(rowInfoGain>0){
    //    //    InformationGainFullPeriodTime = new double[rowInfoGain+1][this.NumAttributes]; 
    //    //    InformationGainClassIndexFullPeriodTime = new String[rowInfoGain+1];
    //    //}else{
    //        InformationGainFullPeriodTime = new double[10000][this.NumAttributes]; 
    //        InformationGainClassIndexFullPeriodTime = new String[10000];
    //    //} 
    //    rowInfoGain = -1;
    //}
    
    //public void InsertInformationGainArrayFullPeriodTime(){
    //    for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
    //        for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
    //            System.out.println(" ");
    //            
    //            MicroClusterMedian MC = Clusters[ClassIndex][i];
    //            rowInfoGain++;
    //            InformationGainFullPeriodTime[rowInfoGain][this.NumAttributes-1] = ClassIndex;
    //            InformationGainClassIndexFullPeriodTime[rowInfoGain] = String.valueOf(ClassIndex);
    //            
    //            for(int column=0; column<this.NumAttributes-1; column++){
    //                InformationGainFullPeriodTime[rowInfoGain][column]=(double)MC.Centers[column];
    //                System.out.print((double)MC.Centers[column]+" ");
    //            }
    //            System.out.print(String.valueOf(ClassIndex));
    //        }
    //    }
   // }
    
    //public void ComputeInformationGainFullPeriodTime(){
    //    /*
    //    It is used to compute IG after each training instance
    //    */
    //    double EntropyBefore = EntropyValueFullPeriodTime(InformationGainClassIndexFullPeriodTime);
    //    
    //    int row = rowInfoGain+1;//InformationGainFullPeriodTime.length;
    //    int column = InformationGainFullPeriodTime[0].length;
    //    double ClassIndexLabel = 0;
    //    double FeatureValue = 0;
    //    
    //    //System.out.println(" ");
    //    for(int j = 0; j<column-1; j++){
    //        Map<Double, String> MapFeatures = new HashMap<>();
    //        double EntropyAfter = 0;
    //        
    //        for(int i=0; i<row; i++){
    //            FeatureValue =  (double)InformationGainFullPeriodTime[i][j];
    //            ClassIndexLabel = InformationGainFullPeriodTime[i][column-1];
    //            
    //            //System.out.println(FeatureValue + " " + (int)ClassIndexLabel);
    //            
    //            if (MapFeatures.containsKey(FeatureValue)) {
    //                MapFeatures.put(FeatureValue, MapFeatures.get(FeatureValue) + (int)ClassIndexLabel);
    //            } else {
    //                MapFeatures.put(FeatureValue, String.valueOf((int)ClassIndexLabel));
    //            }
    //        }
    //        
    //        for (Map.Entry<Double, String> entry : MapFeatures.entrySet()) {
    //            int length = entry.getValue().length();
    //            String s = entry.getValue();
    //            double entropy = EntropyValue(s);
    //            //double e = entropy * (length/row);
    //            //System.out.print(" s: "+ s + " entropy: "+ entropy + " lenght: "+ length + " row: "+ row);
    //            EntropyAfter = EntropyAfter + (entropy * (length/row));
    //        }
    //        
    //        double InformationGainValue = EntropyBefore - EntropyAfter ;
    //        
    //        System.out.println(" EntropyBefore: " + EntropyBefore + " EntropyAfter: "+ EntropyAfter + "  InformationGainValue: "+InformationGainValue+ " ");
    //        
    //    }    
    //    //System.out.println(InformationGainClassIndex + "    EntropyBefore: "+EntropyBefore);
    //}
    
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
    
    //public void ResetInformationGainArray(int rows, int columns){
    //    /*
    //    It is used to reset array after each training instance
    //    */
    //    InformationGainClassIndex = "";
    //    
    //    //for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
    //    //    rows = rows + this.MicroClustersCount[ClassIndex];
    //    //}
    //    
    //    InformationGain = new String[rows][columns];  
    //}
    
    //public void InsertInformationGainArray(Instance instanc, int row){
    //    
    //    //int ClassIndex = (int) instanc.classValue();
    //    String ClassValue = (String) instanc.toString(this.NumAttributes-1);
    //    System.out.println(ClassValue);
    //    
    //    InformationGain[row][this.NumAttributes-1] = ClassValue;
    //    
    //    for (int column = 0; column < this.NumAttributes-1; column++){
    //        InformationGain[row][column]=instanc.toString(column);
    //    }
    //}
    
    //public void InsertInformationGainArray(){
    //    /*
    //    It is used to save centers array after each training instance
    //    */
    //    //System.out.println(" ");
    //    
    //    int row = -1;
    //    for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
    //        for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
    //            MicroClusterMedian MC = Clusters[ClassIndex][i];
    //            row++;
    //            InformationGain[row][this.NumAttributes-1] = ClassIndex;
    //            
    //            /*
    //            InformationGainClassIndex
    //            To be used for calculate entropy before (class indexes)
    //            */
    //            InformationGainClassIndex += ClassIndex;
    //            
    //            for(int column=0; column<this.NumAttributes-1; column++){
    //                InformationGain[row][column]=(double)MC.Centers[column];
    //                //System.out.print((double)MC.Centers[column]+" ");
    //            }
    //            //System.out.print(ClassIndex);
    //            //System.out.println(" ");
    //        }
    //    }
    //    
    //    //System.out.println(" ");
    //    //System.out.println(" ");
    //    //for(int i =0; i<row+1; i++){
    //    //    for(int j=0; j<this.NumAttributes; j++)
    //   //        System.out.print(InformationGain[i][j]+" ");
    //    //    System.out.println(" ");
    //    //}
    //}
    
    //public void ComputeInformationGain(){
    //    /*
    //    It is used to compute IG after each training instance
    //    */
    //    double EntropyBefore = EntropyValue(InformationGainClassIndex);
    //    
    //    int row = InformationGain.length;
    //    int column = InformationGain[0].length;
    //    String ClassIndexLabel = "";
    //    String FeatureValue = "";
    //    
    //    //System.out.println(" ");
    //    for(int j = 0; j<column-1; j++){
    //        Map<String, String> MapFeatures = new HashMap<>();
    //        double EntropyAfter = 0;
    //        
    //        for(int i=0; i<row; i++){
    //            FeatureValue =  (String)InformationGain[i][j];
    //            ClassIndexLabel = (String)InformationGain[i][column-1];
    //            
    //            //System.out.println(FeatureValue + " " + (int)ClassIndexLabel);
    //            
    //            if (MapFeatures.containsKey(FeatureValue)) {
    //                MapFeatures.put(FeatureValue, MapFeatures.get(FeatureValue) + ClassIndexLabel);
    //            } else {
    //                MapFeatures.put(FeatureValue, String.valueOf(ClassIndexLabel));
    //            }
    //        }
    //        
    //        for (Map.Entry<String, String> entry : MapFeatures.entrySet()) {
    //            int length = entry.getValue().length();
    //            String s = entry.getValue();
    //            double entropy = EntropyValue(s);
    //            //double e = entropy * (length/row);
    //            //System.out.print(" s: "+ s + " entropy: "+ entropy + " lenght: "+ length + " row: "+ row);
    //            EntropyAfter = EntropyAfter + (entropy * (length/row));
    //        }
    //        
    //        double InformationGainValue = EntropyBefore - EntropyAfter ;
    //        
    //        //System.out.println(" EntropyBefore: " + EntropyBefore + " EntropyAfter: "+ EntropyAfter + "  InformationGainValue: "+InformationGainValue+ " ");
    //        
    //    }
    //    
    //    //System.out.println(InformationGainClassIndex + "    EntropyBefore: "+EntropyBefore);
    //}
    
    //public double EntropyValue(String features){
    //    String feature_data=features;
    //    double entropy;
    //    //int n = 0;
    //    
    //    Map<Character, Integer> MapFeatures = new HashMap<>();
    //
    //    for (int index = 0; index < feature_data.length(); ++index) {
    //        char cx = feature_data.charAt(index);
    //        if (MapFeatures.containsKey(cx)) {
    //            MapFeatures.put(cx, MapFeatures.get(cx) + 1);
    //        } else {
    //            MapFeatures.put(cx, 1);
    //        }
    //        //++n;
    //    }
    //
    //    double e = 0.0;
    //    for (Map.Entry<Character, Integer> entry : MapFeatures.entrySet()) {
    //        //char cx = entry.getKey();
    //        double p = (double) entry.getValue() / feature_data.length(); //using feature_data.length() instead of n
    //        e += p * log2(p);
    //    }
    //    entropy= -e;
    //
    //    return entropy;
    //}
    
    public void MicroCluster_PrintCenters(){
        for(int ClassIndex = 0; ClassIndex<NumClasses; ClassIndex++){
            System.out.println("ClassIndex: "+ClassIndex);
            for (int i = 0; i < this.MicroClustersCount[ClassIndex]; i++){
                MicroClusterMedian MC = Clusters[ClassIndex][i];
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
}
