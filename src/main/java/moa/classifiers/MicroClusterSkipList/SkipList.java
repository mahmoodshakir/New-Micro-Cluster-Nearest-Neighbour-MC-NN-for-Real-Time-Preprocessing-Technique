/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers.MicroClusterSkipList;

/**
 *
 * @author Mahmood Shakir
 * University of Reading
 * 2016
 */
public class SkipList {
    public final long init_longNumber = 9223372036854775807L;
    //public FIFO_LinkedList FIFO = new FIFO_LinkedList();
    long Infinity = init_longNumber;
    public int halfPrevious;
    Node[] headLeft;
    int[] NumberNodes;
    int MaximumHeads;
    int LastInteredHead = 0;
    int MaximumNodes; // Not needed anymore
    public Node MedianQ1nodes;
    public Node MedianQ3nodes;
    public Node MedianQ1nodesUP;
    public Node MedianQ3nodesUP;
    
    private static final double PROBABILITY = 0.5;
    
    public SkipList(int MaxNodes, int MaxHeads){
        restSkipList(MaxNodes,MaxHeads);
    }
    
    public void restSkipList(int MaxNodes, int MaxHeads){
        
        MaximumNodes = MaxNodes;
        MaximumHeads = MaxHeads;
        headLeft = new Node[MaximumHeads];
        NumberNodes = new int[MaximumHeads];
        
        headLeft[0] = new Node(-Infinity,-1);
        Node headRight = new Node(Infinity,-1);
        
        headLeft[0].setHeadLevel(0);
        headRight.setHeadLevel(0);
        
        headLeft[0].setRight(headRight);
        headRight.setLeft(headLeft[0]);
        
        /*
        Setup Median Node which denotes to Middle (1 or 2 nodes)
        */
        Node MedianNode = new Node(0,-1);
        MedianNode.setTail(headRight);
        MedianNode.setUp(headLeft[0]);
        headLeft[0].setDown(MedianNode);
        headRight.setDown(MedianNode);
        
        /*
        Setup Medians Q1 & Q3
        */
        Node MedianQ1 = new Node(0,-1);
        Node MedianQ3 = new Node(0,-1);
        MedianNode.setMedianQ1(MedianQ1);
        MedianNode.setMedianQ3(MedianQ3);
        
        MedianQ1nodes = MedianQ1;
        MedianQ3nodes = MedianQ3;
        
        for(int i=1; i<MaximumHeads; i++){
            
            headLeft[i] = new Node(-Infinity,-1);
            Node headRightN = new Node(Infinity,-1);
            
            headLeft[i].setHeadLevel(i);
            headRightN.setHeadLevel(i);
            
            headLeft[i].setRight(headRightN);
            headRightN.setLeft(headLeft[i]);
            
            headLeft[i].setDown(headLeft[i-1]);
            headRightN.setDown(headLeft[i-1].getRight());
            
            headLeft[i-1].setUp(headLeft[i]);
            headLeft[i-1].getRight().setUp(headRightN);
            
        }
    }
    
