/**
 *
 * AVLTree
 *
 * An implementation of aמ AVL Tree with
 * distinct integer keys and info.
 *
 */


public class AVLTree {

	private AVLNode root;
	private AVLNode minNode;
	private AVLNode maxNode;
	private int size;

	// Used in keyToArray and valueToArray methods
	private int[] inOrderKeyArray;
	private String[] inOrderValueArray;

	// Virtual Node
	private final AVLNode VIRTUAL_NODE = new AVLNode();

	/**
	 * public boolean empty()
	 * <p>
	 * Returns true if and only if the tree is empty.
	 * Complexity: O(1)
	 */
	public boolean empty() {
		return this.size == 0;
	}

	/**
	 * public String search(int k)
	 * <p>
	 * Returns the info of an item with key k if it exists in the tree.
	 * otherwise, returns null.
	 * Uses method recSearch.
	 * Complexity: O(log n)
	 * </p>
	 */
	public String search(int k) {
		if (k <= 0) return null;
		return recSearch(k, this.root);
	}

	/**
	 * private String recSearch(int k, IAVLNode node)
	 * <p>
	 * Recursive sub function for a binary search in a BST.
	 * Returns the info of an item with key k if it exists in the BST.
	 * otherwise, returns null.
	 * Supports search method.
	 * Complexity: O(log n)
	 * </p>
	 */
	private String recSearch(int k, IAVLNode node){
		if (node == null) return null;
		if (node.getKey() == k) return node.getValue();
		if (node.getKey() > k) return recSearch(k, node.getLeft());
		else return recSearch(k, node.getRight());
	}

	// ---------- Deletion methods  ----------

	/**
	 * public int delete(int k)
	 * <p>
	 * Deletes an item with key k from the binary tree, if it is there.
	 * The tree must remain valid, i.e. keep its invariants.
	 * Returns the number of re-balancing operations, or 0 if no re-balancing operations were necessary.
	 * A promotion/rotation counts as one re-balance operation, double-rotation is counted as 2.
	 * Returns -1 if an item with key k was not found in the tree.
	 * Calls deleteRebalance iterative method.
	 * Complexity: O(log n)
	 * </p>
	 */
	public int delete(int k) {

		AVLNode node = searchNode(k);
		if (node == null) return -1; // K is not part of the tree.
		this.size -= 1; // Decrease size of the tree (K exists and will be deleted).

		// Update min:
		if (this.minNode.getKey() == k){
			this.minNode = getSuccessor(node);
		}
		// Update max:
		if (this.maxNode.getKey() == k) {
			this.maxNode = getPredecessor(node);
		}

		// Check if tree is empty:
		if (this.empty()){
			this.root = null;
			this.minNode = null;
			this.maxNode = null;
			return 0;
		}

		int numOfSons = getNumOfSons(node);
		AVLNode parent = (AVLNode) node.getParent();

		if (numOfSons == 0){
			removeLeaf(node);
		}
		else if (numOfSons == 1){
			removeUnary(node);
		}
		else { // Node is binary.
			AVLNode successor = getSuccessor(node);
			AVLNode parentOfSuccessor = null;
			if (node.getRight() != successor) { // Init successor's parent.
				parentOfSuccessor = (AVLNode) successor.getParent();
			}
			else{
				parentOfSuccessor = (AVLNode) node.getParent();
			}
			// Detach successor and swaps node <-> successor.
			removeBinaryNode(node, successor);
			parent = parentOfSuccessor;

			while (parentOfSuccessor != null){ // Updates node sizes up the tree. (O(log n)).
				updateSize(parentOfSuccessor);
				parentOfSuccessor = (AVLNode) parentOfSuccessor.getParent();
			}
		}

		return deleteRebalance(parent);
	}

	/**
	 * private AVLNode searchNode(int k)
	 * <p>
	 * Performs a binary search in the BST,
	 * returns the pointer to the node with node.key = k, or null if not found.
	 * Calls recSearchNode method.
	 * Complexity: O(log n)
	 * </p>
	 */
	private AVLNode searchNode(int k) {
		if (k < 0) return null;
		return recSearchNode(k, this.root);
	}

	/**
	 * private AVLNode recSearchNode(int k, IAVLNode node)
	 * <p>
	 * Recursive sub function for a binary search in a BST.
	 * Returns the pointer to the node with key k if it exists in the BST.
	 * otherwise, returns null.
	 * Supports searchNode method.
	 * Complexity: O(log n)
	 * </p>
	 */
	private AVLNode recSearchNode(int k, IAVLNode node){
		if (node == null) return null;
		if (node.getKey() == k) return (AVLNode) node;
		if (node.getKey() > k) return recSearchNode(k, node.getLeft());
		else return recSearchNode(k, node.getRight());
	}

