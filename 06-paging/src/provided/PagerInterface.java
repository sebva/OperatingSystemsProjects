package provided;

public interface PagerInterface {

	/**
	 * Writes out the sent page of data into the disk. A free disk-frame is
	 * selected automatically.
	 * 
	 * @param data
	 *            the array holding the page's content to be store to disk
	 * @return the selected disk frame where the data was written
	 */
	public int pageOut(int[] data);

	/**
	 * Writes out the sent page of data into the disk frame specified.
	 * 
	 * @param data
	 *            the array holding the page's content to be store to disk
	 * @param diskFrame
	 *            the disk frame number where to write
	 * @return the selected disk frame where the data was written
	 */

	public int pageOut(int[] data, int diskFrame);

	/**
	 * Reads in the a page from the disk. After the page has been read, it is
	 * marked as free (for future calls to pageOut)
	 * 
	 * @param page
	 *            the disk page to read
	 * @return the array containing the page data loaded from disk
	 */
	public int[] pageIn(int page);
}