    public void addNode(double NodeData, int timestamp){
        /*
        Delete the older node when the size of nodes reaches the threshold
        */
        
        //if(NumberNodes[0] >= MaximumNodes || (int)FIFO.getSize() >= MaximumNodes){ 
        //    
        //    double deleteData = FIFO.getFirstNode();
        //    /*
        //    Check median node if deleted
        //    */
        //    boolean MedianNodeDeleted = false;
        //    Node MedianNode = headLeft[0].getDown();
        //    Node MedianRight = MedianNode.getMedianRight();
        //            
        //    if(deleteData == MedianNode.getMedianRight().getData()){ //|| deleteData == MedianNode.getMedianLeft().getData()
        //        MedianNodeDeleted = true;
        //        
        //        if(NumberNodes[0]%2 == 1){
        //            MedianNode.setMedianRight(MedianRight.getRight());
        //            MedianNode.setMedianLeft(MedianRight.getLeft());
        //        }else{
        //            if(deleteData == MedianNode.getMedianRight().getData()){
        //                MedianNode.setMedianRight(MedianRight.getLeft());//MedianNode.getMedianLeft()
        //                MedianNode.setMedianLeft(null);
        //            }else if(deleteData == MedianNode.getMedianLeft().getData()){
        //                MedianNode.setMedianLeft(null);
        //            }
        //        }
        //    }
        //    
        //    boolean deletedSeccessfully = deleteNode(deleteData);
        //    FIFO.deleteFirstNode();
        //    
        //    while(!deletedSeccessfully){
        //        deleteData = FIFO.getFirstNode();
        //        deletedSeccessfully = deleteNode(deleteData);
        //        FIFO.deleteFirstNode();
        //    }
        //    
        //    FIFO.addNode(NodeData);
        //    
        //    /*
        //    Check Median Node (Deleting)
        //    */
        //    if(!MedianNodeDeleted)
        //        setMedianNode(deleteData, true);
        //}else
        //    FIFO.addNode(NodeData);
        
        Node newNode = new Node(NodeData,timestamp);
        Node currentNode = nearestNode(NodeData, LastInteredHead); 
        Node tempRightNode = currentNode.getRight(); 
        
        currentNode.setRight(newNode);
        newNode.setLeft(currentNode);
        newNode.setRight(tempRightNode);
        tempRightNode.setLeft(newNode);
        
        newNode.setHeadLevel(0);
        NumberNodes[0]++;
        
        // set up levels
        
        int TopLevel = 0; 
	while (Math.random() < PROBABILITY & TopLevel < MaximumHeads-1)
		TopLevel++;
        
        if(TopLevel > LastInteredHead)
            LastInteredHead = TopLevel;
        
        if(TopLevel > 0){
            Node DownNode = newNode;
            
            for(int i = 1; i < TopLevel+1; i++ ){
                Node newUpNode = new Node(NodeData,timestamp);
                
                if(NumberNodes[i] == 0){

                    Node headRight = headLeft[i].getRight();
                    headLeft[i].setRight(newUpNode);
                    
                    headRight.setLeft(newUpNode);
                    DownNode.setUp(newUpNode);
                    
                    newUpNode.setDown(DownNode);
                    newUpNode.setLeft(headLeft[i]);
                    newUpNode.setRight(headRight);
                        
                }else{
                    Node currentN = headLeft[i];
                    while(NodeData > currentN.getRight().getData())
                        currentN = currentN.getRight();
                    
                    tempRightNode = currentN.getRight(); 
                    
                    currentN.setRight(newUpNode);
                    newUpNode.setLeft(currentN);
                    
                    tempRightNode.setLeft(newUpNode);
                    newUpNode.setRight(tempRightNode);
                    
                    DownNode.setUp(newUpNode);
                    newUpNode.setDown(DownNode);
                    
                }
                
                DownNode = newUpNode;
                NumberNodes[i]++;
            }
        }
        
        /*
        Setup Median Node (Inserting)
        */
        //setMedianNode(NodeData,false);
        
        resetMediansQ1Q3(NodeData);
        
    }
    
    public boolean deleteNode(double data){
        
        int headLevel = 0;
        Node nNodeDelete = nearestNode(data, LastInteredHead); 
        if(data == nNodeDelete.getRight().getData()){
            nNodeDelete = nNodeDelete.getRight();
            
            while(nNodeDelete != null){
                NumberNodes[headLevel]--;
                headLevel++;
            
                Node nNodeDelete_Right = nNodeDelete.getRight();
                Node nNodeDelete_Left = nNodeDelete.getLeft();
                nNodeDelete = nNodeDelete.getUp();
        
                nNodeDelete_Right.setLeft(nNodeDelete_Left);
                nNodeDelete_Left.setRight(nNodeDelete_Right);   
            }
            
            return true;
        }else
            return false;
        
    }
    
