/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers.core.driftdetection;
import MathPackages.NormalizeAttributes;
import moa.core.ObjectRepository;
import moa.options.IntOption;
import moa.tasks.TaskMonitor;
import weka.core.Instance;
import MathPackages.StandardDeviation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import moa.classifiers.MicroClusterManager;
import moa.classifiers.MicroClustersSingle;
import MathPackages.PercentageDifference;
import moa.classifiers.MicroClusterManagerMedian;
//import MicroClustersVisualization.FeatureData_boxplot;
import java.util.HashMap;
import java.util.Map;
import moa.classifiers.MicroClusterSkipList.SkipList;
import moa.classifiers.MicroClustersSingleMedian;
import moa.options.FlagOption;
import moa.options.FloatOption;
import static weka.core.Utils.log2;
/**
 *
 * @author Mahmood Shakir 
 * University of Reading 
 * 2016
 */
public class MicroCluster_FeatureTracker_MCNN_InterquartileRange extends AbstractChangeDetector {
    private static final long serialVersionUID = 1L;
    
    //The rest characters that can be used = f, h, i, j, k, m 
    
    //public boolean TriangleNumberApplyOption = false;
    
    //public FlagOption ContinuousFeatures_InfoGain_Option = new FlagOption(
    //		        "ContinuousFeatures_InfoGain",'f', "Dataset with Continuous Features");
     
    public FlagOption TriangleNumberApplyOption = new FlagOption(
		        "TriangleNumberApply",'o', "Triangle Number Apply for Removing MCs");
    
    public FlagOption FeatureSelectionOption = new FlagOption(
		        "FeatureSelection",'s', "Feature selection ");

    public FlagOption CreateNewInstanceOption = new FlagOption(
		        "CreateNewInstance",'v', "Create new instance with feature selection ");
    
    public IntOption minNumInstancesOption = new IntOption(
            "WindowSize",
            'n',
            "The minimum number of instances before permitting detecting change.",
            1000, 2, Integer.MAX_VALUE);
    
    public IntOption PercentageDifferenceOption = new IntOption(
	        "PercentageDifference",'p', "The Percentage Difference between 2 values (Split and Death rates).",
	         50, 0, 100);
    
    //public FloatOption LowPassFilterLPFOption = new FloatOption(
//		        "LowPassFilter",'k', "Low Pass Filter.",
  //              0.5, 0 , 1);
    
    public IntOption ErrorOption = new IntOption(
	        "ErrorOption",'a', "Error option.",
	         10, 0, Integer.MAX_VALUE);
    
    public IntOption MAXClusterArraySizeOption = new IntOption(
	        "MAXClusterArraySizeOption",'b', "MAXClusterArraySizeOption.",
	         100, 0, Integer.MAX_VALUE);
    
    public IntOption RemovePoorlyPerformingClustersOption = new IntOption(
	        "RemovePoorlyPerformingClustersOption",'c', "MAXClusterArraySizeOption.",
	         50, 0, 100);
    
    public FlagOption SplitOnAllAttributesFlagOption = new FlagOption(
		        "SplitOnAllAttributesFlagOption",'e', "SplitOnAllAttributesFlagOption");
    
    public IntOption SplitMultiAttributesOption = new IntOption(
	        "SplitMultiAttributesOption",'d', "SplitMultiAttributesOption.",
	         50, 0, 100);
    
    public FlagOption LowPassFilterOption = new FlagOption(
		        "LowPassFilter",'u', "Low Pass Filter.");
    
    public IntOption AlphaOption = new IntOption(
	        "AlphaLowPassFilter",'x', "Alpha for Low Pass Filter.",
	         50, 0, 100);
    
    public FlagOption NormalizeTheAttributesOption = new FlagOption(
		        "Normalization",'t', "Normalize the attributes before training ");
    
    public IntOption NumAttributesOption = new IntOption(
	        "NumAttributesForResetNormalization",'w', "Number of attributes to be selected for reset normalization.",
	         50, 25, 100);
    
    public IntOption RangeMinimumNormalizationOption = new IntOption(
	        "RangeMinimumNormalization",'r', "Range minimum normalization of attributes.",
	         0, 0, Integer.MAX_VALUE);
    
    public IntOption RangeMaximumNormalizationOption = new IntOption(
	        "RangeMaximumNormalization",'g', "Range maximum normalization of attributes.",
	         100, 0, Integer.MAX_VALUE);
    
    public IntOption MaxHeadsOption = new IntOption(
	        "MaxHeadsSkipListOption",'y', "Maximum number of heads of SkipList.",
	         20, 10, 50);
    
    public IntOption MaxNodesRate = new IntOption(
	        "MaxNodesSkipListRate",'z', "Rate of Maximum number of nodes of SkipList.",
	         100, 0, 100); //Defaut 150 nodes
    
    public static StandardDeviation standard_deviation_equation = new StandardDeviation();
    //public static NormalDistribution NormalDistribution = new NormalDistribution();
    public static StringBuffer SB  =  new StringBuffer();
    public MicroClustersSingleMedian MCS = new MicroClustersSingleMedian();
    public MicroClusterManagerMedian MCM = new MicroClusterManagerMedian();
    //public static PercentageDifference PercentageDifference_class = new PercentageDifference();
    //public static NormalizeAttributes NormalizeAttributes_class = new NormalizeAttributes();
    private String filePath = "c:/FeatureTracker_Statistics.xls";
    public int WindowSize_numberInstances;
    private double splitNum;
    private double deathNum;
    private int count_window=0;
    private double last_splitNum=0;
    private double last_deathNum=0;
    private double SplitMeanLowestValueHistory_Threshold=0;
    private double DeathMeanLowestValueHistory_Threshold=0;
    public int numAttributes=0;
    private double last_splitNum_MM=0;
    private double last_deathNum_MM=0;
    private boolean initial_arrays = true;
    private StringBuffer StrBuffer = new StringBuffer();
    public double[] SplitDeathSum;
    private int[] HighVariantsAttributes_Old;
    private double[] Normalize_Max_AttribValu;
    private double[] Normalize_Min_AttribValu;
    private boolean initial_normalization = true;
    private boolean initial_TitleReport = true;
    private double AlphaValue = 0;
    private double NumberSelectedFeatures;
    private int MaxNodesSkipList = 0;//(int)minNumInstancesOption.getValue() / 2;
    private boolean DriftStarted = false;
    private double oldSplitNumFS=0;
    private double oldDeathNumFS=0;
    private int[] FeatureSelected;
    private boolean ApplyFeatureSelection = false;
    private Instance InstanceToClassifierData;
    public boolean EndOfWindow = false;
    private int NumClasses = 2;
    public boolean DriftIsDetected = false;
    public String[][] InformationGain;
    public String InformationGainClassIndex = "";
    //public double[] InformationGain_Old;
    public double[] InformationGain_Feature;
    public double[] InformationGain_Feature_BecameRelevant;
    public double[] InformationGain_Print;
    public double EntropyBefore = -1;
    public int[][] CountGreaterSmaller = new int[NumClasses][2]; // Column 0 for <=, Column 1 for >
    public String[] Classes = new String[NumClasses];
    public boolean firstWindow = false;
    //public boolean DataSetRealNumbers = true;
            