	/**
	 * private AVLNode getSuccessor(AVLNode node)
	 * <p>
	 * Locates the successor of the node.
	 * Returns a pointer to the successor if exists,
	 * otherwise returns null.
	 * Supports delete method (when deleting a binary node).
	 * Complexity: O(log n)
	 * </p>
	 */
	private AVLNode getSuccessor(AVLNode node){
		if (!node.getRight().isRealNode()){ // Successor is not in node's right subtree.
			AVLNode nodeSuccessor = (AVLNode) node;
			while (node.getParent() != null){ // Finds node's first ancestor with a bigger key.
				if (!isRightSon(nodeSuccessor)){
					return (AVLNode) nodeSuccessor.getParent();
				}
				nodeSuccessor = (AVLNode) nodeSuccessor.getParent();
			}
			return null;
		}
		else { // Successor is in right subtree (find first left son).
			AVLNode nodeSuccessor = (AVLNode) node.getRight();
			while (nodeSuccessor.getLeft().isRealNode()) {
				nodeSuccessor = (AVLNode) nodeSuccessor.getLeft();
			}
			return nodeSuccessor;
		}
	}

	/**
	 * private AVLNode getPredecessor(AVLNode node)
	 * <p>
	 * Locates the predecessor of the node.
	 * Returns a pointer to the predecessor if exists,
	 * otherwise returns null.
	 * Supports delete method (when updating maxNode).
	 * Complexity: O(log n)
	 * </p>
	 */
	private AVLNode getPredecessor(AVLNode node){
		if (!node.getLeft().isRealNode()){ // Predecessor is not in node's left  subtree.
			AVLNode nodePredecessor = (AVLNode) node;
			while (node.getParent() != null){
				if (isRightSon(nodePredecessor)){
					return (AVLNode) nodePredecessor.getParent();
				}
				nodePredecessor = (AVLNode) nodePredecessor.getParent();
			}
			return null;
		}
		else { // Predecessor is in left subtree (find first right son).
			AVLNode nodePredecessor = (AVLNode) node.getLeft();
			while (nodePredecessor.getRight().isRealNode()) {
				nodePredecessor = (AVLNode) nodePredecessor.getRight();
			}
			return nodePredecessor;
		}
	}

	/**
	 * private int getNumOfSons(AVLNode node)
	 * <p>
	 * Returns the number of node's sons.
	 * Supports delete method.
	 * Complexity: O(1)
	 * </p>
	 */
	private int getNumOfSons(AVLNode node){
		int count = 0;
		if (node.getLeft().isRealNode()) count++;
		if (node.getRight().isRealNode()) count++;
		return count;
	}

	/**
	 * private void removeLeaf(AVLNode node)
	 * <p>
	 * Removes a leaf node from the tree.
	 * Complexity: O(1)
	 * </p>
	 */
	private void removeLeaf(AVLNode node){
		if (node.getKey() == root.getKey()){ // If node == root
			this.root = null;
		}
		else {
			if (isRightSon(node)) node.getParent().setRight(VIRTUAL_NODE);
			else node.getParent().setLeft(VIRTUAL_NODE);
		}
		node.setParent(null);
	}

	/**
	 * private void removeUnary(AVLNode node)
	 * <p>
	 * Removes an unary node from the tree.
	 * Complexity: O(1)
	 * </p>
	 */
	private void removeUnary(AVLNode node){
		AVLNode sonOfNode = null;
		if (node.getRight().isRealNode()) { // node is right son.
			sonOfNode = (AVLNode) node.getRight();
			node.setRight(VIRTUAL_NODE);
		}
		else { // node is left son.
			sonOfNode = (AVLNode) node.getLeft();
			node.setLeft(VIRTUAL_NODE);
		}
		sonOfNode.setParent(node.getParent());
		if (node.parent != null) { // If not root
			if (isRightSon(node)) {
				node.parent.setRight(sonOfNode);
			} else node.parent.setLeft(sonOfNode);
		}
		else {
			this.root = sonOfNode;
		}
		node.setParent(null);
	}

