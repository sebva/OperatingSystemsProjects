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
 * Objects of class Stock represent a set of items. Items are not stored effectively,
 * only a counter is used to represent how many items are available.
 * 
 * It could be possible to use a more realistic queue (FIFO) for the Stock representation.
 * This is left as an exercise for home work. *
 */
class Stock {
	/**
	 * Amount of items
	 */
    private int nbItems;
    /**
     * Name of the stock
     */
    private String name;

    /**
     * Creates a new Stock object
     * @param name its name
     * @param nbItems initial number of items
     */
    public Stock(String name, int nbItems) {
        this.nbItems = nbItems;
        this.name = name;		
    }

    /**
     * Adds an item
     */
    public synchronized void put() {
        nbItems++;
    }

    /**
     * Removes (takes) an item
     */
    public synchronized void get() {
        nbItems--;
    }

    /**
     * Display the stock status
     */
    public void display() {
        System.out.println("The stock " + name + " contains " + nbItems + " item(s).");
    }

    /** 
     * Unit test for class Stock
     * @param args Non utilise
     */
    static public void main(String[] args) {
        Stock stock = new Stock("test", 5);
        stock.put();
        stock.display();
        stock.get();
        stock.display();
    }
}
