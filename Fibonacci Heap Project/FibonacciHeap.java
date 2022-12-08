/**
 * Eddy Movshovich & Chen Michaeli
 */
/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap
{
    private HeapNode minNode;
    private int size;
    private int marksCounter;
    private static int cutsCounter;
    private static int linksCounter;
    private static final float GOLDEN_RATIO = (float) 1.62;

   /**
    * public boolean isEmpty()
    *
    * Returns true if and only if the heap is empty.
    * Complexity: O(1).
    */
    public boolean isEmpty() {
    	return this.minNode == null;
    }

   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
    * The added key is assumed not to already belong to the heap.  
    * 
    * Returns the newly created node.
    * Complexity: O(1)
    */
    public HeapNode insert(int key) {
        return insert(key, null);
    }

    private HeapNode insert(int key, HeapNode node){
        HeapNode newNode = new HeapNode(key);
        newNode.kMinPointer = node;
        this.size += 1;

        if (this.isEmpty()){ // Heap is empty.
            DoublyLinkedList nodeList = new DoublyLinkedList();
            nodeList.add(newNode);
            this.minNode = newNode;
            newNode.nodeList = nodeList;
        }
        else{
            //DoublyLinkedList roots = this.minNode.nodeList;
            this.minNode.nodeList.add(newNode);
            //newNode.nodeList = roots;
            if (this.minNode.key > newNode.key){
                this.minNode = newNode;
            }
        }

        return newNode;
    }

    /**
     * public void deleteMin()
     *
     * Deletes the node containing the minimum key, and consolidates the heap.
     * Complexity O(log n amortized), O(n) WC.
     *
     */
    public void deleteMin() {
        if (this.size == 0) return;
        if (this.size == 1) this.minNode = null;
        else {
            deleteMinCut(this.minNode);

            consolidate();

            HeapNode iterNode = this.minNode;
            for (int i=0; i < this.minNode.nodeList.size; i++) {
                if (iterNode.key < this.minNode.key) this.minNode = iterNode;
                iterNode = iterNode.next;
            }
        }
        this.size--;
    }

    // ----- Methods for consolidate -----

    /**
     * private void consolidate()
     * Consolidates all roots by successive linking.
     * Changes this.min.nodeList into new root list.
     * Complexity: WC O(n)
     */
    private void consolidate(){
        HeapNode iterNode = this.minNode.nodeList.first;
        DoublyLinkedList rootList = this.minNode.nodeList;
        DoublyLinkedList consolidatedRootList = new DoublyLinkedList();
        int numOfRoots = this.minNode.nodeList.size;
        HeapNode[] buckets = new HeapNode[(int) (Math.log(this.size)/Math.log(GOLDEN_RATIO)) + 1]; // SIZE OF N????

        // Consolidate roots iteratively (Insert buckets)
        for (int i = 0; i < numOfRoots; i++) {
            // Remove root from rootList
            HeapNode nextIter = iterNode.next;
            rootList.delete(iterNode);

            // Iterative Consolidate
            int rankOfNode = calcRank(iterNode);
            while (buckets[rankOfNode] != null){
                HeapNode secondNode = buckets[rankOfNode];
                iterNode = linkHeapNodes(iterNode, secondNode);
                // Remove node from bucket
                buckets[rankOfNode] = null;
                rankOfNode += 1;
            }
            buckets[rankOfNode] = iterNode;

            iterNode = nextIter;
        }

        // Build new root list.
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != null){
                buckets[i].parent = null;
                consolidatedRootList.add(buckets[i]);
                buckets[i].nodeList = consolidatedRootList;
            }
        }
        this.minNode = consolidatedRootList.first;
    }


    /**
     * private HeapNode linkHeapNodes(HeapNode firstNode, HeapNode secondNode)
     * Link two nodes of the same rank.
     * Returns a root representing a link of both nodes.
     * Complexity: O(1)
     */
    private HeapNode linkHeapNodes(HeapNode firstNode, HeapNode secondNode){
        linksCounter++;
        // Find parent and son
        HeapNode bigger = firstNode.key > secondNode.key ? firstNode : secondNode;
        HeapNode smaller = firstNode.key < secondNode.key ? firstNode : secondNode;
        if (smaller.child != null) { // Smaller already has children
            DoublyLinkedList smallerChildren = smaller.child.nodeList;
            smallerChildren.add(bigger);
        }
        else{ // Smaller has no children, make a child list for bigger
              //and append it as a son of smaller
            smaller.child = bigger;
            bigger.nodeList = new DoublyLinkedList();
            bigger.nodeList.add(bigger);
        }
        bigger.parent = smaller;
        return smaller;
    }

    /**
     * public int calcRank(HeapNode node)
     * Calculate the num of children a node has and returns it.
     * Complexity: O(1)
     */
    private int calcRank(HeapNode node){
        if (node.child != null){
            return node.child.nodeList.size;
        }
        return 0;
    }

    // ----- End of consolidate -----

    // ----- Cut methods -----

    /**
     * private void cascadingCut(HeapNode node)
     * Recursive cut or mark parent. Used for decreaseKey.
     * Complexity: O(log n)
     */
    private void cascadingCut(HeapNode node) {
        cutsCounter++;
        HeapNode parent = node.parent;

        if (node.nodeList.size == 1) { // Only brother in parent's subtree.
            node.parent.child = null;
        } else { // Node has brothers.
            node.parent.child = node.next;
        }
        node.nodeList.delete(node);
        node.parent = null;
        this.minNode.nodeList.add(node);
        if (node.mark) marksCounter--; // Unmark node
        node.mark = false;

        if (parent.mark) {
            cascadingCut(parent);
        }
        else {
            if (parent.parent != null) // Parent is not a root.
            {
                parent.mark = true;
                this.marksCounter++;
            }
        }
    }
    /**
     * private void cascadingCut(HeapNode node)
     * Append min children to the roots list and delete minNode.
     * Complexity: O(log n)
     */
    private void deleteMinCut(HeapNode node){
        if (node.nodeList.size == 1){ // Node is alone in the root list.
            this.minNode = node.child;
            if (this.minNode != null){ // If node had a child.
                //cutsCounter++;
                this.minNode.parent = null;
            }
        }
        else{
            HeapNode newPointer = node.next;
            if (node.child != null) {
                // Insert node's children right to node.
                HeapNode left = node;
                HeapNode rightFromNode = node.next;
                node.nodeList.concatenateBetween(left, rightFromNode, node.child.nodeList, this);
                node.child = null;
            }
            // Detach node from heap.
            node.nodeList.delete(node);
            this.minNode = newPointer;
        }
    }

    // ----- End of Cut methods -----

    /**
    * public HeapNode findMin()
    *
    * Returns the node of the heap whose key is minimal, or null if the heap is empty.
    * Complexity: O(1)
    *
    */
    public HeapNode findMin() {
    	return this.minNode;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Melds heap2 with the current heap.
    * Complexity O(1)
    */
    public void meld (FibonacciHeap heap2) {
    	  if (this.isEmpty()){
    	      this.minNode = heap2.minNode;
          }
    	  else if (!heap2.isEmpty()){
    	      this.minNode.nodeList.concatenate(heap2.minNode.nodeList);
          }
    	  this.size += heap2.size();
    	  this.marksCounter += heap2.marksCounter;
    }

   /**
    * public int size()
    *
    * Returns the number of elements in the heap.
    * Complexity: O(1).
    */
    public int size() {
    	return this.size;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
    * Note: The size of of the array depends on the maximum order of a tree, and an empty heap returns an empty array.
    * Complexity: WC O(n)
    */
    public int[] countersRep() {
    	if (this.minNode == null) return new int[0];
    	int maxRank = findMaxRank();
    	int[] counterRep  = new int[maxRank + 1];
    	HeapNode iterNode = this.minNode.nodeList.first;
    	int numOfRoots = this.minNode.nodeList.size;
        for (int i = 0; i < numOfRoots; i++) {
            counterRep[calcRank(iterNode)]++;
            iterNode = iterNode.next;
        }
        return counterRep;
    }

    /**
     * private int findMaxRank()
     * Find root with the highest rank.
     * Complexity: WC O(n)
     */
    private int findMaxRank(){
        int maxRank = 0;
        HeapNode iterNode = this.minNode;
        for (int i = 0; i < this.minNode.nodeList.size; i++) {
            maxRank = Math.max(calcRank(iterNode), maxRank);
            iterNode = iterNode.next;
        }
        return maxRank;
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap.
	* It is assumed that x indeed belongs to the heap.
    * Complexity: O(1)
    */
    public void delete(HeapNode x) {
        // Decrease Key by "infinite"
        decreaseKey(x, Math.abs(x.key) + Math.abs(this.minNode.key) + 1);
        this.minNode = x;
    	deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     *
     * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     * Complexity O(1).
     */
    public void decreaseKey(HeapNode x, int delta) {
        if (x.parent == null) x.key -= delta; // node is root, doesn't harm heap invariants -> no cut
        else {
            x.key -= delta;
            if (x.key < x.parent.key) cascadingCut(x); // heap order violation -> cascading cut
        }
        if (x.key < this.minNode.key) this.minNode = x;
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * 
    * In words: The potential equals to the number of trees in the heap
    * plus twice the number of marked nodes in the heap.
    * Complexity: O(1).
    */
    public int potential() {
    	return this.minNode.nodeList.size + 2*this.marksCounter;
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the
    * run-time of the program. A link operation is the operation which gets as input two
    * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
    * tree which has larger value in its root under the other tree.
    * Complexity: O(1)
    */
    public static int totalLinks() {
    	return linksCounter;
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the
    * run-time of the program. A cut operation is the operation which disconnects a subtree
    * from its parent (during decreaseKey/delete methods).
    * Complexity: O(1)
    */
    public static int totalCuts() {
    	return cutsCounter;
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     *
     * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
     * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
     *
     * ###CRITICAL### : you are NOT allowed to change H.
     * Complexity: O(k*deg(H))
     */
    public static int[] kMin(FibonacciHeap H, int k) {
        int[] arr = new int[k];
        FibonacciHeap helperHeap = new FibonacciHeap(); // Create a helper heap so original one won't change.
        helperHeap.insert(H.minNode.key, H.minNode);
        helperHeap.minNode.kMinPointer = H.minNode;

        for (int i=0; i<k; i++) {
            HeapNode minNode = helperHeap.minNode;
            arr[i] = minNode.key;

            if (minNode.kMinPointer.child != null) { // before, check if there is a child
                DoublyLinkedList sonsList = minNode.kMinPointer.child.nodeList;
                HeapNode iterNode = sonsList.first;
                for (int j = 0; j < sonsList.size; j++) {
                    helperHeap.insert(iterNode.key, iterNode);
                    iterNode = iterNode.next;
                }
            }

            helperHeap.deleteMin();
        }
        return arr;
    }
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in another file. 
    *  
    */
    public static class HeapNode {

       private HeapNode next;
       private HeapNode prev;
       private HeapNode parent;
       private int key;
       private HeapNode child;
       private int rank;
       private boolean mark;
       private DoublyLinkedList nodeList;
       private HeapNode kMinPointer; // Used for kMin

       public HeapNode(int key){
           this.key = key;
       }

       public int getKey(){
           return this.key;
       }
   }

    public static class DoublyLinkedList {
        private HeapNode first;
        private int size;

        public DoublyLinkedList() {
            this.first = null;
            this.size = 0;
        }

        public boolean isEmpty(){
            return this.size == 0;
        }

        /**
         * public void add(HeapNode node)
         *
         * Method for adding a node to the DLL
         * Complexity: O(1)
         */
        public void add(HeapNode node) { // CHANGE 16:41
            if (this.size == 0) { // List is empty, add first node.
                this.first = node;
                node.next = node;
                node.prev = node;
            }
            else { // Link new node to list.
                HeapNode last = this.first.prev;
                last.next = node;
                node.next = this.first;
                node.prev = last;
                this.first.prev = node;
                this.first = node;
            }
            node.nodeList = this;
            this.size++;
        }

        /**
         * public void delete(HeapNode node)
         *
         * Method for deleting node from DLL
         * Complexity: O(1)
         */
        public void delete(HeapNode node){
            if (this.size == 1){
                this.size--;
                this.first = null;
            }
            else{
                this.size--;
                HeapNode prevNode = node.prev;
                prevNode.next = node.next;
                node.next.prev = prevNode;

                if (node == this.first){
                    this.first = node.next;
                }

                node.prev = node;
                node.next = node;
            }
            node.nodeList = null;
        }

        /**
         * public void concatenate(DoublyLinkedList otherDLL)
         *
         * Method for concatenating two DLL
         * Complexity: O(1)
         */
        private void concatenate(DoublyLinkedList otherDLL){
            this.size += otherDLL.size;

            if (otherDLL.isEmpty()){
            }
            else if(this.isEmpty()){
                this.first = otherDLL.first;
            }
            else { // Both lists aren't empty
                HeapNode lastNodeHeapOne = this.first.prev;
                lastNodeHeapOne.next = otherDLL.first;
                HeapNode lastNodeHeapTwo = otherDLL.first.prev;
                otherDLL.first.prev = lastNodeHeapOne;
                lastNodeHeapTwo.next = this.first;
                this.first.prev = lastNodeHeapTwo;
            }
        }

        /**
         * private void concatenateBetween(HeapNode left, HeapNode right, DoublyLinkedList innerList, FibonacciHeap heap)
         * Inserts a list in the middle of another one according to the pointers provided. Used for deleteMin only.
         * Makes node.parent = null for every node in innerList.
         * Complexity: O(size(innerList))
         */
        private void concatenateBetween(HeapNode left, HeapNode right, DoublyLinkedList innerList, FibonacciHeap heap){
            // Change inner list parents
            HeapNode iterNode = innerList.first;
            for (int i = 0; i < innerList.size; i++) {
                if (iterNode.mark) heap.marksCounter--;
                iterNode.mark = false;
                iterNode.parent = null;
                iterNode.nodeList = this;
                iterNode = iterNode.next;
            }
            // Increase roots list size
            this.size += innerList.size;
            HeapNode leftMostChild = innerList.first;
            HeapNode rightMostChild = innerList.first.prev;
            left.next = leftMostChild;
            leftMostChild.prev = left;
            right.prev = rightMostChild;
            rightMostChild.next = right;
        }
    }
}