	/**
	 * private void removeBinaryNode(AVLNode node, AVLNode successor)
	 * <p>
	 * Removes a binary node from the tree.
	 * Complexity: O(1)
	 * </p>
	 */
	private void removeBinaryNode(AVLNode node, AVLNode successor){
		// Check successor type (unary/leaf) and remove accordingly.
		int numOfSuccessorSons = getNumOfSons(successor);
		if (numOfSuccessorSons == 0) removeLeaf(successor);
		else removeUnary(successor);

		// Set successor's field to swap with node.
		successor.setParent(node.getParent());
		successor.setRight(node.getRight());
		successor.setLeft(node.getLeft());
		successor.setHeight(node.getHeight());
		updateSize(successor);

		// Link successor to his new family
		if (node.getParent() != null){ // If not root
			if (isRightSon(node)) node.getParent().setRight(successor);
			else node.getParent().setLeft(successor);
		}
		else {
			this.root = successor;
		}
		if (node.getRight().isRealNode()) {
			node.getRight().setParent(successor);
		}
		node.getLeft().setParent(successor);

		// Unlink node from the tree.
		node.setParent(null);
		node.setRight(VIRTUAL_NODE);
		node.setLeft(VIRTUAL_NODE);
		node.setHeight(0);
	}

	/**
	 * private int deleteRebalance(AVLNode node)
	 * <p>
	 * Rebalances the tree by the AVL invariants, after a deletion.
	 * Returns a counter of the balancing operations.
	 * Complexity: O(log n)
	 * </p>
	 */
	private int deleteRebalance(AVLNode node) {
		int changeCounter = 0;
		while (node != null) { // Traversal from node to root.
			updateSize(node);
			// ---- Balances according to the node's balance factor -----
			if (!checkHeight(node) && getBalanceFactor(node) == 0) break; // if true -> tree level is balanced.
			switch (checkState(node)) {
				case 0: {
					promoteDemote(-1, node);
					//updateSize(node);
					changeCounter++;
					break;
				}
				case 1: {
					leftRotation(node);
					promoteDemote(1, node.getParent());
					node = (AVLNode) node.getParent();
					changeCounter += 3;
					break;
				}
				case 2: {
					rightRotation(node);
					promoteDemote(1, node.getParent());
					node = (AVLNode) node.getParent();
					changeCounter += 3;
					break;
				}
				case 3: {
					leftRotation(node);
					promoteDemote(-1, node);
					node = (AVLNode) node.getParent();
					changeCounter += 3;
					break;
				}
				case 4: {
					rightRotation(node);
					promoteDemote(-1, node);
					node = (AVLNode) node.getParent();
					changeCounter += 3;
					break;
				}
				case 5: {
					rightLeftRotation(node);
					promoteDemote(-1, node);
					node = (AVLNode) node.getParent();
					changeCounter += 6;
					break;
				}
				case 6: {
					leftRightRotation(node);
					promoteDemote(-1, node);
					node = (AVLNode) node.getParent();
					changeCounter += 6;
					break;
				}
			}
			if (node.getParent() == null) this.root = node; // Updates root after rebalancing
			node = (AVLNode) node.getParent();
		}
		while (node != null) { // Updates node sizes.
			updateSize(node);
			node = (AVLNode) node.getParent();
		}
		return changeCounter;
	}

	/**
	 * private int checkState(AVLNode node)
	 * <p>
	 * Determines the type of balancing operations needed (rotation/promote/demote).
	 * Returns an indicator which signals that type.
	 * Supports deleteReblance method.
	 * Complexity: O(1)
	 * </p>
	 */
	private int checkState(AVLNode node){ // Returns 7 states indicators for rebalancing.
		int myBF = getBalanceFactor(node);
		int rightBF = 0;
		int leftBF = 0;
		if (node.getRight().isRealNode()) rightBF = getBalanceFactor(node.getRight());
		if (node.getLeft().isRealNode()) leftBF = getBalanceFactor(node.getLeft());
		if (myBF == 0) return 0; // Case 1 demote
		if (myBF == -2 && rightBF == 0) return 1; // Case 2 single rotation (a)
		if (myBF == 2 && leftBF == 0) return 2; // Case 2 single rotation (a) symetry
		if (myBF == -2 && rightBF == -1) return 3; // Case 3 single rotation (b)
		if (myBF == 2 && leftBF == 1) return 4; // Case 3 single rotation (b) symetry
		if (myBF == -2 && rightBF == 1) return 5; // Case 4 double rotation
		if (myBF == 2 && leftBF == -1) return 6; // Case 4 double rotation symetry
		return -1;
	}

	// ---------- End of deletion methods -----------

	// ----------     Insert methods      -----------

