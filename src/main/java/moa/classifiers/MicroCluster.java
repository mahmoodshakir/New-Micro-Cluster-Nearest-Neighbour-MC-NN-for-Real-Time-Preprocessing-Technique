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
import weka.core.Instance;

/**
 *
 * @author Mark Tennant @ Reading University :2015
 */
public class MicroCluster 
{
    private BigDecimal BigCF1TInitialTriangle;
    public double[] CF1X  = {-1}; 
    public double[] CF2X  = {-1}; 
    public double CF1T  = -1;    
    public double CF1TInitial  = -1; 
    public double CF1TInitialTriangle = -1; 
    public double CF2T  = -1; 
    public int N = 0;  
    double[] Centers = {-1};
    double[] CentersBias = {-1};
    public double ClassIndex = -1; 
    public double BoundaryDistanceRadius = -1; 
    public double STDDeviation = -1;
    double[] Variants = {-1};
    public int NumAttributes;
    public int LastTTL = -1; 
    public int TTL= 10; 
    public int TTLIncrementVal = -1; 
    public double ClusterTimeMeanVal = -1; 
    public double[] VariantsValuesArray;
    /*
    Added by Mahmood
    MicroCluster ID
    */
    public int MicroClusterID = 0;
    public int NumberParticipationsFalsePositive=0;
    public int NumberParticipationsTruePositive =0;
    public double[] CF1X_Old  = {-1}; 
    public double[] CF2X_Old  = {-1}; 
    public double CF1T_Old  = -1;
    public double CF2T_Old  = -1;
    public int N_Old = 0;
    double[] Centers_Old = {-1};
    
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
    //public int NumberParticipations = 0;
    
    public void IncrementTestingAges()
    {
        //StreamItemCount++;
        //StreamTotal = StreamTotal + StreamItemCount;
        //StreamSquareTotal = StreamSquareTotal + (StreamItemCount * StreamItemCount);
    }
    
    public MicroCluster(int ClassIndexD,int numAttributes, int InstnceTimeStamp, boolean LPF, double AlphaLPF)
    {
        this.ClassIndex = ClassIndexD;        
        NumAttributes = numAttributes;
        ResetCluster(NumAttributes);
        
        this.LastUpdateStreamCount = InstnceTimeStamp;
        LowPassFilterOption = LPF;
        AlphaLowPassFilter = AlphaLPF;
    }
    
    
    public MicroCluster(int ClassIndexD,int numAttributes)
    {
        this.ClassIndex = ClassIndexD;        
        NumAttributes = numAttributes;
        ResetCluster(NumAttributes);
    }
    
    /*
    Added by Mahmood 
    Return array of high variant listed from low to high
    */
    
    public double[] getVariance_visualization(){
        VariantsValuesArray = new double[NumAttributes];
        
        double CF1VAL = 0; 
        double variantValue = 0;
        
        for (int i = 0; i < this.NumAttributes; i++) 
        {
            CF1VAL = (CF1X[i] / this.N); 
            CF1VAL = (CF1VAL * CF1VAL);
            variantValue = (double)((CF2X[i] / this.N) - (CF1VAL));
            VariantsValuesArray[i] +=  variantValue;     
        }
        
        return VariantsValuesArray;
    }
    
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
        Variants = new double[NumAttributes];
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
    
    public double[] getVariants(){
        return Variants;
    }
    
    public double findMaxVariantArrtibureValue()
    {
        return maxVariantAttributeVal;
    }
    
    
    public void AddMergeLocation(double[] CF1XNew,double[] CF2XNew,double CF1TNew,double CF2TNew ,int NNew)
    {
        if (CF1X.length != CF1XNew.length)
        {
            CF1X = new double[CF1XNew.length];
            CF2X = new double[CF1XNew.length];
            Centers = new double[CF1XNew.length];
            CentersBias = new double[CF1XNew.length] ;
        }
        
        for (int i = 0; i < CF1X.length; i++) 
        {
            CF1X[i] += CF1XNew[i];
            CF2X[i] += CF2XNew[i];
        }        
        CF1T += CF1TNew;
        CF1T += CF2TNew;
        N += NNew; 
        
        CalculateClusterCentroid();
        this.CalculateBoundaryDistanceRadius();
        this.CalculateClusterTimeMeanVal();
        this.CalculateSTDDeviation();
    }
    