    public MicroCluster_FeatureTracker_MCNN_InterquartileRange() throws IOException {
        initialFile(filePath);
        resetLearning();
    }
    
    @Override
    public void resetLearning() {
        WindowSize_numberInstances=0;
        splitNum=0;
    	deathNum=0;
    }
    
    public void resetArrays(int numInstances, int numAttributes){
        StrBuffer = new StringBuffer();
    }

    @Override
    public boolean FeatureSelectionIsApplied() {
        return ApplyFeatureSelection;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] FeatureSelectedIndexes() {
        return FeatureSelected;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[][] HighVariantsAttributesIndexes() {
        return null;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void InputInstance(Instance inst) {
        
        if(this.initial_arrays){
            NumClasses = inst.numClasses();
            
            Classes = new String[NumClasses];
            
            for(int iClasses = 0; iClasses<NumClasses; iClasses++)
                Classes[iClasses] = (String)inst.classAttribute().value(iClasses);
            
            
            SplitDeathSum = new double[2];
            
            FeatureSelected = new int[inst.numAttributes()-1];
            
            for(int i=0; i < inst.numAttributes()-1; i++)
                FeatureSelected[i] = 0;
            
            MCS.MicroCluster_resetVelocitySum(inst.numAttributes()-1);
            
            HighVariantsAttributes_Old = new int[inst.numAttributes()-1];
            
            for(int i =0;i<inst.numAttributes()-1;i++)
                HighVariantsAttributes_Old[i] = 0;
            
            MCS.MicroCluster_ResetHighVariantsAttributesIndexes(inst.numAttributes()-1);
            MCS.MicroCluster_ResetOutliers_Num(inst.numAttributes()-1);
            numAttributes = inst.numAttributes()-1;
            
            //NormalizeAttributes_class.resetArrays(numAttributes);
            
            resetArrays(this.minNumInstancesOption.getValue(),numAttributes);
            
            Normalize_Max_AttribValu = new double[inst.numAttributes()];
            Normalize_Min_AttribValu = new double[inst.numAttributes()];
            
            InformationGain_Feature = new double[inst.numAttributes()];
            InformationGain_Print = new double[inst.numAttributes()];
            InformationGain_Feature_BecameRelevant = new double [inst.numAttributes()];
            
            for(int AttribIndex = 0 ; AttribIndex < inst.numAttributes() - 1 ; AttribIndex++){
                Normalize_Max_AttribValu[AttribIndex] = 0;
                Normalize_Min_AttribValu[AttribIndex] = 0;
                
                InformationGain_Feature[AttribIndex] = 0;
                InformationGain_Print[AttribIndex] = 0;
                InformationGain_Feature_BecameRelevant[AttribIndex] = 0;
            }
            
            // reset Information Gain 
            
            InformationGain = new String[this.minNumInstancesOption.getValue()][inst.numAttributes()];
            
            /*
            Set number of attributes to be selected for reset Max Min Online Normalization 
            Median filter
            */
            NumberSelectedFeatures = Math.round(numAttributes * (NumAttributesOption.getValue() / (float)100));
            
            /*
            Calculate AlphaValue
            */
            AlphaValue = AlphaOption.getValue() / (float)100;
            
            MaxNodesSkipList = 1000;//Math.round(minNumInstancesOption.getValue() * (MaxNodesRate.getValue() / (float)100));//1000;//150;//
            //
            boolean TriangleNumberOption = false;
            if (TriangleNumberApplyOption.isSet()){
                TriangleNumberOption = true;
            }
            
            if(LowPassFilterOption.isSet() & SplitOnAllAttributesFlagOption.isSet()){
                MCS.ResetMMOptions(true, AlphaValue, ErrorOption.getValue() ,MAXClusterArraySizeOption.getValue() , RemovePoorlyPerformingClustersOption.getValue() , SplitMultiAttributesOption.getValue() , true , MaxNodesSkipList, MaxHeadsOption.getValue(),TriangleNumberOption);
            }else if(LowPassFilterOption.isSet() || SplitOnAllAttributesFlagOption.isSet()){
                if(LowPassFilterOption.isSet()){
                    MCS.ResetMMOptions(true, AlphaValue, ErrorOption.getValue() ,MAXClusterArraySizeOption.getValue() , RemovePoorlyPerformingClustersOption.getValue() , SplitMultiAttributesOption.getValue() , false , MaxNodesSkipList, MaxHeadsOption.getValue(),TriangleNumberOption);
                }
                
                if(SplitOnAllAttributesFlagOption.isSet()){
                    MCS.ResetMMOptions(false, AlphaValue, ErrorOption.getValue() ,MAXClusterArraySizeOption.getValue() , RemovePoorlyPerformingClustersOption.getValue() , SplitMultiAttributesOption.getValue() , true , MaxNodesSkipList, MaxHeadsOption.getValue(),TriangleNumberOption);
                }
            } else{
                MCS.ResetMMOptions(false, AlphaValue, ErrorOption.getValue() ,MAXClusterArraySizeOption.getValue() , RemovePoorlyPerformingClustersOption.getValue() , SplitMultiAttributesOption.getValue() , false , MaxNodesSkipList, MaxHeadsOption.getValue(),TriangleNumberOption);
            }
            
            this.initial_arrays=false;
        }
        
	if(this.WindowSize_numberInstances >= this.minNumInstancesOption.getValue()){ 
	    //System.out.println(count_window + " "+ this.splitNum+" "+this.deathNum);//+" "+ this.estimation);
	    count_window++;
	}
		
	if (this.WindowSize_numberInstances >= this.minNumInstancesOption.getValue() || this.isChangeDetected == true || this.isInitialized == false) { //  		
	    resetLearning();
            this.isInitialized = true;
            SplitDeathSum = new double[2];
        }
        
        // Insert the new instance to Information Gain Array
        if(FeatureSelectionOption.isSet())
            InsertInformationGainArray(inst, WindowSize_numberInstances);
        
	WindowSize_numberInstances++;
                
        //this.index=0;
        this.estimation = 0.0;
        this.isChangeDetected = false;
        this.isWarningZone = false;
        this.delay = 0;
        this.EndOfWindow = false;
        DriftIsDetected = false;
        
        double[] splitdeathAlarmPointers = new double[2];
        
        /*
        Online Normalization
        */
        
        if(this.NormalizeTheAttributesOption.isSet()){
            
            splitdeathAlarmPointers = MCS.SplitDeathAlarm(OnlineNormalization_Compute(inst),true);
            
        }else{
            
            splitdeathAlarmPointers = MCS.SplitDeathAlarm(inst,false);
            
        }
        
        /*
        sum split and death for velocity
        */
        SplitDeathSum[0] += splitdeathAlarmPointers[0]-last_splitNum_MM; //Split rate
        SplitDeathSum[1] += splitdeathAlarmPointers[1]-last_deathNum_MM; //Death rate
        
        /**
         * Counting split MC-NN
         */
        if (splitdeathAlarmPointers[0]>last_splitNum_MM){
            this.splitNum++;
       	    last_splitNum_MM=splitdeathAlarmPointers[0];
        }
        
        /*
        * Counting death MC-NN
        */
        if (splitdeathAlarmPointers[1]>last_deathNum_MM){
            this.deathNum++;
            last_deathNum_MM=splitdeathAlarmPointers[1];
        }
                   
        if (this.WindowSize_numberInstances < this.minNumInstancesOption.getValue()) {
            return;
        }
              
        /*
        Split mean and Dynamic threshold for split
        */
        double NewSplitRate = Math.abs((double) SplitDeathSum[0] / this.WindowSize_numberInstances);
        double SplitPercentageDiff = PercentageDifferenceCompute(this.last_splitNum, NewSplitRate);
        double SplitMean_Threshold = (this.last_splitNum + NewSplitRate) / 2;
        
        /*
        Death mean and Dynamic threshold for split
        */
        double NewDeathRate = Math.abs((double) SplitDeathSum[1] / this.WindowSize_numberInstances);
        double DeathPercentageDiff = PercentageDifferenceCompute(this.last_deathNum, NewDeathRate);
        double DeathMean_Threshold = (this.last_deathNum + NewDeathRate) / 2;
        
        if(NewSplitRate > SplitMean_Threshold & SplitPercentageDiff > this.PercentageDifferenceOption.getValue() & SplitMean_Threshold > SplitMeanLowestValueHistory_Threshold & NewDeathRate > DeathMean_Threshold & DeathPercentageDiff > this.PercentageDifferenceOption.getValue() & DeathMean_Threshold > DeathMeanLowestValueHistory_Threshold){
            this.isChangeDetected = true;
        }else if((NewSplitRate > SplitMean_Threshold & SplitPercentageDiff > this.PercentageDifferenceOption.getValue() & SplitMean_Threshold > SplitMeanLowestValueHistory_Threshold) || (NewDeathRate > DeathMean_Threshold & DeathPercentageDiff > this.PercentageDifferenceOption.getValue() & DeathMean_Threshold > DeathMeanLowestValueHistory_Threshold)){
            this.isWarningZone = true;
            //if(NewSplitRate > SplitMean_Threshold & SplitPercentageDiff > this.PercentageDifferenceOption.getValue() & SplitMean_Threshold > SplitMeanLowestValueHistory_Threshold){
            //   this.isWarningZone = true;
            //}
            
            //if(NewDeathRate > DeathMean_Threshold & DeathPercentageDiff > this.PercentageDifferenceOption.getValue() & DeathMean_Threshold > DeathMeanLowestValueHistory_Threshold){
            //   this.isChangeDetected = true;
            //}
        }
        
        this.last_splitNum=NewSplitRate;//Math.abs((double) SplitDeathSum[0] / this.WindowSize_numberInstances);//this.splitNum;
        this.last_deathNum=NewDeathRate;//Math.abs((double) SplitDeathSum[1] / this.WindowSize_numberInstances);//this.deathNum;
        
        if(SplitMeanLowestValueHistory_Threshold > SplitMean_Threshold){
            SplitMeanLowestValueHistory_Threshold = SplitMean_Threshold;
        }
        
        if(DeathMeanLowestValueHistory_Threshold > DeathMean_Threshold){
            DeathMeanLowestValueHistory_Threshold = DeathMean_Threshold;
        }
         
        //HighVariantsAttributes_Old = new int[inst.numAttributes()-1];
        //HighVariantsAttributes_Old = Arrays.copyOf(MCS.HighVariantsAttributesIndexes(), inst.numAttributes()-1);
        
        /*
        Reset All MicroClusters after each drifts,  new concpets have arrived/appeared
        */
        
        /*
        Print centers of MCs
        */
        //MCS.MicroCluster_PrintCentersMCs();
            
        if(this.isChangeDetected){
            DriftIsDetected = true;
            //MCS.printBoxPlot();
            
            MCS.ResetAllMicroClusters();
            
            if(this.FeatureSelectionOption.isSet())
                ApplyFeatureSelection = true;
            
        }
        
        /*
        Reset normalization: Max and Min for each attribute
        */
        int attribN = inst.numAttributes()-1;
        if(this.NormalizeTheAttributesOption.isSet() & this.isChangeDetected){
            //Apply both Normalization and Feature Selection
            /*
            Median filter
            */
            int[] Velocity_Selected = Velocity_OrderHighToLow(attribN,(int)NumberSelectedFeatures);
            Vote_ResetNormalizationMinMax(Velocity_Selected, inst);
            
            //ResetNormalizationMinMaxAll();
            
            //System.out.println(" ");
            //System.out.println("Time: "+(count_window+1));
        }else if(!this.NormalizeTheAttributesOption.isSet() & this.FeatureSelectionOption.isSet() & this.isChangeDetected){
            
            /*
            Median filter
            */
            int[] Velocity_Selected = Velocity_OrderHighToLow(attribN,(int)NumberSelectedFeatures);
            ResetFeatureSelectedArray(Velocity_Selected);
        }else if(ApplyFeatureSelection & !this.isChangeDetected){
            //Check relevance of features which have been selected as irrelevant to a classifier .... 
            Check_RelevanceOfSelectedFeatures(inst);
        }
        
        if(firstWindow & this.FeatureSelectionOption.isSet()){
            //Calculate InfoGain for all features at first window only
            Compute_InformationGain_AllFeatures_FirstWindow(inst);
            firstWindow = false;
            //System.out.println(" InfoGain applied");
        }
        
        //initial_normalization_firstWindow = false;
        
        //NormalizeAttributes_class.resetNormalAttributesArray(numAttributes);
        //MCS.resetCorrectPredict();
        
        /*
        write velocity result
        */
        writingResultsVelocitySum(this.WindowSize_numberInstances,inst.numAttributes()-1, inst);
        
        MCS.resetCorrectPredictTestArray();
        MCS.MicroCluster_resetVelocitySum(inst.numAttributes()-1);
        MCS.MicroCluster_ResetHighVariantsAttributesIndexes(inst.numAttributes()-1);
        MCS.MicroCluster_ResetOutliers_Num(inst.numAttributes()-1);
        MCS.MicroCluster_StringBuffer_Reset();
        InformationGain = new String[this.minNumInstancesOption.getValue()][inst.numAttributes()];
        InformationGainClassIndex = "";
        EntropyBefore = -1;
        
        //if(!ApplyFeatureSelection)
        //    ResetInformationGainNew();
        
        resetArrays(this.minNumInstancesOption.getValue(),numAttributes);
        this.EndOfWindow = true;
    }
    
    public SkipList GenerateSkipList_double(double[] FIFOnodes){
         return MCS.GenerateSkipList_double(FIFOnodes);
     }
    
    public SkipList GenerateSkipList(Object[] FIFOnodes){
         return MCS.GenerateSkipList(FIFOnodes);
     }
               
    public double PercentageDifferenceCompute(double Num1, double Num2){
        
        double Difference = Math.abs(Num1 - Num2);
        double Avg = (Num1 + Num2) / 2;
        double PercentageDiff = (Difference / Avg) * 100;
        
        return PercentageDiff;
    }
    
    public Instance OnlineNormalization_Compute(Instance instCurrent){
        
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
                    NormalizedValue = (double)((currentAttributeValue - Normalize_Min_AttribValu[attribIndex])/(Normalize_Max_AttribValu[attribIndex] - Normalize_Min_AttribValu[attribIndex])) * (this.RangeMaximumNormalizationOption.getValue() - this.RangeMinimumNormalizationOption.getValue()) + this.RangeMinimumNormalizationOption.getValue();
                    instCurrent.setValue(attribIndex,NormalizedValue);
                }else{
                    instCurrent.setValue(attribIndex, 0);
                }
            }
        }