	/**
	 * public int insert(int k, String i)
	 * <p>
	 * Inserts an item with key k and info i to the AVL tree.
	 * The tree must remain valid, i.e. keep its invariants.
	 * Returns the number of re-balancing operations, or 0 if no re-balancing operations were necessary.
	 * A promotion/rotation counts as one re-balance operation, double-rotation is counted as 2.
	 * Returns -1 if an item with key k already exists in the tree.
	 * Calls insertRebalance iterative method.
	 * Complexity: O(log n)
	 * </p>
	 */
	public int insert(int k, String i) {
		AVLNode newNode = new AVLNode(k, i);
		if (this.size == 0) { // First node of the tree.
			this.root = newNode;
			this.maxNode = newNode;
			this.minNode = newNode;
		}
		else {
			AVLNode y = this.VIRTUAL_NODE; // Y represents NewNode's parent.
			AVLNode x = this.root; // X represents the location for insertion of NewNode.
			while (x.isRealNode){
				if (x.getKey() == k) return -1; // New node's key exists in the tree
				y = x;
				if (x.getKey() < k){
					x = (AVLNode) x.getRight();
				}
				else {
					x = (AVLNode) x.getLeft();
				}
			}
			newNode.parent = y;
			if (y.getKey() < k) {
				y.setRight(newNode);
			}
			else {
				y.setLeft(newNode);
			}
		}
		// Increase tree size.
		this.size += 1;

		// Minimum update:
		if (this.minNode.getKey() > k){
			this.minNode = newNode;
		}
		// Maximum update:
		if (this.maxNode.getKey() < k){
			this.maxNode = newNode;
		}

		return insertRebalance(newNode);
	}

	/**
	 * private int insertRebalance(AVLNode node)
	 * <p>
	 * Calls insertRebalnce.
	 * Returns number of balance operations done.
	 * Complexity: O(log n)
	 * </p>
	 */
	private int insertRebalance(AVLNode node){
		return insertRebalance(node, false);
	}

	/**
	 * private int insertRebalance(AVLNode node, boolean joinFlag)
	 * <p>
	 * Calls insertRebalnce.
	 * Returns number of balance operations done.
	 * Complexity: O(log n)
	 * </p>
	 */
	private int insertRebalance (AVLNode node, boolean joinFlag){ // Balances the tree after insertion, returns number of operations done
		int changeCounter = 0;
		AVLNode y = (AVLNode) node.getParent();
		while (y != null){ //Traversal from node to root.
			if (!checkHeight(y)) break; // Level is balanced, therefore tree is balanced.
			else {
				int balanceFactor = getBalanceFactor(y);
				// Balance tree according to the balance factor.
				switch (balanceFactor){ // Check AVL offenders.
					case 2:
						if (getBalanceFactor(y.getLeft()) == -1) {
							leftRightRotation(y);
							changeCounter += 5;
						}
						else {
							rightRotation(y, joinFlag);
							changeCounter += 2;
						}
						y = (AVLNode) y.getParent(); // y went down one level therefore we need to get back to the relevant node.
						break;
					case -2:
						if (getBalanceFactor(y.getRight()) == 1) {
							rightLeftRotation(y);
							changeCounter += 5;
						}
						else {
							leftRotation(y, joinFlag);
							changeCounter += 2;
						}
						y = (AVLNode) y.getParent();
						break;
					case -1:
					case 1:
						promoteDemote(1, y);
						updateSize(y);
						changeCounter++;
						break;
				}
			}
			if (y.getParent() == null) this.root = y; // Update root after rebalancing.
			y = (AVLNode) y.getParent();
		}
		while (y != null){
			updateSize(y);
			y = (AVLNode) y.getParent();
		}
		return changeCounter;
	}

	/**
	 * private boolean checkHeight(AVLNode parent)
	 * <p>
	 * Checks if the parent height is updated correctly.
	 * Returns a boolean value represents whether a change is required.
	 * Complexity: O(1)
	 * </p>
	 */
	private boolean checkHeight(AVLNode parent){
		int maxSonHeight = Math.max(parent.getLeft().getHeight(), parent.getRight().getHeight());
		return parent.getHeight() != maxSonHeight + 1;
	}


	/**
	 * private int getBalanceFactor(IAVLNode node)
	 * <p>
	 * Returns the balance factor of the node. (BF = node.left.height - node.right.height)
	 * Complexity: O(1)
	 * </p>
	 */
	private int getBalanceFactor(IAVLNode node){
		if (!node.isRealNode()) return 0;
		return node.getLeft().getHeight() - node.getRight().getHeight();
	}

