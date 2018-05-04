/*
 *    DriftDetectionMethodClassifier.java
 *    Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *    @author Manuel Baena (mbaena@lcc.uma.es)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package moa.classifiers.drift;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.MicroClustersSingle;
import moa.classifiers.meta.WEKAClassifier;
import moa.core.Measurement;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.core.InstancesHeader;
import moa.options.ClassOption;
import moa.streams.InstanceStream;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Class for handling concept drift datasets with a wrapper on a
 * classifier.<p>
 *
 * Valid options are:<p>
 *
 * -l classname <br>
 * Specify the full class name of a classifier as the basis for
 * the concept drift classifier.<p>
 * -d Drift detection method to use<br>
 *
 * @author Manuel Baena (mbaena@lcc.uma.es)
 * @version 1.1
 */
public class DriftDetectionMethodClassifier extends AbstractClassifier {

    private static final long serialVersionUID = 1L;
    
    @Override
    public String getPurposeString() {
        return "Classifier that replaces the current classifier with a new one when a change is detected in accuracy.";
    }
    
    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
            "Classifier to train.", Classifier.class, "bayes.NaiveBayes");
    
    public ClassOption driftDetectionMethodOption = new ClassOption("driftDetectionMethod", 'd',
             "Drift detection method to use.", ChangeDetector.class, "DDM");

    protected Classifier classifier;

    protected Classifier newclassifier;

    protected ChangeDetector driftDetectionMethod;

    protected boolean newClassifierReset;
    //protected int numberInstances = 0;

    protected int ddmLevel;

   /* public boolean isWarningDetected() {
        return (this.ddmLevel == DriftDetectionMethod.DDM_WARNING_LEVEL);
    }

    public boolean isChangeDetected() {
        return (this.ddmLevel == DriftDetectionMethod.DDM_OUTCONTROL_LEVEL);
    }*/

    public static final int DDM_INCONTROL_LEVEL = 0;

    public static final int DDM_WARNING_LEVEL = 1;

    public static final int DDM_OUTCONTROL_LEVEL = 2;
    
    /*
    Added by Mahmood
    save instance either with full feature set or subset (feature selection)
    */
    public Instance inst_withFeatureSelection;
    public int numattrib = 3;
    protected InstancesHeader streamHeader;
    public MicroClustersSingle MCS = new MicroClustersSingle();
    public int FeatureIndexes[];
    public int CorrectPredictClassifier = 0;
    public int[][] CorrectPredictTestArray;
    public boolean init_run = true;
    public int NumClasses = 0;
    public int WinningIndex;
    public boolean NewInst = false;
    
    @Override
    public void resetLearningImpl() {
        this.classifier = ((Classifier) getPreparedClassOption(this.baseLearnerOption)).copy();
        this.newclassifier = this.classifier.copy();
        this.classifier.resetLearning();
        this.newclassifier.resetLearning();
        this.driftDetectionMethod = ((ChangeDetector) getPreparedClassOption(this.driftDetectionMethodOption)).copy();
        this.newClassifierReset = false;
        
    }

    protected int changeDetected = 0;

    protected int warningDetected = 0;

    /*
    Added by Mahmood. Counting number of reclassify for each DDM method
    */
    
    protected int CountChangeDetected=0;
    protected int CountWarning=0;
    protected int CountTimeStamp=0;
    
    @Override
    public void trainOnInstanceImpl(Instance instCurrentTrain) {
        //this.numberInstances++;
        //Start after test
        
        instCurrentTrain = inst_withFeatureSelection;
        
        int trueClass = (int) instCurrentTrain.classValue();
        boolean prediction;
        
        if (Utils.maxIndex(this.classifier.getVotesForInstance(instCurrentTrain)) == trueClass) {
            prediction = true;
            CorrectPredictClassifier++;
        } else {
            prediction = false;
        }
       
        String LearnerName = driftDetectionMethod.getClass().getName();
        if(LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_ConceptDriftDetector" || LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_OutlierTracker"){
            this.driftDetectionMethod.InputInstance(instCurrentTrain);
        }else if(LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureTracker_FS" || LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureTracker_Median" || LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureTracker_DDM" || LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureTracker" || LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureSelection"){
            // Tentitative Accuracy
            
        }else{
            this.driftDetectionMethod.input(prediction ? 0.0 : 1.0);
        }
        
        if(this.driftDetectionMethod.ResetWindowSize()){           
            CountTimeStamp++;
            double OverallAccuracy = Math.abs((double) CorrectPredictClassifier / this.driftDetectionMethod.WindowSize()) * 100;
            System.out.println(OverallAccuracy);
            CorrectPredictClassifier = 0;
            //System.out.println(" Num Attrib: "+instCurrentTrain.numAttributes());
        }
        
        this.ddmLevel = DDM_INCONTROL_LEVEL;
        
        if (this.driftDetectionMethod.getChange()) {
         this.ddmLevel =  DDM_OUTCONTROL_LEVEL;
        }
        
        if (this.driftDetectionMethod.getWarningZone()) {
           this.ddmLevel =  DDM_WARNING_LEVEL;
        }
        switch (this.ddmLevel) {
            case DDM_WARNING_LEVEL:
                
                if (newClassifierReset == true) {
                    this.warningDetected++;
                    this.newclassifier.resetLearning();
                    newClassifierReset = false;
                }
                this.newclassifier.trainOnInstance(instCurrentTrain);
                break;

            case DDM_OUTCONTROL_LEVEL:
                
                CountChangeDetected++;
            	System.out.println("CountChangeDetected: "+ CountChangeDetected + " Time Stamp: "+CountTimeStamp);
                //
                //System.out.println("0 1 O");
            	//System.out.println("DDM_OUTCONTROL_LEVEL");
                this.changeDetected++;
                //if(LearnerName!="moa.classifiers.core.driftdetection.MicroCluster_FeatureTracker"){
                this.classifier = null;
                this.classifier = this.newclassifier;
                if (this.classifier instanceof WEKAClassifier) {
                    ((WEKAClassifier) this.classifier).buildClassifier();
                }
                this.newclassifier = ((Classifier) getPreparedClassOption(this.baseLearnerOption)).copy();
                this.newclassifier.resetLearning();
                
                //}
                
                break;
 
            case DDM_INCONTROL_LEVEL:
                //System.out.println("0 0 I");
            	//System.out.println("DDM_INCONTROL_LEVEL");
                newClassifierReset = true;
                break;
            default:
            //System.out.println("ERROR!");

        }
        
        //System.out.println("    number of attr. "+ instCurrentTrain.numAttributes()+" ");
        //for(int i = 0 ; i < instCurrentTrain.numAttributes()-1; i++)
            //System.out.print(instCurrentTrain.value(i)+ " ");
        //
        //int ClassIndex = instCurrentTrain.numAttributes()-1;
        //ClassIndex = instCurrentTrain.numAttributes()-1;
        //System.out.print(instCurrentTrain.stringValue(ClassIndex));
        //System.out.println(instCurrentTrain.numAttributes());
        
        if(NewInst)
            PrintNewInstance(instCurrentTrain);
        
        this.classifier.trainOnInstance(instCurrentTrain);
    }

    public void resetCorrectPredictTestArray(){
        CorrectPredictTestArray = new int[NumClasses][NumClasses];
        for(int i=0; i<NumClasses; i++){
            for(int j=0; j<NumClasses; j++){
                CorrectPredictTestArray[i][j] = 0;
            }
        }
    }
            
    @Override
    public double[] getVotesForInstance(Instance instCurrentTest) {
        
        String LearnerName = driftDetectionMethod.getClass().getName();
        
        //inst_to_classifierArray = instCurrentTest.toDoubleArray();
        
        //PrintCurrentInstance(inst_to_classifier);

        if(LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureTracker_FS" || LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureTracker_Median" || LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureTracker_DDM" || LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureTracker" || LearnerName=="moa.classifiers.core.driftdetection.MicroCluster_FeatureSelection"){
            
            this.driftDetectionMethod.InputInstance(instCurrentTest);
            
            if(this.driftDetectionMethod.FeatureSelectionIsApplied()){
                
                if(!this.driftDetectionMethod.CreateNewInstance()){
                    inst_withFeatureSelection = InstanceWithFeatureSelection(instCurrentTest,this.driftDetectionMethod.FeatureSelectedIndexes());
                    //NewInst = false;
                }else{
                    inst_withFeatureSelection = InstanceWithMissingValues(instCurrentTest,this.driftDetectionMethod.FeatureSelectedIndexes());
                    //NewInst = true;
                }
                    //inst_withFeatureSelection = InstanceGenerator(instCurrentTest,this.driftDetectionMethod.FeatureSelectedIndexes());
                
                //PrintNewInstance(inst_withFeatureSelection);
                
            }else{
                
                inst_withFeatureSelection = instCurrentTest;
            }
        }else{
            inst_withFeatureSelection = instCurrentTest;
        }
        
        return this.classifier.getVotesForInstance(inst_withFeatureSelection);
    }
    
    public void PrintCurrentInstance(Instance currentInstance){
        System.out.print(" Old Instance: ");
        for(int i = 0 ; i < currentInstance.numAttributes()-1; i++)
            System.out.print(currentInstance.value(i)+ " ");
        
        int ClassIndex = currentInstance.numAttributes()-1;
        System.out.print(currentInstance.stringValue(ClassIndex));
        System.out.println(" ");
    }
    
    public void PrintNewInstance(Instance newInstance){
        System.out.print(" New Instance: ");
                
        for(int i = 0 ; i < newInstance.numAttributes()-1; i++)
            System.out.print(newInstance.value(i)+ " ");
        
        int ClassIndex = newInstance.numAttributes()-1;
        System.out.print(newInstance.stringValue(ClassIndex));
        System.out.println(" ");
                
        System.out.println(" Instance Created succesfully");
    }
    
    @Override
    public boolean isRandomizable() {
        return true;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        ((AbstractClassifier) this.classifier).getModelDescription(out, indent);
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        List<Measurement> measurementList = new LinkedList<Measurement>();
        measurementList.add(new Measurement("Change detected", this.changeDetected));
        measurementList.add(new Measurement("Warning detected", this.warningDetected));
        Measurement[] modelMeasurements = ((AbstractClassifier) this.classifier).getModelMeasurements();
        if (modelMeasurements != null) {
            for (Measurement measurement : modelMeasurements) {
                measurementList.add(measurement);
            }
        }
        this.changeDetected = 0;
        this.warningDetected = 0;
        return measurementList.toArray(new Measurement[measurementList.size()]);
    }
    
    public Instance InstanceWithFeatureSelection(Instance inst2, int[] arrayOfSelectedFeatures){
        for(int column = 0 ; column < arrayOfSelectedFeatures.length-1; column++){
            if(arrayOfSelectedFeatures[column] == 1){
                inst2.setValue(column, 0);
                //
                //inst2.setMissing(column);
                //inst2.dataset().deleteWithMissing(column);
                
                //inst2.deleteAttributeAt(column);
            }
        }
        
        return inst2;
    }
    
    public Instance InstanceWithMissingValues(Instance inst2, int[] arrayOfSelectedFeatures){
        for(int column = 0 ; column < arrayOfSelectedFeatures.length-1; column++){
            if(arrayOfSelectedFeatures[column] == 1){
                inst2.setMissing(column);
            }
        }
        
        return inst2;
    }
    
    /*
    Added by Mahmood
    */
    
    public Instance InstanceGenerator(Instance instCurrent, int[] arrayOfSelectedFeatures){
        int ClassIndex = instCurrent.numAttributes()-1;
        String Classvalue = instCurrent.stringValue(ClassIndex);
        String Classname = instCurrent.classAttribute().name();
        
        FastVector attributes = new FastVector();
        
        for(int i = 0 ; i < arrayOfSelectedFeatures.length-1; i++){ //inst.numAttributes()-2 // numSelectedAttributes
            if(arrayOfSelectedFeatures[i]==0){
                String AttributeName = instCurrent.attribute(i).name();
                attributes.addElement(new Attribute(AttributeName));
            }
        }
        
        FastVector classLabels = new FastVector();
        
        for(int i=0; i<instCurrent.classAttribute().numValues(); i++)
            classLabels.addElement(instCurrent.classAttribute().value(i));
        
        attributes.addElement(new Attribute(Classname, classLabels));
        this.streamHeader = new InstancesHeader(new Instances(getCLICreationString(InstanceStream.class), attributes, 0));
        this.streamHeader.setClassIndex(this.streamHeader.numAttributes() - 1);
        
        InstancesHeader header = this.streamHeader;
        Instance newInst = new DenseInstance(header.numAttributes());
        
        int column = -1;
        for(int i = 0 ; i < arrayOfSelectedFeatures.length-1; i++){ //inst.numAttributes()-2
            if(arrayOfSelectedFeatures[i]==0){
                column++;
                newInst.setValue(column, instCurrent.value(i)); //
            }
        }
        
        newInst.setDataset(header);
        //String str = inst.classAttribute().toString(); retrieve a list of class values
        newInst.setClassValue(Classvalue); //inst.classValue()
        
        //System.out.println(newInst.numAttributes());
        
        return newInst;
    }
    
    public Instance InstanceGeneratorSwapingFeatures(Instance inst, int[] arrayOfHighVariantsAttributes){
        int ClassIndex = inst.numAttributes()-1;
        String Classvalue = inst.stringValue(ClassIndex);
        String Classname = inst.classAttribute().name();
        
        FastVector attributes = new FastVector();
        
        for(int i = arrayOfHighVariantsAttributes.length-2 ; i >=0 ; i--){ //inst.numAttributes()-2 // numSelectedAttributes
            //if(arrayOfHighVariantsAttributes[i]==0){
            String AttributeName = inst.attribute(i).name();
            attributes.addElement(new Attribute(AttributeName));
            //}
        }
        
        FastVector classLabels = new FastVector();
        
        for(int i=0; i<inst.classAttribute().numValues(); i++)
            classLabels.addElement(inst.classAttribute().value(i));
        
        attributes.addElement(new Attribute(Classname, classLabels));
        
        this.streamHeader = new InstancesHeader(new Instances(getCLICreationString(InstanceStream.class), attributes, 0));
        this.streamHeader.setClassIndex(this.streamHeader.numAttributes() - 1);
        
        InstancesHeader header = this.streamHeader;
        
        Instance newInst = new DenseInstance(header.numAttributes());
        
        //int column = -1;
        for(int i = arrayOfHighVariantsAttributes.length-2 ; i >=0 ; i--){ //inst.numAttributes()-2
            //if(arrayOfHighVariantsAttributes[i]==0){
                //column++;
                newInst.setValue(i, inst.value(i));
            //}
        }
        
        newInst.setDataset(header);
        //String str = inst.classAttribute().toString(); retrieve a list of class values
        newInst.setClassValue(Classvalue); //inst.classValue()
        
        return newInst;
    }
    
    public Instance InstanceGeneratorSwapingFeaturesSeaGeneratorOnly(Instance inst, int[] arrayOfHighVariantsAttributes){
                
        int IndexSelectedHighVariant=0;
        
        for(int i = arrayOfHighVariantsAttributes.length-2 ; i >=0 ; i--){ 
            if(arrayOfHighVariantsAttributes[i]==0){
                IndexSelectedHighVariant = i;
            }
        }
                
        double selectedValueToBeSwapped = inst.value(IndexSelectedHighVariant);
        double SwapValue = inst.value(0);
        inst.setValue(0, selectedValueToBeSwapped);
        inst.setValue(IndexSelectedHighVariant,SwapValue);
        
        System.out.println("Feature with high variant is swapped/flipped");
        
        return inst;
    }
}