    public void ForgetFactor(int NewN)
    {
        double Multiplier = N / NewN; 
                
        N  = (int)(N / Multiplier);
        for (int i = 0; i < this.NumAttributes; i++) 
        {
            this.CF1X[i] = this.CF1X[i] / Multiplier;
            this.CF2X[i] = this.CF2X[i] / Multiplier;
        }   
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
    
    
    private void CalculateSTDDeviation()
    {
        double SqTOT = 0; 
        for (int i = 0; i < this.NumAttributes; i++) 
        {
            SqTOT += this.CF2X[i]; 
        }
        STDDeviation =  Math.sqrt(SqTOT / N);
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
            this.Centers[i] =  CentersBias[i] + (CF1X[i] / N);
        }
        
        // and now place in ZSpace 
    }
    
    public void CalculateBoundaryDistanceRadius()
    {
        double R = 0; 
        double[] BoundaryDimension = new double[NumAttributes];
        double CF1VAL = 0; 
            for (int i = 0; i < this.NumAttributes; i++) 
            {
                CF1VAL = (CF1X[i] / this.N); 
                CF1VAL = (CF1VAL * CF1VAL);
                BoundaryDimension[i] += ((CF2X[i] / this.N) - (CF1VAL)) ; 
            }
            
            for (int i = 0; i < this.NumAttributes; i++) 
            {
                R += BoundaryDimension[i] * BoundaryDimension[i]; 
            }
        BoundaryDistanceRadius = Math.sqrt(R);
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
    
    public double EcludianDistancefromBoundary(double[] InstD)
    {
        // need to add one to get rid of Zeros.... 
        // DistA  = Point to Center 
        // DistB  = Center to Boundary 
        // DistC = Boundary to Point 
        
        double DistA = EcludianDistancefromCentroid(InstD);
        double DistB = this.BoundaryDistanceRadius;
        double DistC = DistA - DistB;
        
        return DistC; 
    }
    
    public double RMSDeviation(double[] InstD)
    {   
        double RunningTOT = 0; 
        double SumSQ = 0;
        for (int i = 0; i < this.NumAttributes; i++) 
        {
              SumSQ = (InstD[0] - Centers[i]);
              RunningTOT += (SumSQ * SumSQ);
        }
        RunningTOT = (RunningTOT /N);
        return Math.sqrt(RunningTOT);
    }
    
    public void CalculateClusterTimeMeanVal()
    {
       ClusterTimeMeanVal =  (CF1T / N);
    }

    
    public void ResetCluster(int NumAttrubutes)
    {
            CF1X = new double[NumAttrubutes];
            CF2X = new double[NumAttrubutes];
            Centers = new double[NumAttrubutes];
            CentersBias = new double[NumAttrubutes];
            Variants = new double[NumAttrubutes];
            CF1T = 0;
            CF2T = 0;
            N=0; 
            CF1TInitial = -1; 
            LastUpdateStreamCount = 0; 
            
            /*
            Added by Mahmood
            Microcluster ID
            */
            MicroClusterID++;
            
            /*
            Low Pass Filter
            */
            LowPassFilter_Old = new double[NumAttrubutes];
            
            for(int i=0; i<NumAttrubutes; i++)
                LowPassFilter_Old[i]=0;
            
            NumberParticipationsFalsePositive = 0;
            NumberParticipationsTruePositive = 0;
        
            /*
            Reset backup objects
            */
            //CF1X_Old  = new double[NumAttrubutes]; 
            //CF2X_Old  = new double[NumAttrubutes]; 
            //CF1T_Old  = 0;
            //CF2T_Old  = 0;
            //N_Old = 0;
            //Centers_Old = new double[NumAttrubutes];
            
            /*
            Creating a random UUID (Universally unique identifier).
            */
            uuid = UUID.randomUUID();
            //NumberParticipations = 0;
            //String randomUUIDString = uuid.toString();
            this.CopyData();
    }
    
     public void ResetCluster()
    {
            CF1X = new double[this.NumAttributes];
            CF2X = new double[NumAttributes];
            Centers = new double[NumAttributes];
            CentersBias = new double[NumAttributes];
            Variants = new double[NumAttributes];
            CF1T = 0;
            CF2T = 0;
            N=0; 
            CF1TInitial = -1; 
            LastUpdateStreamCount = 0; 
            
            /*
            Added by Mahmood
            Microcluster ID
            */
            MicroClusterID++;
            
            /*
            Low Pass Filter
            */
            LowPassFilter_Old = new double[this.NumAttributes];
            
            for(int i=0; i<this.NumAttributes; i++)
                LowPassFilter_Old[i]=0;
            
            NumberParticipationsFalsePositive = 0;
            NumberParticipationsTruePositive = 0;
            /*
            Reset backup objects
            */
            //CF1X_Old  = new double[this.NumAttributes]; 
            //CF2X_Old  = new double[this.NumAttributes]; 
            //CF1T_Old  = 0;
            //CF2T_Old  = 0;
            //N_Old = 0;
            //Centers_Old = new double[this.NumAttributes];
            
            /*
            Creating a random UUID (Universally unique identifier).
            */
            uuid = UUID.randomUUID();
            //NumberParticipations = 0;
            
            this.CopyData();
    }
       
     public void ParticipationFalsePositiveIncrement(){
        NumberParticipationsFalsePositive++;
    }
    
    public void ParticipationFalsePositiveDecrement(){
        NumberParticipationsFalsePositive--;
    }
    
    public void InisaliseCluster(double[] instD, int TimeStamp, double BoundaryLength)
    {
        double BAddition = BoundaryLength;
        double BSubtraction = BoundaryLength * -1; 
            
        double[] DD1 = new double[NumAttributes];
        double[] DD2 = new double[NumAttributes];
            
        for (int i = 0; i < this.NumAttributes ; i++) 
        {
            DD1[i] = instD[i] + BAddition; 
            DD2[i] = instD[i] + BSubtraction; 
        }
        
        IncrementCluster(DD1,TimeStamp);
        IncrementCluster(DD2,TimeStamp);
    }
        
    public void IncrementCluster(double[] inst, int TimeStamp)
    {   
        /*
        Apply Low Pass Filter
        */
       
        if(LowPassFilterOption){
            double[] inst_Filtered = LowPassFilter_Compute(inst);
            
            IncrementCF1X(inst_Filtered);
            IncrementCF2X(inst_Filtered);
        }else{
            IncrementCF1X(inst);
            IncrementCF2X(inst);
        }
        
        IncrementCF1T(TimeStamp);
        IncrementCF2T(TimeStamp);
        
        if (CF1TInitial==-1)
        {
            CF1TInitial = TimeStamp; 
            CF1TInitialTriangle = ((TimeStamp * TimeStamp) + TimeStamp) / 2; 
            BigCF1TInitialTriangle = new BigDecimal(((this.CF1TInitial * this.CF1TInitial)+(this.CF1TInitial))/2);
        }
        
        
        N++; 
        CalculateClusterCentroid();
        CalculateBoundaryDistanceRadius();
        CalculateClusterTimeMeanVal();
        CalculateSTDDeviation();
        LastTTL = TimeStamp;
        this.TTL += this.TTLIncrementVal; 
        
        LastUpdateStreamCount = TimeStamp;
    }  
            
    private void IncrementCF1X(double[]  instD)
    {
           for (int i = 0; i < this.NumAttributes; i++) 
            {
                CF1X[i] += instD[i];
            }
    }
    
     private void IncrementCF2X(double[] instD)
    {
           for (int i = 0; i < this.NumAttributes; i++) 
            {
                CF2X[i] += instD[i] * instD[i];
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
            
            //if(attribIndex == 2){
            //    System.out.println(currentAttributeValue + " " + NewFilterValue);
            //}
            
            inst[attribIndex] = NewFilterValue;
            LowPassFilter_Old[attribIndex] = NewFilterValue;
        }
        
        return (double[]) inst;
    }
    
    //public void ParticipationIncrement(){
    //    NumberParticipations++;
    //}
}