	/**
	 * private boolean isRightSon(IAVLNode son)
	 * <p>
	 * Checks whether the node is the right son of the node's parent.
	 * Returns a boolean value accordingly.
	 * Complexity: O(1)
	 * </p>
	 */
	private boolean isRightSon(IAVLNode son){ // Checks if the node is a right son of his parent
		return son.getParent().getRight() == son;
	}

	/**
	 * private void leftRotation(IAVLNode x)
	 * <p>
	 * Calls leftRotation(IAVLNode x, boolean joinFlag) with the relevant flag.
	 * joinFlag is used to determine the rotation process.
	 * Complexity: O(1)
	 * </p>
	 */
	private void leftRotation(IAVLNode x){
		leftRotation(x, false);
	}

	/**
	 * private void leftRotation(IAVLNode x, boolean joinFlag)
	 * <p>
	 * Performs a left rotation on x as the pivot node.
	 * x represents the pivot node.
	 * y represents x.right.
	 * Complexity: O(1)
	 * </p>
	 */
	private void leftRotation(IAVLNode x, boolean joinFlag){ // Costs 2 operations - rotate and demote.
		IAVLNode y = x.getRight();
		x.setRight(y.getLeft());

		if (y.getLeft().isRealNode()){
			y.getLeft().setParent(x);
		}
		y.setParent(x.getParent());

		if (x.getParent() == null){
			this.root = (AVLNode) y;
		}
		else if (x == x.getParent().getLeft()){
			x.getParent().setLeft(y);
		}
		else {
			x.getParent().setRight(y);
		}
		y.setLeft(x);
		x.setParent(y);
		if (joinFlag){ // Join requires a different balancing operation.
			promoteDemote(1, y);
		}
		else {
			promoteDemote(-1, x);
		}
		// Update size field
		updateSize(x);
		updateSize(y);
	}

	/**
	 * private void rightRotation(IAVLNode y)
	 * <p>
	 * Calls rightRotation(IAVLNode y, boolean joinFlag) with the relevant flag.
	 * joinFlag is used to determine the rotation process.
	 * Complexity: O(1)
	 * </p>
	 */
	private void rightRotation(IAVLNode y){
		rightRotation(y, false);
	}

	/**
	 * private void rightRotation(IAVLNode y, boolean joinFlag)
	 * <p>
	 * Performs a right rotation on x as the pivot node.
	 * y represents the pivot node.
	 * x represents y.left.
	 * Complexity: O(1)
	 * </p>
	 */
	private void rightRotation(IAVLNode y, boolean joinFlag){ // Costs 2 operations - rotate and demote.
		IAVLNode x = y.getLeft();
		y.setLeft(x.getRight());

		if (x.getRight().isRealNode()){
			x.getRight().setParent(y);
		}
		x.setParent(y.getParent());

		if (y.getParent() == null){
			this.root = (AVLNode) x;
		}
		else if (y == y.getParent().getRight()){
			y.getParent().setRight(x);
		}
		else {
			y.getParent().setLeft(x);
		}
		x.setRight(y);
		y.setParent(x);
		if (joinFlag){ // Join requires a different balancing operation.
			promoteDemote(1, x);
		}
		else {
			promoteDemote(-1, y);
		}
		// Update size field
		updateSize(y);
		updateSize(x);
	}

	/**
	 * private void leftRightRotation(IAVLNode y)
	 * <p>
	 * Performs a left rotation on y.left and then right rotation on y as the pivot node.
	 * y represents the pivot node.
	 * x represents y.left.
	 * z represents x.right.
	 * Complexity: O(1)
	 * </p>
	 */
	private void leftRightRotation(IAVLNode y){ // Costs 5 operations - 2 rotate 2 demote 1 promote.
		IAVLNode x = y.getLeft();
		IAVLNode z = x.getRight();
		leftRotation(x);
		rightRotation(y);
		promoteDemote(1, z);
	}

	/**
	 * private void rightLeftRotation(IAVLNode x)
	 * <p>
	 * Performs a right rotation on x.right and then left rotation on x as the pivot node.
	 * x represents the pivot node.
	 * y represents x.right.
	 * z represents y.left.
	 * Complexity: O(1)
	 * </p>
	 */
	private void rightLeftRotation(IAVLNode x){ // Costs 5 operations - 2 rotate 2 demote 1 promote.
		IAVLNode y = x.getRight();
		IAVLNode z = y.getLeft();
		rightRotation(y);
		leftRotation(x);
		promoteDemote(1, z);
	}

