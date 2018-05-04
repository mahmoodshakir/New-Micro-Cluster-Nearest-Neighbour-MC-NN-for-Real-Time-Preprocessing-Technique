/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers;

import java.lang.Math;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.UUID;
import moa.classifiers.MicroClusterSkipList.FIFO_LinkedList;
import moa.classifiers.MicroClusterSkipList.SkipList;
import weka.core.Instance;
/**
 *
 * @author Mahmood Shakir
 * University of Reading
 * 2016
 */
public class MicroClusterMedian {
    public FIFO_LinkedList[] FIFO;
    //public SkipList[] SkipList;
    public int MaxNodes_SkipList; 
    public int MaxHeads_SkipList;
    private BigDecimal BigCF1TInitialTriangle;
    public double[] CF1X  = {-1}; 
    public double[] CF2X  = {-1}; 
    public double CF1T  = -1;    
    public double CF1TInitial  = -1; 
    public double CF1TInitialTriangle = -1; 
    public double CF2T  = -1; 
    public int N = 0;  
    public double[] Centers = {-1};
    //double[] CentersBias = {-1};
    public double ClassIndex = -1; 
    public double BoundaryDistanceRadius = -1; 
    public double STDDeviation = -1;
    public int NumAttributes;
    public int LastTTL = -1; 
    public int TTL= 10; 
    public int TTLIncrementVal = -1; 
    public double ClusterTimeMeanVal = -1; 
    public double[] VariantsValuesArray;
    public int MicroClusterID = 0;
    public double[] CF1X_Old  = {-1}; 
    public double[] CF2X_Old  = {-1}; 
    public double CF1T_Old  = -1;
    public double CF2T_Old  = -1;
    public int N_Old = 0;
    double[] Centers_Old = {-1};
    public int NumberParticipationsFalsePositive=0;
    public int NumberParticipationsTruePositive =0;
    public double[] MaximumValue  = {-1}; 
    public double[] MinimumValue  = {-1}; 
    public boolean Intitialized = false;
    /*
    Creating a random UUID (Universally unique identifier).
    */
    
    public UUID uuid;
    // time STAMP VALUES  
    //public double StreamSquareTotal = 0; 
    public int LastUpdateStreamCount = 0; 
    //public int StreamTotal = 0;
    
    /*
    Low Pass Filter Old-Previous 
    */
    private double[] LowPassFilter_Old;
    private boolean LowPassFilterOption;
    private double AlphaLowPassFilter;
    private double maxVariantAttributeVal = -1; 
    private static final long serialVersionUID = 1L;
    
    public void IncrementTestingAges()
    {
        //StreamItemCount++;
        //StreamTotal = StreamTotal + StreamItemCount;
        //StreamSquareTotal = StreamSquareTotal + (StreamItemCount * StreamItemCount);
    }
    
    public MicroClusterMedian(int ClassIndexD,int numAttributes, int InstnceTimeStamp, boolean LPF, double AlphaLPF, int MaxNodes, int MaxHeads)
    {
        MaxNodes_SkipList = MaxNodes; 
        MaxHeads_SkipList = MaxHeads;
         
        this.ClassIndex = ClassIndexD;        
        NumAttributes = numAttributes;
        ResetCluster(NumAttributes);
        
        this.LastUpdateStreamCount = InstnceTimeStamp;
        LowPassFilterOption = LPF;
        AlphaLowPassFilter = AlphaLPF;
        
    }
    
    
    public MicroClusterMedian(int ClassIndexD,int numAttributes)
    {
        this.ClassIndex = ClassIndexD;        
        NumAttributes = numAttributes;
        ResetCluster(NumAttributes);
        
    }
    
