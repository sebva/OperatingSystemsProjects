/*
 * Operating Systems - Universite de Neuchatel
 * 
 * Exercise #2: an introduction to threads and synchronization in Java
 * 
 * Do not forget to indicate with comments inside the code the 
 * modifications you have made and what problems they fix or 
 * prevent, with references to the questions of the subject (Q1, Q2, etc.)
 */

/**
 * Objects instances of Factory represent a factory with initially two workshops and 
 * two stocks: initial stock of 10 items and empty final stock. Workshops are in charge
 * of transforming from the former to the latter.
 */
class Factory {
	/**
	 * Stock of items to transform
	 */
    Stock stockInput = new Stock("input", 10);
    /**
     * Stock of final (transformed) items
     */
    Stock stockOutput = new Stock("output", 0);
    /**
     * Workshops for the transformations
     */
    Workshop workshop1 = new Workshop(stockInput, stockOutput, 5);
    Workshop workshop2 = new Workshop(stockInput, stockOutput, 5);
    
    /**
     * Main entry point: proceed to operate the factory work of transformation
     */
    public void work() {
    	System.out.println("Starting factory work ...");
    	long initialTime = System.currentTimeMillis();
   		workshop1.work();
   		workshop2.work();
   		stockInput.display();
   		stockOutput.display();
   		System.out.println("... done ("+((double)(System.currentTimeMillis() - initialTime)/1000)+" second(s))");
    }
    
    /**
     * Entry point for the whole program
     * @param args not used
     */
    public static void main(String[] args) {
    	new Factory().work();
    }
}
