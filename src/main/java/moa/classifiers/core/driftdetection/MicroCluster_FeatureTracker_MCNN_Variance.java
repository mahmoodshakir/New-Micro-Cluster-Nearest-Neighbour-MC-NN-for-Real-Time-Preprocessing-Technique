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
//import MicroClustersVisualization.FeatureData;
import moa.options.FlagOption;
import moa.options.FloatOption;
/**
 *
 * @author Mahmood 2015 - 2016
 */
public class MicroCluster_FeatureTracker_MCNN_Variance extends AbstractChangeDetector {
    private static final long serialVersionUID = 1L;

    public IntOption minNumInstancesOption = new IntOption(
            "WindowSize",
            'n',
            "The minimum number of instances before permitting detecting change.",
            10000, 2, Integer.MAX_VALUE);
    
    public IntOption PercentageDifferenceOption = new IntOption(
	        "PercentageDifference",'p', "The Percentage Difference between 2 values (Split and Death rates).",
	         50, 0, 100);
    
    //public FloatOption LowPassFilterLPFOption = new FloatOption(
//		        "LowPassFilter",'k', "Low Pass Filter.",
  //              0.5, 0 , 1);
    
    public IntOption ErrorOption = new IntOption(
	        "ErrorOption",'a', "Error option.",
	         50, 0, Integer.MAX_VALUE);
    
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
    
    public static StandardDeviation standard_deviation_equation = new StandardDeviation();
    //public static NormalDistribution NormalDistribution = new NormalDistribution();
    public static StringBuffer SB  =  new StringBuffer();
    public MicroClustersSingle MCS = new MicroClustersSingle();
    public MicroClusterManager MCM = new MicroClusterManager();
    public static PercentageDifference PercentageDifference_class = new PercentageDifference();
    //public static NormalizeAttributes NormalizeAttributes_class = new NormalizeAttributes();
    private String filePath = "c:/FeatureTracker_Statistics.xls";
    private int WindowSize_numberInstances;
    private double splitNum;
    private double deathNum;
    private int count_window=0;
    private double last_splitNum=0;
    private double last_deathNum=0;
    private double SplitMeanLowestValueHistory_Threshold=0;
    private double DeathMeanLowestValueHistory_Threshold=0;
    private int numAttributes=0;
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
    private int NumClasses = 0;
    public boolean EndOfWindow = false;
    public boolean DriftIsDetected = false;
    