	/**
	 * private void promoteDemote(int k, IAVLNode node)
	 * <p>
	 * Changes node.height according to k.
	 * Complexity: O(1)
	 * </p>
	 */
	private void promoteDemote(int k, IAVLNode node){ // Costs 1 promote/demote operation.
		node.setHeight(node.getHeight() + k);
	}

	/**
	 * public String min()
	 * <p>
	 * Returns the info of the item with the smallest key in the tree,
	 * or null if the tree is empty.
	 * Complexity: O(1)
	 * </p>
	 */
	public String min() {
		if (this.minNode == null) return null;
		return this.minNode.getValue();
	}

	/**
	 * public String max()
	 * <p>
	 * Returns the info of the item with the largest key in the tree,
	 * or null if the tree is empty.
	 * Complexity: O(1)
	 * </p>
	 */
	public String max() {
		if (this.maxNode == null) return null;
		return this.maxNode.getValue();
	}

	/**
	 * public int[] keysToArray()
	 * <p>
	 * Returns a sorted array which contains all keys in the tree,
	 * or an empty array if the tree is empty.
	 * Complexity: O(n)
	 * </p>
	 */
	public int[] keysToArray() {
		if (this.size == 0) return new int[0];
		buildInOrderArrays();
		return this.inOrderKeyArray;
	}

	/**
	 * public String[] infoToArray()
	 * <p>
	 * Returns an array which contains all info in the tree,
	 * sorted by their respective keys,
	 * or an empty array if the tree is empty.
	 * Complexity: O(n)
	 * </p>
	 */
	public String[] infoToArray() {
		if (this.size == 0) return new String[0];
		buildInOrderArrays();
		return this.inOrderValueArray;
	}

	/**
	 * private void buildInOrderArrays()
	 * <p>
	 * Builds inOrderKeyArray and inOrderValueArray fields for keysToArray and valueToArray methods.
	 * Uses recBuildInOrderArrays method.
	 * Complexity: O(n)
	 * </p>
	 */
	private void buildInOrderArrays(){
		this.inOrderKeyArray = new int[this.size];
		this.inOrderValueArray = new String[this.size];
		IAVLNode[] nodeArray = new IAVLNode[this.size];
		int[] arrayIndex = new int[1];
		recBuildInOrderArrays(this.root, nodeArray, arrayIndex);
		insertInOrderArrays(nodeArray);
	}

	/**
	 * private void recBuildInOrderArrays(IAVLNode node, IAVLNode[] nodeArray, int[] arrayIndex)
	 * <p>
	 * Builds in order node array recursively.
	 * Complexity: O(n)
	 * </p>
	 */
	private void recBuildInOrderArrays(IAVLNode node, IAVLNode[] nodeArray, int[] arrayIndex){
		if (node.isRealNode()){
			recBuildInOrderArrays(node.getLeft(), nodeArray, arrayIndex);
			nodeArray[arrayIndex[0]] = node;
			arrayIndex[0]++;
			recBuildInOrderArrays(node.getRight(), nodeArray, arrayIndex);
		}
	}

	/**
	 * private void insertInOrderArrays(IAVLNode[] arrayNode)
	 * <p>
	 * Inserts arrayNode keys and values to the relevant tree fields.
	 * Complexity: O(n)
	 * </p>
	 */
	private void insertInOrderArrays(IAVLNode[] arrayNode){
		for (int i = 0; i < arrayNode.length; i++) {
			this.inOrderKeyArray[i] = arrayNode[i].getKey();
			this.inOrderValueArray[i] = arrayNode[i].getValue();
		}
	}

	/**
	 * public int size()
	 * <p>
	 * Returns the number of nodes in the tree.
	 * Complexity: O(1)
	 * </p>
	 */
	public int size() {
		return this.size;
	}

	/**
	 * public int getRoot()
	 * <p>
	 * Returns the root AVL node, or null if the tree is empty
	 * Complexity: O(1)
	 * </p>
	 */
	public IAVLNode getRoot() {
		return this.root;
	}

	/**
	 * public AVLTree[] split(int x)
	 * <p>
	 * splits the tree into 2 trees according to the key x.
	 * Returns an array [t1, t2] with two AVL trees. keys(t1) < x < keys(t2).
	 * <p>
	 * precondition: search(x) != null (i.e. you can also assume that the tree is not empty)
	 * postcondition: none
	 * Complexity: O(log n)
	 */
	public AVLTree[] split(int x){
		AVLTree[] splittedTree = new AVLTree[2];
		AVLNode pivot = searchNode(x);
		splittedTree[0] = buildTree((AVLNode) pivot.getLeft());
		splittedTree[1] = buildTree((AVLNode) pivot.getRight());

		while (pivot.getParent() != null){ // Traverse x -> root and join sub trees as needed.
			AVLNode joinNode = new AVLNode(pivot.getParent().getKey(), pivot.getParent().getValue());
			if (isRightSon(pivot)){
				splittedTree[0].join(joinNode, buildTree((AVLNode) pivot.getParent().getLeft()));
			}
			else {
				splittedTree[1].join(joinNode, buildTree((AVLNode) pivot.getParent().getRight()));
			}
			pivot = (AVLNode) pivot.getParent();
		}

		return splittedTree;
	}

