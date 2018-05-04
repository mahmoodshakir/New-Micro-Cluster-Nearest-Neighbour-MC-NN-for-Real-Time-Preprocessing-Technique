/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MathPackages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 *
 * @author Mahmood @ University of Reading 2016
 * Normalization
 */

public class NormalizeAttributes {

    private String filePath = "c:/NormalizedDataset.xls";
    public double[] MinValue;
    public double[] MaxValue;
    public double[] NormalAttributes;
    public double currentAttributeValue = 0;
    public double previousNormalAttribute = 0;
    public boolean initial_class = true;
    public boolean initial_arffHeader = true;
    public static Instances data;
    public double RangeMin = 0;
    public double RangeMax = 100;
        
    public NormalizeAttributes() throws IOException{
        initialFile(filePath);
    }
    
    public void init(int attribNum){
        MinValue = new double[attribNum];
        MaxValue = new double[attribNum];
        NormalAttributes = new double[attribNum];
        
        for(int index = 0; index < attribNum; index++){
            MinValue[index] = 0;
            MaxValue[index] = 0;
        }
    }
    
    public void compute(Instance InstanceData){
        /*
        Normalize the attribute
        */
        if(initial_class){
            initial_class = false;
            
            for(int attribIndex = 0; attribIndex < MinValue.length; attribIndex++){
               
                MinValue[attribIndex] = InstanceData.value(attribIndex);
                MaxValue[attribIndex] = InstanceData.value(attribIndex);
           
            }
            
        }else{
            for(int attribIndex = 0; attribIndex < MinValue.length; attribIndex++){
                currentAttributeValue = (double)InstanceData.value(attribIndex);
              
                /*
                Chack Minimum attibute value
                */
                if(currentAttributeValue<MinValue[attribIndex]){
                    MinValue[attribIndex] = currentAttributeValue;
                }
        
                /*
                Chack Maximum attibute value
                */
                if(currentAttributeValue>MaxValue[attribIndex]){
                    MaxValue[attribIndex] = currentAttributeValue;
                }
                
                if(MaxValue[attribIndex] - MinValue[attribIndex]!= 0){
                    NormalAttributes[attribIndex] += (double)((currentAttributeValue - MinValue[attribIndex])/(MaxValue[attribIndex] - MinValue[attribIndex])) * (RangeMax - RangeMin) + RangeMin;
                }else{
                    NormalAttributes[attribIndex] += 0;
                }
            }
        } 
    }
    
    public double[] getMinValue(){
        return MinValue; 
    }
     
    public double[] getMaxValue(){
        return MaxValue; 
    }
    
    public double[] getNormalAttributes(){
        return NormalAttributes; 
    }
    
    public void resetArrays(int attribNum){
        init(attribNum);
        //for(int i=0; i<attribNum; i++){
        //    MinValue[i] = 0;
        //    MaxValue[i] = 0;
        //    NormalAttributes[i] = 0;
        //}
    }
    
    public void resetNormalAttributesArray(int attribNum){
        NormalAttributes = new double[attribNum];
        //for(int i=0; i<attribNum; i++){
        //    NormalAttributes[i] = 0;
        //}
    }

    public void identifyMaxMinAttribValue(Instance inst){
        double AttributeValue = 0;
        
        if(initial_class){
            initial_class = false;
            
            for(int index = 0; index < inst.numAttributes()-1; index++){
               
                MinValue[index] = inst.value(index);
                MaxValue[index] = inst.value(index);
           
            }
            
        }else{
            for(int index = 0; index < inst.numAttributes()-1; index++){
                AttributeValue = (double)inst.value(index);
           
                /*
                Chack Minimum attibute value
                */
                if(AttributeValue<MinValue[index]){
                    MinValue[index] = AttributeValue;
                }
        
                /*
                Chack Maximum attibute value
                */
                if(AttributeValue>MaxValue[index]){
                    MaxValue[index] = AttributeValue;
                }
            }
        }
    }
    
    public void PrintMinMaxValue(int NumAttributes){
        System.out.println(" MinValue ");
        for(int index = 0; index < NumAttributes; index++){
            System.out.print(MinValue[index]+ " ");
        }
        
        System.out.println(" ");
        System.out.println(" MaxValue ");
        for(int index = 0; index < NumAttributes; index++){
            System.out.print(MaxValue[index]+ " ");
        }
    }
    