        return instCurrent;
    }
    
    public int[] Variant_OrderHighToLow(int[] variantHistory, int NumAttributesTotal, int NumAttributesPart){
        int[] AttributesIndexes = new int[NumAttributesTotal]; // indexes of attributes
        int temp_variant = 0;
        int temp_index = 0;
        
        /*
        Indexing the array with attributes indexes to be ordered later
        */
        for (int i = 0; i < NumAttributesTotal; i++) 
        {      
            AttributesIndexes[i] = i;
        }
        
        for (int a = 0; a < NumAttributesTotal-1; ++a) {
            for (int b = a+1; b < NumAttributesTotal; ++b) {
                if (variantHistory[a] < variantHistory[b]) {
                    
                    /*
                    Check the variant only, the indexes will be swapped only without checking them
                    */
                    temp_variant = variantHistory[b];
                    variantHistory[b] = variantHistory[a];
                    variantHistory[a] = temp_variant;
                    
                    temp_index = AttributesIndexes[b];
                    AttributesIndexes[b] = AttributesIndexes[a];
                    AttributesIndexes[a] = temp_index;
                    
                }
            }
        }

        /* 
        Copy part of the array (The highest values)
        */
        int[] AttributesIndexesPart = new int[NumAttributesPart];
        System.arraycopy(AttributesIndexes, 0, AttributesIndexesPart, 0, NumAttributesPart);
        
        return AttributesIndexesPart;
    }
    
    public int[] Velocity_OrderHighToLow(int NumAttributesTotal, int NumAttributesPart){
        double[] velocity = new double[NumAttributesTotal];
        int[] AttributesIndexes = new int[NumAttributesTotal]; // indexes of attributes
        double temp_variant = 0;
        int temp_index = 0;
        
        /*
        Average velocity
        */
        for(int i=0; i<NumAttributesTotal; i++){
            double averageVelocity = Math.abs((double) MCS.getVelocitySum()[i] / MCS.getVelocityNum()[i]);
            velocity[i] = averageVelocity;
            
            /*
            Indexing the array with attributes indexes to be ordered later
            */
            AttributesIndexes[i] = i;
        }
                
        for (int a = 0; a < NumAttributesTotal-1; ++a) {
            for (int b = a+1; b < NumAttributesTotal; ++b) {
                if (velocity[a] < velocity[b]) {
                    
                    /*
                    Check the velocity only, the indexes will be swapped only without checking them
                    */
                    temp_variant = velocity[b];
                    velocity[b] = velocity[a];
                    velocity[a] = temp_variant;
                    
                    temp_index = AttributesIndexes[b];
                    AttributesIndexes[b] = AttributesIndexes[a];
                    AttributesIndexes[a] = temp_index;
                    
                }
            }
        }
        
        /* 
        Copy part of the array (The highest values)
        */
        int[] AttributesIndexesPart = new int[NumAttributesPart];
        System.arraycopy(AttributesIndexes, 0, AttributesIndexesPart, 0, NumAttributesPart);
        
        return AttributesIndexesPart;
    }
    
    public void Check_RelevanceOfSelectedFeatures(Instance instanc){
        boolean printNewLine = false;
        int ClassIndex = numAttributes;        
        //boolean ContinuousFeatures_InfoGain = ContinuousFeatures_InfoGain_Option.isSet();
        //System.out.println(ContinuousFeatures_InfoGain);
        //String str1 = "InfoGain Before: ";
        //String str2 = "InfoGain After: ";
        //for(int i = 0; i < FeatureSelected.length; i++)
        //    str = str + FeatureSelected[i] +  " ";
        
        for(int i = 0; i < FeatureSelected.length; i++){ //InformationGain_Feature.lenght
            
            //str1 = str1 + InformationGain_Feature[i] +  " ";
            
            if(FeatureSelected[i] == 1){ //InformationGain_Feature[i]>0
                int featureIndex = i;
                double NewInfoGain = 0.0;
                
                if(!instanc.attribute(featureIndex).isNumeric())
                    NewInfoGain = (double)ComputeInformationGain_SelectedFeature(this.WindowSize_numberInstances,ClassIndex,featureIndex);
                else
                    NewInfoGain = (double)ComputeInformationGain_Continuous_Attribute(this.WindowSize_numberInstances,ClassIndex,featureIndex);

                double OldInfoGain = (double)InformationGain_Feature[featureIndex];
          
                //System.out.println((i+1) + " OldInfoGain: "+ OldInfoGain + " NewInfoGain: "+NewInfoGain);
                
                double InfoGainPercentageDiff = PercentageDifferenceCompute(OldInfoGain, NewInfoGain);
                double InfoGainMean_Threshold = (OldInfoGain + NewInfoGain) / 2;
                
                InformationGain_Feature[featureIndex] = (double)NewInfoGain;
                InformationGain_Print[featureIndex] = (double)NewInfoGain;
                
                if(NewInfoGain > InfoGainMean_Threshold & InfoGainPercentageDiff > this.PercentageDifferenceOption.getValue()){ //(this.PercentageDifferenceOption.getValue() * 1)
                    
                    //System.out.println((i+1) + " OldInfoGain: "+ OldInfoGain + " NewInfoGain: "+NewInfoGain);
                    
                    FeatureSelected[featureIndex] = 0;
                    //InformationGain_Feature[featureIndex] = 0;
                    InformationGain_Feature_BecameRelevant[featureIndex] = 1;
                    System.out.print(featureIndex+1+" became relevant ");
                    printNewLine = true;
                    this.isChangeDetected = true; // To start new classifier as one feature is became relevant
                    //InformationGain_Old[indexMatched] = 0;
                }
            }  
            
            //str2 = str2 + InformationGain_Feature[i] +  " ";
        }
        
        if(printNewLine)
            System.out.println(" ");
        
        //System.out.println(str1);
        //System.out.println(str2);
    }
    
    public void Compute_InformationGain_AllFeatures_FirstWindow(Instance instanc){
        int ClassIndex = numAttributes;        

        for(int i = 0; i < numAttributes; i++){ 
        
            int featureIndex = i;
            double NewInfoGain = 0.0;
                
            if(!instanc.attribute(featureIndex).isNumeric())
                NewInfoGain = (double)ComputeInformationGain_SelectedFeature(this.WindowSize_numberInstances,ClassIndex,featureIndex);
            else
                NewInfoGain = (double)ComputeInformationGain_Continuous_Attribute(this.WindowSize_numberInstances,ClassIndex,featureIndex);

            InformationGain_Feature[featureIndex] = (double)NewInfoGain;
            InformationGain_Print[featureIndex] = (double)NewInfoGain;
        }
    }
    
    public void Vote_ResetNormalizationMinMax(int[] velocityIndexesSelected, Instance instanc){
        int ClassIndex = numAttributes;        
        
        System.out.print("Feature Selected: ");
        
        for(int i = 0; i < velocityIndexesSelected.length; i++){
            int indexMatched = velocityIndexesSelected[i];
            
            if(MCS.HighVariantsAttributesIndexes()[indexMatched] > 0){
                Normalize_Min_AttribValu[indexMatched] = 0;
                Normalize_Max_AttribValu[indexMatched] = 0;
                
                if(this.FeatureSelectionOption.isSet()){
                    double NewInfoGain = 0;
                
                    if(!instanc.attribute(indexMatched).isNumeric()) //if(!ContinuousFeatures_InfoGain_Option.isSet()){
                        NewInfoGain = ComputeInformationGain_SelectedFeature(this.WindowSize_numberInstances,ClassIndex,indexMatched);
                    else
                        NewInfoGain = ComputeInformationGain_Continuous_Attribute(this.WindowSize_numberInstances,ClassIndex,indexMatched);
                
                    //double OldInfoGain = InformationGain_Feature[indexMatched];
                    
                    //double InfoGainPercentageDiff = PercentageDifferenceCompute(OldInfoGain, NewInfoGain);
                    //double InfoGainMean_Threshold = (OldInfoGain + NewInfoGain) / 2;
                    
                    //if(NewInfoGain < InfoGainMean_Threshold & InfoGainPercentageDiff > this.PercentageDifferenceOption.getValue()){ //(this.PercentageDifferenceOption.getValue() * 1)
                    
                        InformationGain_Feature[indexMatched] = NewInfoGain;
                        InformationGain_Print[indexMatched] = NewInfoGain;
                    
                        FeatureSelected[indexMatched] = 1;
                        System.out.print(indexMatched+1+" ");
                    //}else{
                    //    FeatureSelected[indexMatched] = 0;
                    //}
                
                }else{
                    FeatureSelected[indexMatched] = 1;
                    System.out.print(indexMatched+1+" ");
                }
                
            }else
                FeatureSelected[indexMatched] = 0;
        }  
        System.out.println(" ");
        
    }
    
    public void ResetFeatureSelectedArray(int[] velocityIndexesSelected){
                
        //System.out.println(" ");
        for(int i = 0; i < velocityIndexesSelected.length; i++){
            int indexMatched = velocityIndexesSelected[i];
            if(MCS.HighVariantsAttributesIndexes()[indexMatched] > 0){
                //Check Information Gain of Feature
                int ClassIndex = numAttributes;
                
                double InfoGain = ComputeInformationGain_SelectedFeature(this.WindowSize_numberInstances,ClassIndex,indexMatched);
               
                if(InfoGain < 0.5){
                    
                    InformationGain_Print[indexMatched] = InfoGain;
                    FeatureSelected[indexMatched] = 1;
                }else{
                    FeatureSelected[indexMatched] = 0;
                }
                
            }else
                FeatureSelected[indexMatched] = 0;
        }        
    }
    
    //public void ResetInformationGainNew(){
    //    for(int i = 0; i < InformationGain_New.length; i++){
    //        InformationGain_Old[i] = InformationGain_New[i];
    //        InformationGain_New[i] = 0;       
    //    }
    //}
    
    public void ResetNormalizationMinMaxAll(){
        for(int i = 0; i < Normalize_Min_AttribValu.length; i++){
            Normalize_Min_AttribValu[i] = 0;
            Normalize_Max_AttribValu[i] = 0;
        }        
    }
    
    public void StringBuffer(Instance inst, int InstanceNumber){
        StrBuffer.append(InstanceNumber);
        StrBuffer.append(": ");
        for(int i=0;i<inst.numAttributes()-1; i++){
            StrBuffer.append(inst.value(i));
            StrBuffer.append(" ,");
        }
        StrBuffer.append("\n");
        
    }
    
    /**
     *
     * @param filePath
     * @throws IOException
     */
    public void initialFile(String filePath) throws IOException{
        File TXTfile = new File(filePath); 
                
        if(TXTfile.exists()){
            TXTfile.delete();
            TXTfile.createNewFile();
            //create a new file
            
            FileOutputStream LoadFile = new FileOutputStream(TXTfile);
            OutputStreamWriter WriteOnFilw = new OutputStreamWriter(LoadFile);    
            Writer w = new BufferedWriter(WriteOnFilw);
                
            w.write("");
            w.flush();
            w.close();
                
            //System.out.println("New file has created");
        }
    }
        
    /**
     *
     * @param StrBuffer
     * @param filePath
     */
    public void writingResults(StringBuffer StrBuffer, String filePath, Boolean Drifts) {
        try {
            File TXTfile = new File(filePath);
            
            if(TXTfile.exists()){
                
                FileWriter fw = new FileWriter(filePath,true); //the true will append the new data
                //fw.write("add a line\n");//appends the string to the file             
                //fw.append("Drifts have been detected ... " + "\n");
                //fw.append("Feature Velocity ... " + "\n");
                //fw.append("\n");
                
                //if(Drifts){
                //   fw.append("DriftDetected");
                //   fw.append("\n");
                //}else{
                //    fw.append("Stationary");
                //    fw.append("\n");
                //}
                
                fw.append(StrBuffer);
                //fw.append("\n");
                fw.flush();
                fw.close();
            
            }else{
            
                FileOutputStream LoadFile = new FileOutputStream(TXTfile);
                OutputStreamWriter WriteOnFilw = new OutputStreamWriter(LoadFile);    
                Writer w = new BufferedWriter(WriteOnFilw);
                //w.append("Drifts have been detected ... " + "\n");
                //w.append("Feature Velocity ... " + "\n");
                //w.append("\n");
                
                //if(Drifts){
                //   w.append("DriftDetected");
                //   w.append("\n");
                //}else{
                //    w.append("Stationary");
                //    w.append("\n");
                //}
                
                w.append(StrBuffer);
                //w.append("\n");
                w.flush();
                w.close();
            
            }
            
        } catch (IOException e) {
            System.err.println("Problem writing to the file");
        }
    }

    public void writingResultsVelocitySum(int windowSize, int numattrib, Instance instanc) {
        try {
            File TXTfile = new File(filePath);
            
            if(TXTfile.exists()){
                
                FileWriter fw = new FileWriter(filePath,true); //the true will append the new data
                
                String str ="";
                
                if(initial_TitleReport){
                    /*
                    Velocity
                    Attributes
                    */
                    
                    int j=0;
                    String columnName="";
                    
                    for(int i=0; i<numattrib; i++){
                       j = i + 1;
                       columnName = "Feature" + j;
                       str +=columnName;
                       str +=" ";
                    }
                    
                    /*
                    Split and Death
                    */
                    
                    str +="Split";
                    str +=" ";
                    str +="Death";
                    str +=" ";
                    
                    /*
                    Reset MC
                    */
                    str +="ResetMC(s)afterDrifts";
                    str +=" ";
                    
                    /*
                    History of High Variant
                    Attributes
                    */
                    
                    j=0;
                    columnName="";
                    
                    for(int i=0; i<numattrib; i++){
                       j = i + 1;
                       columnName = "Feature" + j;
                       str +=columnName;
                       str +=" ";
                    }
                    
                    str +="OverallAccuracy";
                    str +=" ";
                    
                    //important outliers
                    //for(int i=0; i<numattrib; i++){
                    //   j = i + 1;
                    //   columnName = "Attribute" + j;
                    //   str +=columnName;
                    //   str +=" ";
                    //}
                    
                    //extreme outliers
                    //for(int i=0; i<numattrib; i++){
                    //   j = i + 1;
                    //   columnName = "Attribute" + j;
                    //   str +=columnName;
                    //   str +=" ";
                    //}
                    
                    //Information Gain 
                    if(FeatureSelectionOption.isSet()){
                        for(int i=0; i<numattrib; i++){
                            j = i + 1;
                            columnName = "Feature" + j;
                            str +=columnName;
                            str +=" ";
                        }
                    }
                    
                    
                    fw.append(str);
                    fw.append("\n");
                    initial_TitleReport = false;
                }
                
                str ="";
                
                // Velocity
                
                for(int i=0; i<numattrib; i++){
                    double averageVelocity = Math.abs((double) MCS.getVelocitySum()[i] / MCS.getVelocityNum()[i]);
                    str += averageVelocity;
                    str += " ";
                }
                
                // Split and Death rates
                double SplitRate_average = Math.abs((double) SplitDeathSum[0] / windowSize);
                double DeathRate_average = Math.abs((double) SplitDeathSum[1] / windowSize);
                
                str += SplitRate_average;
                str += " ";
                str += DeathRate_average;
                str += " ";
                
                //Resst all MC
                if(this.isChangeDetected){
                    str += 1;
                }else{
                    str += 0;
                }
                
                str += " ";
                
                //History of High Variants
                for(int i=0; i<numattrib; i++){
                    str += MCS.HighVariantsAttributesIndexes()[i];//Math.abs(MCS.HighVariantsAttributesIndexes()[i] - HighVariantsAttributes_Old[i]);
                    str += " ";
                }
                
                //double TentativeAccuracyTest = Math.abs((double) MCS.getCorrectPredictTest() / windowSize) * 100;
                
                int[][] ActualPredictedClassification = MCS.getCorrectPredictTestArray();
                
                int Tp = 0; // True Possitive
                int TpTnFpFn = 0; // True Possitive + True Negative + False positive + False Negative
                
                for(int i=0; i<NumClasses; i++){
                    for(int j=0; j<NumClasses; j++){
                        
                        if(i == j)
                            Tp = Tp+ ActualPredictedClassification[i][j];
                        TpTnFpFn = TpTnFpFn + ActualPredictedClassification[i][j];
                    }
                }
                
                double OverallAccuracy = ((double)Tp/(double)TpTnFpFn) * 100;
                //System.out.println("TpTn: " + (double)TpTn + " TpTnFpFn: "+ (double)TpTnFpFn + " Accuracy = " + (double)TpTn/(double)TpTnFpFn);
                
                str += OverallAccuracy;
                str += " ";
                               
                // Normalization
                //for(int i = 0; i < NormalizeAttributes_class.getNormalAttributes().length; i++){
                //    str += (double)NormalizeAttributes_class.getNormalAttributes()[i]/windowSize;
                //    str += " ";
                //}
                
                //important outliers
                //for(int i=0; i<numattrib; i++){
                //    str += MCS.MicroCluster_get_Important_Outliers_Num()[i];
                //    str += " ";
                //}
                
                //extreme outliers
                //for(int i=0; i<numattrib; i++){
                //    str += MCS.MicroCluster_get_Extreme_Outliers_Num()[i];
                //    str += " ";
                //}
                
                //Information Gain 
                
                if(FeatureSelectionOption.isSet()){
                    int ClassIndex = numAttributes; 
                    for(int i=0; i<numattrib; i++){
                        double InfoGain_BecameRelevant = 0;
                        //String InfoGain = ComputeInformationGain( windowSize, numattrib+1);//Math.abs((double) MCS.getRangeSum()[i] / MCS.getRangeNum()[i]);
                        if(InformationGain_Feature_BecameRelevant[i] == 1 & FeatureSelected[i] == 0){
                            if(!instanc.attribute(i).isNumeric()) //ContinuousFeatures_InfoGain_Option.isSet()
                                InfoGain_BecameRelevant = (double)ComputeInformationGain_SelectedFeature(this.WindowSize_numberInstances,ClassIndex,i);
                            else
                                InfoGain_BecameRelevant = (double)ComputeInformationGain_Continuous_Attribute(this.WindowSize_numberInstances,ClassIndex,i);
                            
                            str += InfoGain_BecameRelevant;//InfoGain;
                        }else
                            str += InformationGain_Print[i];//InfoGain;
                        str += " ";
                    }
                }
                
                fw.append(str);
                fw.append("\n");
                fw.flush();
                fw.close();
            
            }else{
            
                FileOutputStream LoadFile = new FileOutputStream(TXTfile);
                OutputStreamWriter WriteOnFilw = new OutputStreamWriter(LoadFile);    
                Writer w = new BufferedWriter(WriteOnFilw);
                
                String str ="";
                
                if(initial_TitleReport){
                    /*
                    Velocity
                    Attributes
                    */
                    
                    int j=0;
                    String columnName="";
                    
                    for(int i=0; i<numattrib; i++){
                       j = i + 1;
                       columnName = "Feature" + j;
                       str +=columnName;
                       str +=" ";
                    }
                    
                    /*
                    Split and Death
                    */
                    
                    str +="Split";
                    str +=" ";
                    str +="Death";
                    str +=" ";
                    
                    /*
                    Reset MC
                    */
                    str +="ResetMC(s)afterDrifts";
                    str +=" ";
                    
                    /*
                    History of High Variant
                    Attributes
                    */
                    
                    j=0;
                    columnName="";
                    
                    for(int i=0; i<numattrib; i++){
                       j = i + 1;
                       columnName = "Feature" + j;
                       str +=columnName;
                       str +=" ";
                    }
                    
                    str +="OverallAccuracy";
                    str +=" ";
                    
                    //important outliers
                    //for(int i=0; i<numattrib; i++){
                    //   j = i + 1;
                    //   columnName = "Attribute" + j;
                    //   str +=columnName;
                    //   str +=" ";
                    //}
                    
                    //extreme outliers
                    //for(int i=0; i<numattrib; i++){
                    //   j = i + 1;
                    //   columnName = "Attribute" + j;
                    //   str +=columnName;
                    //   str +=" ";
                    //}
                    
                    //Information Gain
                    if(FeatureSelectionOption.isSet()){
                        for(int i=0; i<numattrib; i++){
                            j = i + 1;
                            columnName = "Feature" + j;
                            str +=columnName;
                            str +=" ";
                        }
                    }
                    
                    w.append(str);
                    w.append("\n");
                    initial_TitleReport = false;
                }
                
                // Velocity
                
                for(int i=0; i<numattrib; i++){
                    double averageVelocity = (double) MCS.getVelocitySum()[i] / MCS.getVelocityNum()[i];
                    str += averageVelocity;
                    str += " ";
                }
                
                // Split and Death rates
                
                double SplitRate_average = (double) SplitDeathSum[0] / windowSize;
                double DeathRate_average = (double) SplitDeathSum[1] / windowSize;
                
                str += SplitRate_average;
                str += " ";
                str += DeathRate_average;
                str += " ";
                
                //Resst all MC
                if(this.isChangeDetected){
                    str += 1;
                }else{
                    str += 0;
                }
                
                str += " ";
                
                //History of High Variants
                for(int i=0; i<numattrib; i++){
                    str += MCS.HighVariantsAttributesIndexes()[i];//Math.abs(MCS.HighVariantsAttributesIndexes()[i] - HighVariantsAttributes_Old[i]);
                    str += " ";
                }
                
                //double TentativeAccuracyTest = Math.abs((double) MCS.getCorrectPredictTest() / windowSize) * 100;
               
                int[][] ActualPredictedClassification = MCS.getCorrectPredictTestArray();
                
                int Tp = 0; // True Possitive 
                int TpTnFpFn = 0; // True Possitive + True Negative + False positive + False Negative
                
                for(int i=0; i<NumClasses; i++){
                    for(int j=0; j<NumClasses; j++){
                        
                        if(i == j)
                            Tp = Tp + ActualPredictedClassification[i][j];
                        TpTnFpFn = TpTnFpFn + ActualPredictedClassification[i][j];
                    }
                }
                
                //System.out.println("TpTn: " + (double)TpTn + " TpTnFpFn: "+ (double)TpTnFpFn + " Accuracy = " + (double)TpTn/(double)TpTnFpFn);
                double OverallAccuracy = ((double)Tp/(double)TpTnFpFn) * 100;
                
                str += OverallAccuracy;
                str += " ";
                
                // Normalization
                //for(int i = 0; i < NormalizeAttributes_class.getNormalAttributes().length; i++){
                //    str += (double)NormalizeAttributes_class.getNormalAttributes()[i]/windowSize;
                //    str += " ";
                //}
                
                //important outliers
                //for(int i=0; i<numattrib; i++){
                //    str += MCS.MicroCluster_get_Important_Outliers_Num()[i];
                //    str += " ";
                //}
                
                //extreme outliers
                //for(int i=0; i<numattrib; i++){
                //    str += MCS.MicroCluster_get_Extreme_Outliers_Num()[i];
                //    str += " ";
                //}
                
                //Information Gain
                if(FeatureSelectionOption.isSet()){
                    int ClassIndex = numAttributes; 
                    for(int i=0; i<numattrib; i++){
                        double InfoGain_BecameRelevant = 0;
                        //String InfoGain = ComputeInformationGain( windowSize, numattrib+1);//Math.abs((double) MCS.getRangeSum()[i] / MCS.getRangeNum()[i]);
                        if(InformationGain_Feature_BecameRelevant[i] == 1 & FeatureSelected[i] == 0){
                            if(!instanc.attribute(i).isNumeric()) //ContinuousFeatures_InfoGain_Option.isSet()
                                InfoGain_BecameRelevant = (double)ComputeInformationGain_SelectedFeature(this.WindowSize_numberInstances,ClassIndex,i);
                            else
                                InfoGain_BecameRelevant = (double)ComputeInformationGain_Continuous_Attribute(this.WindowSize_numberInstances,ClassIndex,i);
                            
                            str += InfoGain_BecameRelevant;//InfoGain;
                        }else
                            str += InformationGain_Print[i];//InfoGain;
                        str += " ";
                    }
                }
                
                w.append(str);
                w.append("\n");
                w.flush();
                w.close();
            
            }
            
        } catch (IOException e) {
            System.err.println("Problem writing to the file");
        }
    }
    
    public void writingResultsVelocitySumBeforeSplit(int numattrib) {
        
        try {
            File TXTfile = new File(filePath);
            
            if(TXTfile.exists()){
                
                FileWriter fw = new FileWriter(filePath,true); //the true will append the new data
                
                String str ="";
                for(int i=0; i<numattrib; i++){
                    double averageVelocity = (double) MCS.getVelocitySum()[i] / MCS.getVelocityNum()[i];
                    str += averageVelocity;
                    str += " ";
                }
                
                fw.append(str);
                fw.append("\n");
                fw.flush();
                fw.close();
            
            }else{
            
                FileOutputStream LoadFile = new FileOutputStream(TXTfile);
                OutputStreamWriter WriteOnFilw = new OutputStreamWriter(LoadFile);    
                Writer w = new BufferedWriter(WriteOnFilw);
                
                String str ="";
                for(int i=0; i<numattrib; i++){
                    double averageVelocity = (double) MCS.getVelocitySum()[i] / MCS.getVelocityNum()[i];
                    str += averageVelocity;
                    str += " ";
                }
                
                w.append(str);
                w.append("\n");
                w.flush();
                w.close();
            
            }
            
        } catch (IOException e) {
            System.err.println("Problem writing to the file");
        }
    }
    
    public void InsertInformationGainArray(Instance instanc, int row){
        //String str = "";
        
        for (int column = 0; column < instanc.numAttributes(); column++){
            if(instanc.attribute(column).isNumeric()){
                InformationGain[row][column]=Double.toString(instanc.value(column));
                //str = str + instanc.toString(column) + " "; 
            }else
                InformationGain[row][column]=instanc.toString(column);
        }
        
        //System.out.println(str);
        /*
          InformationGainClassIndex
          To be used for calculate entropy before (class indexes)
        */
        InformationGainClassIndex += instanc.stringValue(instanc.numAttributes()-1);//instanc.classIndex();
        
    }
    
    public double ComputeInformationGain_SelectedFeature(int row, int ClassIndex, int SelectedFeature){
        /*
        It is used to compute IG after each training instance
        */
        //InformationGainClassIndex = "";
        
        if(EntropyBefore < 0)
            EntropyBefore = EntropyValue(InformationGainClassIndex);
       
        String ClassIndexLabel = "";
        String FeatureValue = "";
        
        Map<String, String> MapFeatures = new HashMap<>();
        double EntropyAfter = 0;
        
        int numberItems = row - MaxNodesSkipList;
                
        for(int i=0; i<row; i++){  //  int i=row-1; i>=numberItems;i--
            FeatureValue =  (String)InformationGain[i][SelectedFeature];
            ClassIndexLabel = (String)InformationGain[i][ClassIndex];
            //InformationGainClassIndex = InformationGainClassIndex + ClassIndexLabel;
            
            if (MapFeatures.containsKey(FeatureValue)) {
                MapFeatures.put(FeatureValue, MapFeatures.get(FeatureValue) + ClassIndexLabel);
            } else {
                MapFeatures.put(FeatureValue, String.valueOf(ClassIndexLabel));
            }
        }
        
        for (Map.Entry<String, String> entry : MapFeatures.entrySet()) {
            int length = entry.getValue().length();
            String s = entry.getValue();
            double entropy = EntropyValue(s);
            double div = (double)length/row;
        
            EntropyAfter = EntropyAfter + (double)(entropy * div);
        }
            
        double InformationGainValue = EntropyBefore - EntropyAfter ;
         
        return (double)InformationGainValue;
        
    }
    
    public double ComputeInformationGain_Continuous_Attribute(int row, int ClassIndex, int SelectedFeature){
        /*
        It is used to compute IG after each training instance
        */
        
        double MaxInfoGain = 0;
        double InfoGain = 0;
        String InfoGainClassIndex = "";
        
        SkipList[] SL = new SkipList[1];
        SL[0] = new SkipList(MaxNodesSkipList, MaxHeadsOption.getValue());
    
        int NumRows = row - MaxNodesSkipList;
        
        for(int i = row - 1; i >= NumRows; i--){// Choose the most recent one //int i = 0; i < row; i++
            if(i >= 0){
                double val = (double)Double.parseDouble(InformationGain[i][SelectedFeature]);
                SL[0].addNode((double)val,i);
                InfoGainClassIndex = InfoGainClassIndex + InformationGain[i][ClassIndex];
            }
        }
    
        double EntropyBefore = EntropyValue(InfoGainClassIndex);
    
        //System.out.println("EntropyBefore: "+ EntropyBefore);
        double[] Nodes = SL[0].RetrieveNodes();
    
        double SplitPosition = 0;
    
        for(int j = 0 ; j < Nodes.length; j++){
        
            CountGreaterSmaller = new int[NumClasses][2];
            for(int iColumn = 0; iColumn<2; iColumn++){
                for(int iRow = 0; iRow<NumClasses; iRow++)
                    CountGreaterSmaller[iRow][iColumn] = 0;
            }
            
            if(j == 0){
                SplitPosition = (double)Nodes[j] / 2;
            }else if(j == Nodes.length-1){
                SplitPosition = (double)(Nodes[j] + Nodes[j] + 1) / 2;
            }else{
                SplitPosition = (double)(Nodes[j-1] + Nodes[j]) / 2;
            }
        
            int length_smaller = 0;
            int length_greater = 0;
            double e_smaller = 0.0;
            double e_greater = 0.0;
            double entropy_smaller = 0.0;
            double entropy_greater = 0.0;
            double EntropyAfter = 0;
        
            for(int i=0; i<row; i++){
                double FeatureValue =  (double)Double.parseDouble(InformationGain[i][SelectedFeature]); // 0 because I have 1 column
                String ClassIndexLabel = (String)InformationGain[i][ClassIndex]; // 1 because the class label is the second column
            
                int WinningClassIndex = check_Class(ClassIndexLabel);
                
                if(FeatureValue <= SplitPosition){
                    CountGreaterSmaller[WinningClassIndex][0] ++;
                    length_smaller ++;
                }else if(FeatureValue > SplitPosition){
                    CountGreaterSmaller[WinningClassIndex][1] ++;
                    length_greater ++;
                }     
            }
            
            //System.out.println(CountGreaterSmaller[0][0] + " " + CountGreaterSmaller[0][1]);
            //System.out.println(CountGreaterSmaller[1][0] + " " + CountGreaterSmaller[1][1]);
        
            double p_smaller = 0;
            double p_greater = 0;
        
            for(int SmallGreatIndex=0; SmallGreatIndex<2; SmallGreatIndex++){
                for(int classindex=0; classindex<NumClasses; classindex++){
                    if(CountGreaterSmaller[classindex][SmallGreatIndex] != 0){
                        if(SmallGreatIndex == 0){
                            p_smaller = (double) CountGreaterSmaller[classindex][SmallGreatIndex] / length_smaller;
                            e_smaller += p_smaller * log2(p_smaller);
                        }else{
                            p_greater = (double) CountGreaterSmaller[classindex][SmallGreatIndex] / length_greater;
                            e_greater += p_greater * log2(p_greater);
                        }    
                    }
                }
            }
        
            entropy_smaller= -e_smaller;
            entropy_greater= -e_greater;
        
            double div_smaller = (double)length_smaller/row;
            double div_greater = (double)length_greater/row;
        
            EntropyAfter = (double)(entropy_smaller * div_smaller) + (entropy_greater * div_greater); //EntropyAfter + 
        
            InfoGain = Math.abs((double)EntropyBefore - EntropyAfter);
            //System.out.println("EntropyAfter :"+ EntropyAfter + " Info Gain :" + InfoGain);
       
            if(InfoGain > MaxInfoGain)
                MaxInfoGain = InfoGain;
        }
        
        //if(InfoGain < 0)
        //    System.out.println(" InfoGain < 0 "+ InfoGain);
        
        return (double)MaxInfoGain;
    }
    
    public int check_Class(String ClassLabel){
        int classIndex = -1;
        
        for(int i=0; i<Classes.length; i++){
            if(ClassLabel.equals(Classes[i])){ //Classes[i] == ClassLabel
                classIndex = i;
                return classIndex;
            }
        }
        
        return (int)classIndex;
    }
    
    //public String ComputeInformationGain(int row, int columns){
    //    /*
    //    It is used to compute IG after each training instance
    //    */
    //    
    //    EntropyBefore = EntropyValue(InformationGainClassIndex);
    //    
    //    //int row = InformationGain.length;
    //    //int column = InformationGain[0].length;
    //    String ClassIndexLabel = "";
    //    String FeatureValue = "";
    //    String InfoGain = "";
    //    //System.out.println(" ");
    //    for(int j = 0; j<columns-1; j++){
    //        
    //        Map<String, String> MapFeatures = new HashMap<>();
    //        double EntropyAfter = 0;
    //        
    //        for(int i=0; i<row; i++){
    //            FeatureValue =  (String)InformationGain[i][j];
    //            ClassIndexLabel = (String)InformationGain[i][columns-1];
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
    //            double div = (double)length/row;
    //            //double e = entropy * (length/row);
    //            //System.out.print(" entropy: "+ entropy + " lenght: "+ length + " row: "+ row);
    //            EntropyAfter = EntropyAfter + (double)(entropy * div);
    //        }
    //        
    //        double InformationGainValue = EntropyBefore - EntropyAfter ;
    //        
    //        InfoGain = InfoGain + InformationGainValue + " ";
    //        
    //    }
    //    
    //    return InfoGain;
    //    //System.out.println("Information Gain: " + InfoGain);
    //    //System.out.println(InformationGainClassIndex + "    EntropyBefore: "+EntropyBefore);
   // }
    
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
    
    public double log2(double a) {
        return Math.log(a) / Math.log(2);
    }
    
    @Override
    public void input(double inputValue) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getClassName() {
        return null;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean CreateNewInstance() {
        if(this.CreateNewInstanceOption.isSet())
            return true;
        else
            return false;
    }

    @Override
    public Instance InstanceToClassifier() {
        return InstanceToClassifierData;
    }

    @Override
    public int WindowSize() {
        return this.minNumInstancesOption.getValue();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean ResetWindowSize() {
        return this.EndOfWindow;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