	/**
	 * static private AVLTree buildTree(AVLNode root)
	 * <p>
	 * Build a new tree from a node which represents the root.
	 * Used for the split function.
	 * Complexity: O(log n)
	 * <p>
	 */
	static private AVLTree buildTree(AVLNode root){
		AVLTree tree = new AVLTree();
		root.setParent(null);
		tree.root = root;
		tree.size = root.size;
		if (root.isRealNode()) {
			AVLNode minCandidate = root;
			AVLNode maxCandidate = root;
			while (minCandidate.getLeft().isRealNode()) { // Update minNode in new tree.
				minCandidate = (AVLNode) minCandidate.getLeft();
			}
			tree.minNode = minCandidate;
			while (maxCandidate.getRight().isRealNode()) { // Update maxNode in new tree.
				maxCandidate = (AVLNode) maxCandidate.getRight();
			}
			tree.maxNode = maxCandidate;
		}
		else {
			tree.root = null;
			tree.maxNode = tree.minNode = null;
		}

		return tree;
	}


	/**
	 * private void updateSize(IAVLNode node)
	 * <p>
	 * Updates the node's size based on his son's size.
	 * Complexity: O(1)
	 * </p>
	 */
	private void updateSize(IAVLNode node){
		((AVLNode)node).size = ((AVLNode)node.getLeft()).size + ((AVLNode)node.getRight()).size + 1;
	}

	/**
	 * public int join(IAVLNode x, AVLTree t)
	 * <p>
	 * joins t and x with the tree.
	 * Returns the complexity of the operation (|tree.rank - t.rank| + 1).
	 * <p>
	 * precondition: keys(t) < x < keys() or keys(t) > x > keys(). t/tree might be empty (rank = -1).
	 * postcondition: none
	 * Complexity: O(log n)
	 */
	public int join(IAVLNode x, AVLTree t) {
		if (this.empty() && t.empty()) { // Both trees are empty.
			this.insert(x.getKey(), x.getValue());
			return 1;
		}
		// One of the tree is empty
		if (this.empty() && !t.empty()) {
			t.insert(x.getKey(), x.getValue());
			this.root = t.root;
			this.minNode = t.minNode;
			this.maxNode = t.maxNode;
			this.size = t.size;
			return Math.abs(-1 - t.root.height) + 1;
		}
		if (!this.empty() && t.empty()) {
			this.insert(x.getKey(), x.getValue());
			return Math.abs(this.root.getHeight() + 1) + 1;
		}

		// Both of size > 0:
		int rtrnComplexity = joinComplexity(this, t);
		AVLTree biggerKeys = this.root.getKey() > t.root.getKey() ? this : t;
		AVLTree smallerKeys = this.root.getKey() < t.root.getKey() ? this : t;

		if (biggerKeys.size >= smallerKeys.size) {
			AVLNode rightSonOfX = (AVLNode) biggerKeys.getRoot();
			int i = rightSonOfX.height;
			AVLNode parent = null;
			while (i > smallerKeys.root.height) { // Find join point.
				parent = (AVLNode) rightSonOfX;
				rightSonOfX = (AVLNode) rightSonOfX.getLeft();
				i = rightSonOfX.height;
			}
			x.setRight(rightSonOfX);
			x.setLeft(smallerKeys.root);
			if (rightSonOfX.isRealNode) { // Edge case
				x.setParent(rightSonOfX.getParent());
			}
			else {
				x.setParent(parent);
			}
			if (x.getParent() != null) x.getParent().setLeft(x); // X is not root
			rightSonOfX.setParent(x);
			smallerKeys.root.setParent(x);
		}
		else {
			AVLNode leftSonOfX = (AVLNode) smallerKeys.getRoot();
			int i = leftSonOfX.height;
			AVLNode parent = null;
			while (i > biggerKeys.root.height){ // Find join point.
				parent = (AVLNode) leftSonOfX;
				leftSonOfX = (AVLNode) leftSonOfX.getRight();
				i = leftSonOfX.height;
			}
			x.setLeft(leftSonOfX);
			x.setRight(biggerKeys.root);
			if (leftSonOfX.isRealNode) {
				x.setParent(leftSonOfX.getParent());
			}
			else {
				x.setParent(parent);
			}
			if (x.getParent() != null) x.getParent().setRight(x); // X is not root
			leftSonOfX.setParent(x);
			biggerKeys.root.setParent(x);
		}
		x.setHeight(Math.max(x.getRight().getHeight(), x.getLeft().getHeight()) + 1);
		updateSize(x);

		AVLNode newRoot = (AVLNode) x;
		this.root = this.getNewRoot(newRoot);
		insertRebalance((AVLNode) x, true); // Rebalance tree after join.
		this.size = this.root.size;
		updateMaxMin();
		return rtrnComplexity;
	}