    /*
    Added by Mahmood 
    Return array of high variant listed from low to high
    */
    public int[] findMaxVariantArrtibute_AttributesIndexesHightoLow()
    {
        VariantsValuesArray = new double[NumAttributes];  // variants of attributes
        int[] AttributesIndexes = new int[NumAttributes]; // indexes of attributes
        double CF1VAL = 0; 
        double variantValue = 0;
        
         for (int i = 0; i < this.NumAttributes; i++) 
            {
                CF1VAL = (CF1X[i] / this.N); 
                CF1VAL = (CF1VAL * CF1VAL);
                variantValue = (double)((CF2X[i] / this.N) - (CF1VAL));
                VariantsValuesArray[i] +=  variantValue; 
                
                AttributesIndexes[i] = i;
            }
        
        double temp_variant = 0;
        int temp_index = 0;
        
        for (int a = 0; a < VariantsValuesArray.length-1; ++a) {
            for (int b = a+1; b < VariantsValuesArray.length; ++b) {
                if (VariantsValuesArray[a] < VariantsValuesArray[b]) {
                    
                    /*
                    Check the variant only, the indexes will be swapped only without checking them
                    */
                    temp_variant = VariantsValuesArray[b];
                    VariantsValuesArray[b] = VariantsValuesArray[a];
                    VariantsValuesArray[a] = temp_variant;
                    
                    temp_index = AttributesIndexes[b];
                    AttributesIndexes[b] = AttributesIndexes[a];
                    AttributesIndexes[a] = temp_index;
                    
                }
            }
        }
         
         return AttributesIndexes; 
    }
    
    public double[] findMaxVariantArrtibute_AttributesValuesHightoLow(){
        return VariantsValuesArray;
    }
    
    public int findMaxVariantArrtibute()
    {
        double[] Variants = new double[NumAttributes];
        double CF1VAL = 0; 
                
         for (int i = 0; i < this.NumAttributes; i++) 
            {
                CF1VAL = (CF1X[i] / this.N); 
                CF1VAL = (CF1VAL * CF1VAL);
                Variants[i] += ((CF2X[i] / this.N) - (CF1VAL)) ; 
            }
        
        
         int MaxIndex = 0; 
         double MaxVal = 0; 
         
         for (int i = 0; i < Variants.length; i++) 
         {
            if (Variants[i] > MaxVal)
            {
                MaxVal = Variants[i];
                MaxIndex = i;
                maxVariantAttributeVal = MaxVal;
            }
         }
         
         return MaxIndex; 
    }
    
    public double findMaxVariantArrtibureValue()
    {
        return maxVariantAttributeVal;
    }
    
    public double CalculateMyWeight(int ExternalInstanceStamp)
    {
        
        // WAIT ..... 
        // MAX double can handle is 46312 AS (n^2 + n / 2) gives a Minus Number.... 
        
        if (ExternalInstanceStamp > 47000)
        {
            int n=0;
        }
        
        
        //look forward 
        ExternalInstanceStamp = ExternalInstanceStamp + 1;
        // Might have to Remove 
        int ClusterLength = (int)(ExternalInstanceStamp  - this.CF1TInitial);
        double MAXTrianglePossible = ((ExternalInstanceStamp * ExternalInstanceStamp) + ExternalInstanceStamp)  / 2; 
        MAXTrianglePossible = MAXTrianglePossible - this.CF1TInitialTriangle;
        double FullMean = MAXTrianglePossible / ClusterLength;

        double TempT1 = this.CF1T + ExternalInstanceStamp;
        
        double DataMean = TempT1 / (this.N+1);
        
        double PercentReturn = ((DataMean  * 100) / (FullMean)); 
        return PercentReturn;
        // Scale using RBS Exponential 
        
  //      double X = (DataMean - FullMean)/ (this.N + 1);
  //      double PercentReturn =  Math.exp(X); 
  //      PercentReturn = 100 * PercentReturn;
  //      return PercentReturn ;
    }
    
    
    public double CalculateMyBigWeight(int ExternalInstanceStamp)
    {
        
        // WAIT ..... so use BigDecimal 
        // MAX double can handle is 46312 AS (n^2 + n / 2) gives a Minus Number.... 
        double ExternalInstanceStampD = ExternalInstanceStamp;
        if (ExternalInstanceStamp > 12000)
        {
            int n=0;
        }
            
        //look forward 
        ExternalInstanceStampD = ExternalInstanceStampD + 1;
        // Might have to Remove  
        
        if(BigCF1TInitialTriangle == null){
            BigCF1TInitialTriangle = new BigDecimal(((this.CF1TInitial * this.CF1TInitial)+(this.CF1TInitial))/2);
        }
       
        BigDecimal ClusterLength = new BigDecimal((int)(ExternalInstanceStampD  - this.CF1TInitial));
        BigDecimal MAXTrianglePossible = new BigDecimal (((ExternalInstanceStampD * ExternalInstanceStampD) + ExternalInstanceStampD)  / 2); 
        MAXTrianglePossible = MAXTrianglePossible.subtract(BigCF1TInitialTriangle);
    //    BigDecimal FullMean = (MAXTrianglePossible.divide( ClusterLength ));

        BigDecimal TempT1 = new BigDecimal(this.CF1T + ExternalInstanceStampD);
        
        // REMOVED 
        //double DataMean = TempT1 / (this.N+1);
        
        BigDecimal PercentReturn = new BigDecimal(100); 
        PercentReturn = PercentReturn.multiply(TempT1);
        MathContext mc = new MathContext(2, RoundingMode.HALF_UP);
        PercentReturn = PercentReturn.divide(MAXTrianglePossible,mc);
        
        double REtVal = PercentReturn.doubleValue();
        return REtVal;
        // Scale using RBS Exponential 
        
  //      double X = (DataMean - FullMean)/ (this.N + 1);
  //      double PercentReturn =  Math.exp(X); 
  //      PercentReturn = 100 * PercentReturn;
  //      return PercentReturn ;
    }
    