    public void setMedianNode(double data, boolean Deleting){
        Node MedianNode = headLeft[0].getDown();
        //Node MedianNodeLeft = MedianNode.getMedianLeft();
        Node MedianNodeRight = MedianNode.getMedianRight();
        
        if(NumberNodes[0] == 1){
            MedianNode.setMedianRight(headLeft[0].getRight());
           
            //resetMediansQ1Q3(MedianNode,data);
            halfPrevious = (int)NumberNodes[0]/2;
            
        }else if(NumberNodes[0] == 2){
            MedianNode.setMedianLeft(headLeft[0].getRight());
            MedianNode.setMedianRight(headLeft[0].getRight().getRight());
           
            //resetMediansQ1Q3(MedianNode,data);
            halfPrevious = (int)NumberNodes[0]/2;
            
        }else if(NumberNodes[0] == 3){
            MedianNode.setMedianLeft(null);
            MedianNode.setMedianRight(headLeft[0].getRight().getRight());
            
            //resetMediansQ1Q3(MedianNode,data);
            halfPrevious = (int)NumberNodes[0]/2;
            
        }else if(!Deleting){
            //if(checkInfinity(MedianNodeRight.getData())){
            //    restSkipList(MaximumNodes, MaximumHeads);
            //    return;
            //}
            /*
            Reset Median after Inserting
            */
            if (NumberNodes[0]%2 == 1) {
                // Odd
                // Keep one and leave one
                // Data > Right , Left = null
                // Data <= Right , Left = null and Right = Right.getLeft()
                if(data > MedianNodeRight.getData()){
                    MedianNode.setMedianLeft(null);
                    
                }else if(data <= MedianNodeRight.getData()){
                    MedianNode.setMedianRight(MedianNodeRight.getLeft());
                    MedianNode.setMedianLeft(null);
                   
                }
            }else{
                // Even
                // Keep 2 nodes
                // Data > Right , Left = Right , Right = Right.getRight
                // Data <= Right , Left = Right.getLeft() 
                
                if(data > MedianNodeRight.getData()){
                    MedianNode.setMedianLeft(MedianNode.getMedianRight());//MedianNodeRight
                    MedianNode.setMedianRight(MedianNodeRight.getRight());
                }else if(data <= MedianNodeRight.getData()){
                    MedianNode.setMedianLeft(MedianNodeRight.getLeft());
                }
            }
            
            //resetMediansQ1Q3(MedianNode,data);
            halfPrevious = (int)NumberNodes[0]/2;
        }else{
            //if(checkInfinity(MedianNodeRight.getData())){
            //    restSkipList(MaximumNodes, MaximumHeads);
            //    return;
            //}
            /*
            Reset Median after Deleting, opposite way with Inserting
            */
            if (NumberNodes[0]%2 == 1) {
                // Odd
                // Keep one and leave one
                // Data > Right , Left = null
                // Data <= Right , Left = null and Right = Right.getLeft()
                if(data > MedianNodeRight.getData()){
                    MedianNode.setMedianRight(MedianNodeRight.getLeft()); //MedianNodeRight
                    MedianNode.setMedianLeft(null);
                }else if(data <= MedianNodeRight.getData()){
                    MedianNode.setMedianLeft(null);
                }
            }else{
                // Even
                // Keep 2 nodes
                // Data > Right , Left = Right , Right = Right.getRight
                // Data <= Right , Left = Right.getLeft() 
                if(data > MedianNodeRight.getData()){
                    MedianNode.setMedianLeft(MedianNodeRight.getLeft());
                }else if(data <= MedianNodeRight.getData()){
                    MedianNode.setMedianLeft(MedianNode.getMedianRight());//MedianNodeRight
                    MedianNode.setMedianRight(MedianNodeRight.getRight());
                }
            }
        }
    }
    
