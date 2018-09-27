# New-Micro-Cluster-Nearest-Neighbour-MC-NN-for-Real-Time-Preprocessing-Technique
1. Download MOA

2. Create new java package:

   MathPackages
   moa.classifiers
   moa.classifiers.MicroClusterSkipList
   moa.classifiers.core.driftdetection
   moa.classifiers.drift
   
3. Add the following java classes into the packages (2):

   (MathPackages) 
       NormalizeAttributes.java
       PercentageDifference.java
       StandardDeviation.java
   (classifiers) 
       MicroCluster.java
       MicroClusterManager.java
       MicroClusterManagerMedian.java
       MicroClusterMedian.java
       MicroClustersSingle.java
       MicroClustersSingleMedian.java
   (MicroClusterSkipList)
       FIFO_LinkedList.java
       Node.java
       SkipList.java
       SkipListManager.java
   (driftdetection)
       ChangeDetector.java
       MicroCluster_FeatureTracker_MCNN_InterquartileRange.java
       MicroCluster_FeatureTracker_MCNN_Variance.java
   (drift)
       DriftDetectionMethodClassifier.java
       
4. Create new java package:

   moa.gui
   
5. Add the following java class into the package (4): 

   GUI.java
   
6. Run MOA:   

   moa.gui.GUI.java
   
7. Click on classification - Configure

8. Choose EvaluatePrequential

9. Learner: Click on Edit and choose SingleClassifierDrift: 

   baseLearner: Choose a classifier (i.e., HoeffdingTree)
   
   driftDetectionMethod: Choose the MCNN
      (old MCNN with Variance) MicroCluster_FeatureTracker_MCNN_Variance.java
      (new MCNN with IQR) MicroCluster_FeatureTracker_MCNN_InterquartileRange