    public double ClusterTimeStdDevVal()
    {
        double d =0 ;
        d = (CF1T/N) *(CF1T/N);
        d = (CF2T/N) - d; 
        return Math.sqrt(d);
    }
    
    public void CalculateClusterCentroid()
    {
        for (int i = 0; i < NumAttributes; i++) 
        {
            this.Centers[i] =  (double)(CF1X[i] / N);//SkipList[i].getMedianNodeValue();//.getMedian();//(CF1X[i] / N); //CentersBias[i] + 
        }
        
        // and now place in ZSpace 
    }
    
    public double EcludianDistancefromCentroid(double[] InstD)
    {
        // need to add one to get rid of Zeros.... 
        
        double RunningTOT = 0 ; 
        double SqrVAL = 0; 
        
        for (int i = 0; i < this.NumAttributes; i++) {
               SqrVAL = ((Centers[i]+1) - (InstD[i]+1));
               RunningTOT += (SqrVAL * SqrVAL);
        }
        return Math.sqrt(RunningTOT);     
    }
    
    public void CalculateClusterTimeMeanVal()
    {
       ClusterTimeMeanVal =  (CF1T / N);
    }

    
    public void ResetCluster(int NumAttrubutes)
    {
        Intitialized = false;
        CF1X = new double[NumAttrubutes];
        CF2X = new double[NumAttrubutes];
        Centers = new double[NumAttrubutes];
            //CentersBias = new double[NumAttrubutes];
        CF1T = 0;
        CF2T = 0;
        N=0; 
        CF1TInitial = -1; 
        LastUpdateStreamCount = 0; 
        MicroClusterID++;
            
        /*
        Low Pass Filter
        */
        LowPassFilter_Old = new double[NumAttrubutes];
            
        for(int i=0; i<NumAttrubutes; i++)
            LowPassFilter_Old[i]=0;
            
        /*
        SkipList
        */
            
        //SkipList = new SkipList[NumAttrubutes];
            
        //for(int i=0; i<NumAttrubutes; i++)
        //    SkipList[i]= new SkipList(MaxNodes_SkipList, MaxHeads_SkipList);
        
        /*
        FIFO
        */
        FIFO = new FIFO_LinkedList[NumAttrubutes];
        for(int i=0; i<NumAttrubutes; i++)
            FIFO[i]= new FIFO_LinkedList();
        
        /*
        Creating a random UUID (Universally unique identifier).
        */
        uuid = UUID.randomUUID();
        //String randomUUIDString = uuid.toString();
        NumberParticipationsFalsePositive = 0;
        NumberParticipationsTruePositive = 0;
        
        MaximumValue = new double[NumAttrubutes];
        MinimumValue = new double[NumAttrubutes];
        
        this.CopyData();
    }
    
     public void ResetCluster()
    {
        Intitialized = false;
        CF1X = new double[this.NumAttributes];
        CF2X = new double[NumAttributes];
        Centers = new double[NumAttributes];
        //CentersBias = new double[NumAttributes];
        CF1T = 0;
        CF2T = 0;
        N=0; 
        CF1TInitial = -1; 
        LastUpdateStreamCount = 0; 
        MicroClusterID++;
            
        /*
        Low Pass Filter
        */
        LowPassFilter_Old = new double[this.NumAttributes];
            
        for(int i=0; i<this.NumAttributes; i++)
            LowPassFilter_Old[i]=0;
            
        /*
        SkipList
        */
            
        //SkipList = new SkipList[this.NumAttributes];
            
        //for(int i=0; i<this.NumAttributes; i++)
        //    SkipList[i]= new SkipList(MaxNodes_SkipList, MaxHeads_SkipList);
        
        /*
        FIFO
        */
        FIFO = new FIFO_LinkedList[this.NumAttributes];
        for(int i=0; i<this.NumAttributes; i++)
            FIFO[i]= new FIFO_LinkedList();
        
        /*
        
        */
        /*
        Creating a random UUID (Universally unique identifier).
        */
        uuid = UUID.randomUUID();
        
        NumberParticipationsFalsePositive =0;
        NumberParticipationsTruePositive =0;
        
        MaximumValue = new double[this.NumAttributes];
        MinimumValue = new double[this.NumAttributes];
        
        this.CopyData();
    }
    