    public void resetMediansQ1Q3(double data){
        if (NumberNodes[0] == 1) {
            MedianQ1nodes.setUp(headLeft[0].getRight());
            MedianQ3nodes.setUp(headLeft[0].getRight());
        }else if (NumberNodes[0] == 2) {
            MedianQ1nodes.setUp(headLeft[0].getRight());
            MedianQ3nodes.setUp(headLeft[0].getRight().getRight());
        }else if (NumberNodes[0] == 3 || NumberNodes[0] == 4) {
            MedianQ1nodes.setUp(headLeft[0].getRight());
            MedianQ3nodes.setUp(headLeft[0].getRight().getRight().getRight());
        }else{
            MedianQ1nodesUP = MedianQ1nodes.getUp();
            MedianQ3nodesUP = MedianQ3nodes.getUp();
            double Q1= MedianQ1nodesUP.getData();
            double Q3= MedianQ3nodesUP.getData();
        
            /*
            Print
            */
            //if(data == Q1)
            //    System.out.println(" Data = Q1 "+ Q1 + " "+ data);
            //if(data == Q3)
            //    System.out.println(" Data = Q3 "+ Q3 + " "+ data);
            
            if (NumberNodes[0]%2 != 1) {
                /*
                Reset Medians on even
                */
                int half = (int)NumberNodes[0]/2;
                
                if(half%2 == 1){
                    if(data >= Q3){ //New with =
                        MedianQ1nodes.setUp(MedianQ1nodesUP.getRight());
                        MedianQ3nodes.setUp(MedianQ3nodesUP.getRight());
                    }else if(data > Q1 & data < Q3){
                        MedianQ1nodes.setUp(MedianQ1nodesUP.getRight());
                    }
                }else{
                    if(data > Q1 & data <= Q3){ // New with =
                        MedianQ3nodes.setUp(MedianQ3nodesUP.getLeft());
                    }else if(data <= Q1){ // New with =
                        MedianQ1nodes.setUp(MedianQ1nodesUP.getLeft());
                        MedianQ3nodes.setUp(MedianQ3nodesUP.getLeft());
                    }
                }
            }else{
                /*
                Reset Medians on odd
                */
                if(data == Q3) //New
                    MedianQ3nodes.setUp(MedianQ3nodesUP.getLeft());
                else if(data > Q3)
                    MedianQ3nodes.setUp(MedianQ3nodesUP.getRight());
                else if(data <= Q1) // New with =
                    MedianQ1nodes.setUp(MedianQ1nodesUP.getLeft());
            }
        }
    }
    
    public double getMedianNodeValue(){
        if(NumberNodes[0]==0)
            return 0;
        
        Node MedianN = headLeft[0].getDown();
        if (NumberNodes[0]%2 == 1) {
            return (double) MedianN.getMedianRight().getData();
        }else{
            return (double) (MedianN.getMedianRight().getData() + MedianN.getMedianLeft().getData()) / 2;
        }
        
        //if (NumberNodes[0]%2 == 1) {
        //    if(checkInfinity(MedianN.getMedianRight().getData())){
        //        restSkipList(MaximumNodes,MaximumHeads);
        //        return 0;
        //    }else
        //        return (double) MedianN.getMedianRight().getData();
        //}else{
        //    if(checkInfinity(MedianN.getMedianRight().getData()) || checkInfinity(MedianN.getMedianLeft().getData())){
        //        restSkipList(MaximumNodes,MaximumHeads);
        //        return 0;
        //    }else
        //        return (double) (MedianN.getMedianRight().getData() + MedianN.getMedianLeft().getData()) / 2;
        //}
    }
    
    public boolean checkInfinity(double dataN){
        if(dataN == -init_longNumber || dataN == init_longNumber)
            return true;
        else
            return false;
    }
    
    public void resetTail(){
        //For lower boundary
        int headLevel = 0;
        Node Tail = headLeft[headLevel].getDown().getTail();
        Node upLevelTail = headLeft[headLevel].getDown().getTail();
        Node LeftMedianN = headLeft[headLevel].getDown().getMedianLeft();
        Node RightMedianN = headLeft[headLevel].getDown().getMedianRight();
        Node node;
        
        if (NumberNodes[headLevel]%2 == 1){
            RightMedianN.getLeft().setRight(Tail);
            Tail.setLeft(RightMedianN.getLeft());
            node = RightMedianN.getLeft();
            
        }else{
            LeftMedianN.setRight(Tail);
            Tail.setLeft(LeftMedianN);
            node = headLeft[headLevel].getDown().getMedianLeft();
        }
        
        // reset the rest of the heads
        while(headLevel <= LastInteredHead){
            if(node.getUp() == null){
                node = node.getLeft();
            }else{
                upLevelTail = upLevelTail.getUp();
                node = node.getUp();
                node.setRight(upLevelTail);
                upLevelTail.setLeft(node);
                headLevel++;
            }
        }
            
        NumberNodes[0] = (int) NumberNodes[0]/2;
        System.gc();
    }
    