    public Instance computeNormalization(Instance inst){
        
        double normalizedValue = 0;
        double AttribValue = 0;
        
        for(int index = 0; index < inst.numAttributes()-1; index++){
           AttribValue = inst.value(index);
           if(MaxValue[index] - MinValue[index] == 0){
               inst.setValue(index, 0);
           }else{
               normalizedValue = (double)((AttribValue - MinValue[index])/(MaxValue[index] - MinValue[index])) * (RangeMax - RangeMin) + RangeMin;
               inst.setValue(index, normalizedValue);
           }
        }
        
        return inst;
    }
    
    public static void main ( String[] args) throws FileNotFoundException, IOException{
        
        BufferedReader reader = new BufferedReader(new FileReader("D:/PhD Research - Frederic/Report MCNN DDM Low Pass Filter and Online Normalization/kddcup/kddcup-10Features_Swapped234With8910.arff")); 
	ArffReader arff = new ArffReader(reader, 1000);
        data = arff.getStructure();
        data.setClassIndex(data.numAttributes() - 1);
        Instance inst;
	inst = arff.readInstance(data);
        int NumAttributes = inst.numAttributes();
                
        NormalizeAttributes normalization = new NormalizeAttributes();
        normalization.init(NumAttributes);
        
        /*
        Identify Maximum and Minimum value of each attribute
        */
        int instanceNumber = 0;
        while ((inst = arff.readInstance(data)) != null) {
            data.add(inst);
            normalization.identifyMaxMinAttribValue(inst);
            instanceNumber++;
            
            System.out.println("Instance Number: "+instanceNumber);
         
        }
        
        /*
        Normalize the data
        */
        Instance instNew,instNormalized;
        int printedInstance = 0;
        
        for(int row = 0; row < instanceNumber; row++){
            instNew = data.instance(row);
            instNormalized = normalization.computeNormalization(instNew);
            
            normalization.writingNormalizedInstance(instNormalized);
            
            printedInstance = row+1;
            System.out.println("Normalized Instance Number: " + printedInstance);
        }
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
        }
    }
    
    public void writingNormalizedInstance(Instance inst) {
        
        String class_label = "";
        int column = 0;
            
        try {
            File TXTfile = new File(filePath);
            
            if(TXTfile.exists()){
                
                FileWriter fw = new FileWriter(filePath,true);
                
                String str ="";
                
                if(initial_arffHeader){
                    
                    for(int index = 0; index <inst.numAttributes(); index++){
                        str += inst.attribute(index).name();
                        str += " ";
                    }
                    
                    fw.append(str);
                    fw.append("\n");
                    
                    this.initial_arffHeader = false;
                }
               
                str ="";
                
                for(int index = 0; index <inst.numAttributes()-1; index++){
                    str += (double)inst.value(index);
                    str += " ";
                }
                
                column=inst.numAttributes()-1;
                class_label=inst.toString(column);
                str += class_label;
                //str += " ";
                    
                fw.append(str);
                fw.append("\n");
                fw.flush();
                fw.close();
            
            }else{
            
                FileOutputStream LoadFile = new FileOutputStream(TXTfile);
                OutputStreamWriter WriteOnFilw = new OutputStreamWriter(LoadFile);    
                Writer w = new BufferedWriter(WriteOnFilw);
                
                String str ="";
                if(initial_arffHeader){
                    
                    for(int index = 0; index <inst.numAttributes(); index++){
                        str += inst.attribute(index).name();
                        str += " ";
                    }
                    
                    w.append(str);
                    w.append("\n");
                    
                    this.initial_arffHeader = false;
                }
                
                str ="";
                for(int index = 0; index <inst.numAttributes()-1; index++){
                    str += (double)inst.value(index);
                    str += " ";
                }
                
                column=inst.numAttributes()-1;
                class_label=inst.toString(column);
                str += class_label;
                //str += " ";
                
                w.append(str);
                w.append("\n");
                w.flush();
                w.close();
            
            }
            
        } catch (IOException e) {
            System.err.println("Problem writing to the file");
        }
    }
}