     public void ResetClusterSplit()
    {
        Intitialized = false;
        //CF1X = new double[this.NumAttributes];
        CF2X = new double[NumAttributes];
        CF1T = 0;
        CF2T = 0;
        N=0; 
        CF1TInitial = -1; 
        LastUpdateStreamCount = 0; 
        
        //MicroClusterID++;
        //LowPassFilter_Old = new double[this.NumAttributes];
            
        //for(int i=0; i<this.NumAttributes; i++)
        //    LowPassFilter_Old[i]=0;
        
        uuid = UUID.randomUUID();
        //if(NumberParticipationsFalsePositive>0)
        NumberParticipationsFalsePositive = 0;
        //NumberParticipationsTruePositive = 1;
        
        MaximumValue = new double[NumAttributes];
        MinimumValue = new double[NumAttributes];
        
        this.CopyData();
    }
    
    //public void InisaliseCluster(double[] instD, int TimeStamp, double BoundaryLength)
    //{
    //    double BAddition = BoundaryLength;
    //    double BSubtraction = BoundaryLength * -1; 
    //        
    //    double[] DD1 = new double[NumAttributes];
    //    double[] DD2 = new double[NumAttributes];
    //        
    //    for (int i = 0; i < this.NumAttributes ; i++) 
    //    {
    //        DD1[i] = instD[i] + BAddition; 
    //        DD2[i] = instD[i] + BSubtraction; 
    //    }
    //    
    //    IncrementCluster(DD1,TimeStamp);
    //    IncrementCluster(DD2,TimeStamp);
    //}
        
    public void IncrementCluster(double[] inst, int TimeStamp, boolean TriangleNumberApplyOption)
    {   
        /*
        Apply Low Pass Filter
        */
        Intitialized = true;
        
        if(LowPassFilterOption){
            double[] inst_Filtered = LowPassFilter_Compute(inst);
            Increment_CF1X_FIFO(inst_Filtered);
            //IncrementNodes(inst_Filtered);
            //IncrementFIFO(inst_Filtered);
            //IncrementCF1X(inst_Filtered);
            //IncrementCF2X(inst_Filtered);
        }else{
            Increment_CF1X_FIFO(inst);
            //IncrementNodes(inst);
            //IncrementFIFO(inst);
            //IncrementCF1X(inst);
            //IncrementCF2X(inst);
        }
        
        IncrementCF1T(TimeStamp);
        
        if(TriangleNumberApplyOption){
            IncrementCF2T(TimeStamp);
            
            if (CF1TInitial==-1)
            {
                CF1TInitial = TimeStamp; 
                CF1TInitialTriangle = ((TimeStamp * TimeStamp) + TimeStamp) / 2; 
                BigCF1TInitialTriangle = new BigDecimal(((this.CF1TInitial * this.CF1TInitial)+(this.CF1TInitial))/2);
            }
        }
        
        N++; 
        CalculateClusterCentroid();
        
        if(TriangleNumberApplyOption){
            CalculateClusterTimeMeanVal();
            LastTTL = TimeStamp;
            this.TTL += this.TTLIncrementVal; 
            LastUpdateStreamCount = TimeStamp;
        }
    }  
    
    public void IncrementClusterTimeStamp(int TimeStamp)
    {   
        
        IncrementCF1T(TimeStamp);
        IncrementCF2T(TimeStamp);
        
        if (CF1TInitial==-1)
        {
            CF1TInitial = TimeStamp; 
            CF1TInitialTriangle = ((TimeStamp * TimeStamp) + TimeStamp) / 2; 
            BigCF1TInitialTriangle = new BigDecimal(((this.CF1TInitial * this.CF1TInitial)+(this.CF1TInitial))/2);
        }
        
        N++; 
        CalculateClusterTimeMeanVal();
        LastTTL = TimeStamp;
        this.TTL += this.TTLIncrementVal; 
        
        LastUpdateStreamCount = TimeStamp;
    }
    