	/**
	 * private void updateMaxMin()
	 * Update Max and Min node after join.
	 * Complexity: O(log n)
	 */
	private void updateMaxMin(){
		AVLNode minCandidate = this.root;
		AVLNode maxCandidate = this.root;
		while (minCandidate.getLeft().isRealNode()) {
			minCandidate = (AVLNode) minCandidate.getLeft();
		}
		this.minNode = minCandidate;
		while (maxCandidate.getRight().isRealNode()) {
			maxCandidate = (AVLNode) maxCandidate.getRight();
		}
		this.maxNode = maxCandidate;
	}

	/**
	 * private AVLNode getNewRoot(AVLNode x)
	 * Finds and returns the new root after join.
	 * Complexity: O(log n)
	 */
	private AVLNode getNewRoot(AVLNode x){
		while (x.getParent() != null){
			x = (AVLNode) x.getParent();
		}
		return x;
	}

	/**
	 * private int joinComplexity(AVLTree tree, AVLTree otherTree)
	 * Returns the join operation complexity.
	 */
	private int joinComplexity(AVLTree tree, AVLTree otherTree){
		return Math.abs(tree.root.getHeight() - otherTree.root.getHeight()) + 1;
	}

	/**
	 * public interface IAVLNode
	 * ! Do not delete or modify this - otherwise all tests will fail !
	 */
	public interface IAVLNode {
		public int getKey(); // Returns node's key (for virtual node return -1).

		public String getValue(); // Returns node's value [info], for virtual node returns null.

		public void setLeft(IAVLNode node); // Sets left child.

		public IAVLNode getLeft(); // Returns left child, if there is no left child returns null.

		public void setRight(IAVLNode node); // Sets right child.

		public IAVLNode getRight(); // Returns right child, if there is no right child return null.

		public void setParent(IAVLNode node); // Sets parent.

		public IAVLNode getParent(); // Returns the parent, if there is no parent return null.

		public boolean isRealNode(); // Returns True if this is a non-virtual AVL node.

		public void setHeight(int height); // Sets the height of the node.

		public int getHeight(); // Returns the height of the node (-1 for virtual nodes).
	}

	/**
	 * public class AVLNode
	 * <p>
	 * If you wish to implement classes other than AVLTree
	 * (for example AVLNode), do it in this file, not in another file.
	 * <p>
	 * This class can and MUST be modified (It must implement IAVLNode).
	 */
	public class AVLNode implements IAVLNode {
		private int key;
		private String value;
		private IAVLNode left;
		private IAVLNode right;
		private IAVLNode parent;
		private boolean isRealNode;
		private int height;
		private int size;

		// Builds a virtual Node
		public AVLNode() {
			this.key = -1;
			this.value = null;
			this.height = -1;
			this.isRealNode = false;
			this.size = 0;
		}

		// Builds a real node
		public AVLNode(int key, String value) {
			this.key = key;
			this.value = value;
			this.isRealNode = true;
			this.right = VIRTUAL_NODE;
			this.left = VIRTUAL_NODE;
			this.height = 0;
			this.size = 1;
		}

		public int getKey() {
			return this.key;
		}

		public String getValue() {
			return this.value;
		}

		public void setLeft(IAVLNode node) {
			this.left = node;
		}

		public IAVLNode getLeft() {
			return this.left;
		}

		public void setRight(IAVLNode node) {
			this.right = node;
		}

		public IAVLNode getRight() {
			return this.right;
		}

		public void setParent(IAVLNode node) {
			this.parent = node;
		}

		public IAVLNode getParent() {
			return this.parent;
		}

		public boolean isRealNode() {
			return this.isRealNode;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getHeight() {
			return height;
		}
	}
}