    public void resetHead(){
        //For upper boundary
        int headLevel = 0;
        Node RightMedianN = headLeft[headLevel].getDown().getMedianRight();
        Node node;
        
        if (NumberNodes[headLevel]%2 == 1){
            RightMedianN.getRight().setLeft(headLeft[headLevel]);
            headLeft[headLevel].setRight(RightMedianN.getRight());
            node = RightMedianN.getRight();
        }else{
            RightMedianN.setLeft(headLeft[headLevel]);
            headLeft[headLevel].setRight(RightMedianN);
            node = headLeft[headLevel].getDown().getMedianRight();
        }
        
        // reset the rest of the heads
        while(headLevel <= LastInteredHead){
            if(node.getUp() == null){
                node = node.getRight();
            }else{
                headLevel++;
                node = node.getUp();
                node.setLeft(headLeft[headLevel]);
                headLeft[headLevel].setRight(node);
            }
        }
            
        NumberNodes[0] = (int) NumberNodes[0]/2;
        System.gc();
    }
    
    public double[] getMedianQ1Q3(){
        Node MedianNode = headLeft[0].getDown();
        Node MedianQ1 = MedianNode.getMedianQ1();
        Node MedianQ3 = MedianNode.getMedianQ3();
        
        double[] medians = new double[2];
        int half = NumberNodes[0]/2;
        
        if(half%2 == 1){
            medians[0] = (double)MedianQ1.getUp().getData();
            medians[1] = (double)MedianQ3.getUp().getData();
        }else{
            medians[0] = (double)(MedianQ1.getUp().getData()+MedianQ1.getUp().getRight().getData())/2;
            medians[1] = (double)(MedianQ3.getUp().getData()+MedianQ3.getUp().getRight().getData())/2;
        }
        
        return (double[])medians;
    }
    
    public Node[] getLowerUpperMedian(){
        Node MedianNodeRight = headLeft[0].getDown().getMedianRight();
        Node MedianNodeLeft = headLeft[0].getDown().getMedianLeft();
        Node MoveRight;
        Node MoveLeft;
        
        Node[] LowerUpperMedian;
        int sizeMiddle = (int)NumberNodes[0]/2;
        int sizeQuartile = (int)NumberNodes[0]/4;
        
        if (NumberNodes[0]%2 == 1){
            MoveRight = MedianNodeRight.getRight();
            MoveLeft = MedianNodeRight.getLeft();
        }else{
            MoveRight = MedianNodeRight;
            MoveLeft = MedianNodeLeft;
        }
        
        int Counter = 1;
        if(NumberNodes[0]>3){
            while(Counter < sizeQuartile){
                MoveRight = MoveRight.getRight();
                MoveLeft = MoveLeft.getLeft();
                Counter++;
            }
        }
       
        if (sizeMiddle%2 == 1){
            LowerUpperMedian = new Node[2]; // 0 lower 1 upper median
            
            if(NumberNodes[0] == 2 || NumberNodes[0] == 3){
                LowerUpperMedian[0] = MoveLeft;
                LowerUpperMedian[1] = MoveRight;
            }else{
                LowerUpperMedian[0] = MoveLeft.getLeft();
                LowerUpperMedian[1] = MoveRight.getRight();
            }
            
        }else{
            LowerUpperMedian = new Node[4]; // 0(Left) 1(ight) lower 2(Left) 3(Right) upper median
            
            LowerUpperMedian[0] = MoveLeft.getLeft();
            LowerUpperMedian[1] = MoveLeft;
            LowerUpperMedian[2] = MoveRight;
            LowerUpperMedian[3] = MoveRight.getRight();
        }
        
        return (Node[])LowerUpperMedian;
    }
    
    public double[] quartileLowerUpperMedian(){
        Node[] LowerUpperMedian = getLowerUpperMedian();
        double[] Quartiles;
        if(LowerUpperMedian.length==2){
            Quartiles = new double[2];
            Quartiles[0] = (double)LowerUpperMedian[0].getData(); // Q1 Lower Median
            Quartiles[1] = (double)LowerUpperMedian[1].getData(); // Q3 upper Median
        }else{
            Quartiles = new double[2];
            Quartiles[0] = (double)(LowerUpperMedian[0].getData()+LowerUpperMedian[1].getData())/2; // Q1 Lower Median
            Quartiles[1] = (double)(LowerUpperMedian[2].getData()+LowerUpperMedian[3].getData())/2; // Q1 Lower Median
        }
        return (double[])Quartiles; 
    }
    