    public Object[] getFIFOnodes(int featureIndex)
    {
        return (Object[])FIFO[featureIndex].getNodes();
    }
    
    public double[] getFIFOnodes_double(int featureIndex)
    {
        return (double[])FIFO[featureIndex].getNodes_double();
    }
    //public void GenerateSkipList(int featureIndex)
    //{
    //    Object[] array = FIFO[featureIndex].getNodes();
    //    for(int i=0; i<array.length; i++)
    //        SkipList[featureIndex].addNode((double)array[i],-1);
    //}
    
    //private void IncrementNodes(double[]  instD)
    //{
    //    for (int i = 0; i < this.NumAttributes; i++) 
    //    {
    //        SkipList[i].addNode((double)instD[i],-1);
    //    }
    //}
    
    private void IncrementFIFO(double[]  instD)
    {
        for (int i = 0; i < this.NumAttributes; i++) 
        {
            if((int)FIFO[i].getSize() >= MaxNodes_SkipList){
                double deleteData = FIFO[i].getFirstNode();
                FIFO[i].deleteFirstNode();
                CF1X[i] -= deleteData;
            }
            FIFO[i].addNode((double)instD[i]);
        }
    }
    
    private void IncrementCF1X(double[]  instD)
    {
           for (int i = 0; i < this.NumAttributes; i++) 
            {
                CF1X[i] += instD[i];
            }
    }
    
    private void Increment_CF1X_FIFO(double[]  instD)
    {
        for (int i = 0; i < this.NumAttributes; i++) 
        {
            if((int)FIFO[i].getSize() >= MaxNodes_SkipList){
                //double deleteData = FIFO[i].getFirstNode();
                FIFO[i].deleteFirstNode();
                //CF1X[i] -= deleteData;
            }
            FIFO[i].addNode((double)instD[i]);
            CF1X[i] += instD[i];
            
            if(instD[i]>MaximumValue[i])
                MaximumValue[i] = instD[i];
            
            if(instD[i]<MinimumValue[i])
                MinimumValue[i] = instD[i];
        }
    }
    
    private void IncrementCF1T(int TimeStamp)
    {
        CF1T += TimeStamp;
    }
    
    private void IncrementCF2T(int TimeStamp)
    {
        CF2T += (TimeStamp * TimeStamp);
    }
    
    /*
    Added by Mahmood
    Backup MicroCluster data
    */
    public void CopyData(){
        CF1X_Old  = Arrays.copyOf(CF1X,this.NumAttributes); 
        CF2X_Old  = Arrays.copyOf(CF2X,this.NumAttributes); 
        CF1T_Old  = CF1T;
        CF2T_Old  = CF2T;
        N_Old = N;
        Centers_Old = Arrays.copyOf(Centers,this.NumAttributes);
    }
    
    public double[] LowPassFilter_Compute(double[] inst){
        
        //LowPassFilter_Old
        double currentAttributeValue = 0;
        double NewFilterValue = 0;
        
        for(int attribIndex = 0; attribIndex < LowPassFilter_Old.length; attribIndex++){
            currentAttributeValue = (double)inst[attribIndex];
            NewFilterValue = AlphaLowPassFilter * currentAttributeValue + (1-AlphaLowPassFilter) * LowPassFilter_Old[attribIndex];
            inst[attribIndex] = NewFilterValue;
            LowPassFilter_Old[attribIndex] = NewFilterValue;
        }
        
        return (double[]) inst;
    }
    
    public void ParticipationFalsePositiveIncrement(){
        NumberParticipationsFalsePositive++;
    }
    
    public void ParticipationFalsePositiveDecrement(){
        NumberParticipationsFalsePositive--;
    }
    
    public void ParticipationTruePositiveIncrement(){
        NumberParticipationsTruePositive++;
    }
    
   // public void addNode(int featureIndex, double featureValue){
   //     SkipList[featureIndex].addNode(featureValue,-1);
    //}
    
    //public double getMedian(int featureIndex){
    //    return (double)SkipList[featureIndex].getMedianNodeValue();//.getMedian();
    //}
    
    //public double[] getSortedNodes(int featureIndex){
    //    return (double[])SkipList[featureIndex].RetrieveNodes(); 
    //}
}