    public MicroCluster_FeatureTracker_MCNN_Variance() throws IOException {
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
        return MCS.SplitIsHappened();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] FeatureSelectedIndexes() {
        return MCS.HighVariantsAttributesIndexes();
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
            
            SplitDeathSum = new double[2];
                    
            MCS.MicroCluster_resetVelocitySum(inst.numAttributes()-1);
            
            HighVariantsAttributes_Old = new int[inst.numAttributes()-1];
            
            for(int i =0;i<inst.numAttributes()-1;i++)
                HighVariantsAttributes_Old[i] = 0;
            
            MCS.MicroCluster_ResetHighVariantsAttributesIndexes(inst.numAttributes()-1);
            numAttributes = inst.numAttributes()-1;
            
            //NormalizeAttributes_class.resetArrays(numAttributes);
            
            resetArrays(this.minNumInstancesOption.getValue(),numAttributes);
            
            Normalize_Max_AttribValu = new double[inst.numAttributes()];
            Normalize_Min_AttribValu = new double[inst.numAttributes()];
            
            for(int AttribIndex = 0 ; AttribIndex < inst.numAttributes() - 1 ; AttribIndex++){
                Normalize_Max_AttribValu[AttribIndex] = 0;
                Normalize_Min_AttribValu[AttribIndex] = 0;
            }
            
            /*
            Set number of attributes to be selected for reset Max Min Online Normalization 
            Median filter
            */
            NumberSelectedFeatures = Math.round(numAttributes * (NumAttributesOption.getValue() / (float)100));
            
            /*
            Calculate AlphaValue
            */
            AlphaValue = AlphaOption.getValue() / (float)100;
           
            if(LowPassFilterOption.isSet() & SplitOnAllAttributesFlagOption.isSet()){
                MCS.ResetMMOptions(true, AlphaValue, ErrorOption.getValue() ,MAXClusterArraySizeOption.getValue() , RemovePoorlyPerformingClustersOption.getValue() , SplitMultiAttributesOption.getValue() , true );
            }else if(LowPassFilterOption.isSet() || SplitOnAllAttributesFlagOption.isSet()){
                if(LowPassFilterOption.isSet()){
                    MCS.ResetMMOptions(true, AlphaValue, ErrorOption.getValue() ,MAXClusterArraySizeOption.getValue() , RemovePoorlyPerformingClustersOption.getValue() , SplitMultiAttributesOption.getValue() , false );
                }
                
                if(SplitOnAllAttributesFlagOption.isSet()){
                    MCS.ResetMMOptions(false, AlphaValue, ErrorOption.getValue() ,MAXClusterArraySizeOption.getValue() , RemovePoorlyPerformingClustersOption.getValue() , SplitMultiAttributesOption.getValue() , true );
                }
            } else{
                MCS.ResetMMOptions(false, AlphaValue, ErrorOption.getValue() ,MAXClusterArraySizeOption.getValue() , RemovePoorlyPerformingClustersOption.getValue() , SplitMultiAttributesOption.getValue() , false );
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
            
            splitdeathAlarmPointers = MCS.SplitDeathAlarm(OnlineNormalization_Compute(inst));
            
        }else{
            
            splitdeathAlarmPointers = MCS.SplitDeathAlarm(inst);
            
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
        
        /**
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
        double SplitPercentageDiff = PercentageDifference_class.compute(this.last_splitNum, NewSplitRate);
        double SplitMean_Threshold = (this.last_splitNum + NewSplitRate) / 2;
        
        /*
        Death mean and Dynamic threshold for split
        */
        double NewDeathRate = Math.abs((double) SplitDeathSum[1] / this.WindowSize_numberInstances);
        double DeathPercentageDiff = PercentageDifference_class.compute(this.last_deathNum, NewDeathRate);
        double DeathMean_Threshold = (this.last_deathNum + NewDeathRate) / 2;

        //System.out.println(NewSplitRate + " " + NewDeathRate + " " + SplitPercentageDiff + " " + DeathPercentageDiff + " "+ SplitMean_Threshold + " " + DeathMean_Threshold);
        
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
        
        /*
        write velocity result
        */
        writingResultsVelocitySum(this.WindowSize_numberInstances,inst.numAttributes()-1);
        
        //HighVariantsAttributes_Old = new int[inst.numAttributes()-1];
        //HighVariantsAttributes_Old = Arrays.copyOf(MCS.HighVariantsAttributesIndexes(), inst.numAttributes()-1);
        
        /*
        Reset All MicroClusters after each drifts,  new concpets have arrived/appeared
        */
        if(this.isChangeDetected){
            DriftIsDetected = true;
            /*
            Print centers of MCs
            */
            //MCS.MicroCluster_PrintCentersMCs();
            MCS.ResetAllMicroClusters();
        }
        
        /*
        Reset normalization: Max and Min for each attribute
        */
        if(this.NormalizeTheAttributesOption.isSet() & this.isChangeDetected){
            
            int attribN = inst.numAttributes()-1;
            /*
            Median filter
            */
            int[] Velocity_Selected = Velocity_OrderHighToLow(attribN,(int)NumberSelectedFeatures);
            Vote_ResetNormalizationMinMax(Velocity_Selected);
            
            //ResetNormalizationMinMaxAll();
            
            System.out.println(" ");
            System.out.println("Time: "+(count_window+1));
        }
        
        //initial_normalization_firstWindow = false;
        
        //NormalizeAttributes_class.resetNormalAttributesArray(numAttributes);
        MCS.resetCorrectPredictTestArray();
        MCS.MicroCluster_resetVelocitySum(inst.numAttributes()-1);
        MCS.MicroCluster_ResetHighVariantsAttributesIndexes(inst.numAttributes()-1);
        MCS.MicroCluster_StringBuffer_Reset();
        resetArrays(this.minNumInstancesOption.getValue(),numAttributes);
        this.EndOfWindow = true;
    }
    
    public Instance OnlineNormalization_Compute(Instance inst){
        
        double currentAttributeValue = 0;
        double NormalizedValue = 0;
        
        if(initial_normalization){
            
            for(int attribIndex = 0; attribIndex < inst.numAttributes()-1; attribIndex++){
                currentAttributeValue = (double)inst.value(attribIndex);
            
                Normalize_Min_AttribValu[attribIndex] = currentAttributeValue;
                Normalize_Max_AttribValu[attribIndex] = currentAttributeValue;
                
                //if(attribIndex ==0){
                //    System.out.println(currentAttributeValue + " " + currentAttributeValue + " " + currentAttributeValue + " " + currentAttributeValue);
                //}
                /*
                Normalization equals 0 because Max - Min = 0 
                */
                inst.setValue(attribIndex, 0);
            }
            
            this.initial_normalization = false;
            
        }else{
            for(int attribIndex = 0; attribIndex < inst.numAttributes()-1; attribIndex++){
                currentAttributeValue = (double)inst.value(attribIndex);
            
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
                    inst.setValue(attribIndex,NormalizedValue);
                }else{
                    inst.setValue(attribIndex, 0);
                }
                
                //if(attribIndex ==0){
                //    System.out.println(currentAttributeValue + " " + Normalize_Min_AttribValu[attribIndex] + " " + Normalize_Max_AttribValu[attribIndex] + " " +  NormalizedValue);
                //}
            }
        }

        return inst;
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
    
    public void Vote_ResetNormalizationMinMax(int[] velocityIndexesSelected){
                
        System.out.println(" ");
        for(int i = 0; i < velocityIndexesSelected.length; i++){
            int indexMatched = velocityIndexesSelected[i];
            if(MCS.HighVariantsAttributesIndexes()[indexMatched] > 0){
                Normalize_Min_AttribValu[indexMatched] = 0;
                Normalize_Max_AttribValu[indexMatched] = 0;
                System.out.println(indexMatched+1);
            }
        }        
    }
    
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
        
    public double[] getVelocitySum_visualization(){
         return MCS.getVelocitySum();
     }
     
     public int[] getVelocityNum_visualization(){
         return MCS.getVelocityNum();
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

    public void writingResultsVelocitySum(int windowSize, int numattrib) {
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
                
                int TpTn = 0; // True Possitive + True Negative
                int TpTnFpFn = 0; // True Possitive + True Negative + False positive + False Negative
                
                for(int i=0; i<NumClasses; i++){
                    for(int j=0; j<NumClasses; j++){
                        
                        if(i == j)
                            TpTn = TpTn + ActualPredictedClassification[i][j];
                        TpTnFpFn = TpTnFpFn + ActualPredictedClassification[i][j];
                    }
                }
                
                double OverallAccuracy = ((double)TpTn/(double)TpTnFpFn) * 100;
                
                str += OverallAccuracy;
                str += " ";
                
                // Normalization
                //for(int i = 0; i < NormalizeAttributes_class.getNormalAttributes().length; i++){
                //    str += (double)NormalizeAttributes_class.getNormalAttributes()[i]/windowSize;
                //    str += " ";
                //}
                
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
                
                int TpTn = 0; // True Possitive + True Negative
                int TpTnFpFn = 0; // True Possitive + True Negative + False positive + False Negative
                
                for(int i=0; i<NumClasses; i++){
                    for(int j=0; j<NumClasses; j++){
                        
                        if(i == j)
                            TpTn = TpTn + ActualPredictedClassification[i][j];
                        TpTnFpFn = TpTnFpFn + ActualPredictedClassification[i][j];
                    }
                }
                
                double OverallAccuracy = ((double)TpTn/(double)TpTnFpFn) * 100;
                
                str += OverallAccuracy;
                str += " ";
                
                // Normalization
                //for(int i = 0; i < NormalizeAttributes_class.getNormalAttributes().length; i++){
                //    str += (double)NormalizeAttributes_class.getNormalAttributes()[i]/windowSize;
                //    str += " ";
                //}
                
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
    public Instance InstanceToClassifier() {
        return null;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean CreateNewInstance() {
        return false;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