    public void printLowerUpperMedian(){
        Node[] LowerUpperMedian = getLowerUpperMedian();
        if(LowerUpperMedian.length==2){
            System.out.println("Lower Median: "+ LowerUpperMedian[0].getData()+" Upper Median: "+LowerUpperMedian[1].getData());
        }else{
            System.out.println("Lower Median Left: "+ LowerUpperMedian[0].getData()+" Lower Median Right: "+LowerUpperMedian[1].getData());
            System.out.println("Upper Median Left: "+ LowerUpperMedian[2].getData()+" Upper Median Right: "+LowerUpperMedian[3].getData());
        }
    }
    
    public void resetLowerMedian(Node[] LowerUpperMedian){
        Node MedianNodeRight = headLeft[0].getDown();
        if(LowerUpperMedian.length==2){
            MedianNodeRight.setMedianRight(LowerUpperMedian[0]);
        }else{
            MedianNodeRight.setMedianLeft(LowerUpperMedian[0]);
            MedianNodeRight.setMedianRight(LowerUpperMedian[1]);
        }
    }
    
    public void resetUpperMedian(Node[] LowerUpperMedian){
        Node MedianNodeRight = headLeft[0].getDown();
        if(LowerUpperMedian.length==2){
            MedianNodeRight.setMedianRight(LowerUpperMedian[1]);
        }else{
            MedianNodeRight.setMedianLeft(LowerUpperMedian[2]);
            MedianNodeRight.setMedianRight(LowerUpperMedian[3]);
        }
    }
    
    public int getNumberOutliers(double MaxRang, double MinRang){
        int numOutliers = 0;
        Node MoveRight = headLeft[0].getRight();
        Node MoveLeft = headLeft[0].getDown().getTail().getLeft();
            
        while(MoveLeft.getData() > MaxRang){
            numOutliers++;
            MoveLeft = MoveLeft.getLeft();
        }
            
        while(MoveRight.getData() < MinRang){
            numOutliers++;
            MoveRight = MoveRight.getRight();
        }
        
        return numOutliers;
    }
    
    public boolean checkOutliers(double MaxRang, double MinRang, double maxvalue, double minvalue){
        //double MinValue = headLeft[0].getRight().getData();
        //double MaxValue = headLeft[0].getDown().getTail().getLeft().getData();
        
        if(minvalue < MinRang || maxvalue > MaxRang)
            return true;
        else
            return false;
    }
    
    public double getMinValue(){
        return (double)headLeft[0].getRight().getData();
    }
    
    public double getMaxValue(){
        return (double)headLeft[0].getDown().getTail().getLeft().getData();
    }
    
    /*
    Check older node timestamp
    */
    //public void checkNodeTimeStamp(Node current){
    //    if(current.getTimeStamp() < OlderNode_timeStamp & current.getTimeStamp()>=0){
    //        OlderNode_timeStamp = (int)current.getTimeStamp();
    //        OlderNode_value = (double)current.getData();
    //    }
    //}
    
