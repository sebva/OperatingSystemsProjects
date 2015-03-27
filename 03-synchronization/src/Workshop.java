/*
 * Operating Systems - Universite de Neuchatel
 * 
 * Exercise #3: an introduction to threads and synchronization in Java
 * 
 * Do not forget to comment your modifications in the code 
 * (e.g., what problems you fixed) and to add a reference to the question 
 * that was addressed.
 *
 */

/**
 * Objects that are instances of the Workshop class represent transformation
 * workshops. The principle is as follows: the call to transform() picks an element
 * from Stock A, waits for 100 ms, and puts an element to stock B. The work() method
 * runs nbTransform times the transform() method, nbTransform being set by the constructor.
 */
class Workshop extends Thread {

	/**
	 * The initial stock
	 */
    private Stock A;
    /**
     * The stock where to put transformed items
     */
    private Stock B;
    /** 
     * Number of transformations when calling work().
     */
    private int nbTransform;

    /**
     * Constructs an instance of Workshop
     * @param A Initial stock
     * @param B Destination stock
     * @param nbTransfo How many transforms should be made
     */
    public Workshop(Stock A, Stock B, int nbTransfo) {
        this.A = A;
        this.B = B;
        this.nbTransform = nbTransfo;
    }

    /**
     * Proceeds to a single transformation from an item of Stock A to an item of Stock B
     */
    public void transform() {
        A.get();
        //try { Thread.sleep(100); } catch(InterruptedException ignored) {}
        B.put();
    }

    /**
     * Proceeds to nbTransform transformations
     */
    public void work() {
        for(; nbTransform > 0; nbTransform--)
            transform();
    }

    @Override
    public void run() {
        work();
    }

    /**
     * Unit test for class Workshop
     * @param args not used
     */
    static public void main(String[] args) {
        Stock stockInput = new Stock("input", 4);
        Stock stockOutput = new Stock("output", 1);
        new Workshop(stockInput, stockOutput, 2).work();
        stockInput.display();
        stockOutput.display();
    }
}