    /*
    Interquartile Range (IQR)
    */
    public double[] getIQR(double[] FeatureSortedNodes){
        double Q1,Q2,Q3,IQR,MinRange, MaxRange;
        double[] infoIQR = new double[6];
        /*
        infoIQR[0] = Median Q1
        infoIQR[1] = Median Q2
        infoIQR[2] = Median Q3
        infoIQR[3] = MinRange
        infoIQR[4] = MaxRange
        infoIQR[5] = IQR
        */
        
        int middle = FeatureSortedNodes.length/2;
        double[] ArrayQ1 = new double[middle];
        double[] ArrayQ3 = new double[middle];
        
        Q2 = (double)getMedian(FeatureSortedNodes);
        System.arraycopy(FeatureSortedNodes, 0, ArrayQ1, 0, middle);
        
        if (FeatureSortedNodes.length%2 == 1) {
            System.arraycopy(FeatureSortedNodes, middle+1, ArrayQ3, 0, middle);
        }else{
            System.arraycopy(FeatureSortedNodes, middle, ArrayQ3, 0, middle);
        }
        
        Q1=(double)getMedian(ArrayQ1);
        Q3=(double)getMedian(ArrayQ3);
        IQR = (double) Q3 - Q1;
        MinRange = (double) Q1 - (1.5 * IQR);
        MaxRange = (double) Q3 + (1.5 * IQR);
        //for(int i=0; i<ArrayQ1.length; i++)
        //    System.out.println(ArrayQ1[i]+" ");
        //System.out.println("Q1:"+Q1+"  Q2:"+ Q2+"  Q3:"+Q3+"  IQR:"+IQR+"  MinRange:"+MinRange+"  MaxRange:"+MaxRange );
        
        infoIQR[0] = Q1;
        infoIQR[1] = Q2;
        infoIQR[2] = Q3;
        infoIQR[3] = MinRange;
        infoIQR[4] = MaxRange;
        infoIQR[5] = IQR;

        return (double[])infoIQR;
    }
    
    public double getMedian(double[] array){
        int middle = array.length/2;
        if (array.length%2 == 1) {
            return (double)array[middle];
        } else {
            return (double)(array[middle-1] + array[middle]) / 2.0;
        }   
    }
    
    public double getMedian(){
        int size = getNumberNodesHead(0);
        int middle = size/2;
        
        Node current = headLeft[0].getRight();
        
        switch (size) {
            case 1:  return (double)current.getData();
            case 2:  return (double)(current.getData()+current.getRight().getData())/2;
            case 3:  return (double)current.getRight().getData();
            case 4:  return (double)(current.getRight().getData()+current.getRight().getRight().getData())/2;
            case 5:  return (double)current.getRight().getRight().getData();
            default: 
                int index = 1;
                Node next = current.getRight();
                while(index < middle){
                    if(index+15 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 15;
                        //System.out.println("Case 15");
                    }else if(index+14 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 14;
                        //System.out.println("Case 14");
                    }else if(index+13 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 13;
                        //System.out.println("Case 13");
                    }else if(index+12 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 12;
                        //System.out.println("Case 12");
                    }else if(index+11 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 11;
                        //System.out.println("Case 11");
                    }else if(index+10 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 10;
                        //System.out.println("Case 10");
                    }else if(index+9 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 9;
                        //System.out.println("Case 9");
                    }else if(index+8 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 8;
                        //System.out.println("Case 8");
                    }else if(index+7 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 7;
                        //System.out.println("Case 7");
                    }else if(index+6 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight().getRight();
                        index = index + 6;
                        //System.out.println("Case 6");
                    }else if(index+5 < middle){
                        current = current.getRight().getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight().getRight();
                        index = index + 5;
                        //System.out.println("Case 5");
                    }else if(index+4 < middle){
                        current = current.getRight().getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight().getRight();
                        index = index + 4;
                        //System.out.println("Case 4");
                    }else if(index+3 < middle){
                        current = current.getRight().getRight().getRight();
                        next = next.getRight().getRight().getRight();
                        index = index + 3;
                        //System.out.println("Case 3");
                    }else if(index+2 < middle){
                        current = current.getRight().getRight();
                        next = next.getRight().getRight();
                        index = index + 2;
                        //System.out.println("Case 2");
                    }else{
                        current = current.getRight();
                        next = next.getRight();
                        index++;
                        //System.out.println("Case 1");
                    }
                }
        
                //next = current.getRight();
        
                if (size%2 == 1) {
                    return (double)next.getData();
                } else {
                    return (double)(current.getData() + next.getData()) / 2.0;
                }
        }
        //if(size > 1){
        //    while(index < middle){
        //        
        //        current = current.getRight();
        //        index++;
        //        
        //    }
      //  
        //    Node next = current.getRight();
      /// 
        //    if (size%2 == 1) {
        //        return (double)next.getData();
        //    } else {
        //        return (double)(current.getData() + next.getData()) / 2.0;
        //    }
        //}else
        //    return (double)current.getData();
    }
    
    public void printHeads(){
        System.out.println(" ");
        System.out.println(" ++++++++++ Heads ++++++++++ ");
        
        int i = MaximumHeads-1;
        
        while(i>=0){
            Node headLeftN = headLeft[i];
            System.out.print("Head: " + (i+1) + " Nodes: "); //+ headLeftN.getData()+ " "
            headLeftN = headLeftN.getRight();
            while(headLeftN.getData() != Infinity){
                System.out.print(headLeftN.getData()+" ");
                headLeftN = headLeftN.getRight();
            }
            System.out.println(" ");
            i--;
        }
    }
    
    public void printFirstHead(){
        //System.out.println(" ");
        
        int i = 0;
        
        while(i>=0){
            Node headLeftN = headLeft[i];
            System.out.print("Nodes: "); //+ headLeftN.getData()+ " "
            headLeftN = headLeftN.getRight();
            while(headLeftN.getData() != Infinity){
                System.out.print(headLeftN.getData()+" ");
                headLeftN = headLeftN.getRight();
            }
            //System.out.println(" ");
            i--;
        }
    }
    
    public double[] RetrieveNodes(){
        double[] Nodes = new double[getNumberNodesHead(0)];
        Node current = headLeft[0].getRight();
        
        int index = 0;
        while(current.getData() != Infinity){
            Nodes[index] = (double)current.getData();
            //TimeStampNodes[index] = (int)current.getTimeStamp();
            current = current.getRight();
            index++;
        }
        return (double[])Nodes;
    }

    public int[] RetrieveFeatureIndex(int ratio){ // ratio = 50 or 25%
        
        int numberNodes = (int)NumberNodes[0]/2;
        
        int[] FeatureIndex = new int[numberNodes];
        Node current = headLeft[0].getDown().getMedianRight();
        
        if(NumberNodes[0]%2 == 1) // Median Q2
            current = headLeft[0].getDown().getMedianRight().getRight();
            
        int index = 0;
        while(current.getData() != Infinity){
            FeatureIndex[index] = (int)current.getTimeStamp();
            //TimeStampNodes[index] = (int)current.getTimeStamp();
            current = current.getRight();
            index++;
        }
        return (int[])FeatureIndex;
    }
    
    //public int[] RetrieveTimeStampNodes(){
    //    /*
    //    Feature Index
    //    It can be used for feature selection and normalization
    //    */
    //    return (int[])TimeStampNodes;
    //}
    
    /*
    retrieve the nearest node in the base line level 0
    */
    public Node nearestNode(double data, int toplevel){
        
        Node currentNode = headLeft[toplevel];
        
        while(toplevel > 0) {
            
            Node nRight = currentNode.getRight();
            Node nDown = currentNode.getDown();
        
            /*
            Check older node timestamp
            */
            //checkNodeTimeStamp(nRight);
            
            if(data > nRight.getData()){
                currentNode = nRight;
            }else if(data <= nRight.getData() & toplevel > 0){
                toplevel--;
                currentNode = nDown;
            }
	}
        
        /*
        Matching in the base level
        */
        Node nRight = currentNode.getRight();
        
        /*
        Check older node timestamp
        */
        //checkNodeTimeStamp(nRight);
            
        while(data > nRight.getData()){
            currentNode = nRight;
            nRight = currentNode.getRight();
            
            //checkNodeTimeStamp(nRight);
        }
        
        return currentNode;
    }
    
    public int[] getNumberNodesHeads(){
        return (int[])NumberNodes;
    }
    
    public int getNumberNodesHead(int headLevel){
        return (int)NumberNodes[headLevel];
    }
    
    //Node<Integer> current = linkedList.getHead();
    //    int length = 0;
    //    Node<Integer> middle = linkedList.getHead();
 
 
      //  //Loop until last element is reached
      //  while (current.getNext() != null) {
      //      length++;
      //      if (length % 2 == 0) {
      //          middle = middle.getNext();
      //      }
      //      current = current.getNext();
      //  }
 //
     //   if (length % 2 == 1) {
   //        middle = middle.getNext();
    //   }
 
    //    System.out.println("length of SinglyLinkedList: " + (length+1));
      //  System.out.println("middle element of SinglyLinkedList : " + middle);
 
}
