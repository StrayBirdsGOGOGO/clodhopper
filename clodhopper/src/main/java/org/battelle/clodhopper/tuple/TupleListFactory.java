package org.battelle.clodhopper.tuple;

import java.util.Set;

/**
 * A <code>TupleListFactory</code> is an entity that creates and manages </code>TupleList</code>s.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public interface TupleListFactory {

	/**
	 * Create a new <code>TupleList</code> and associate it with a name. The name must
	 * be unique in this factory.
	 * 
	 * @param name the name to be associated with the tuple list.
	 * @param tupleLength the length of the tuples.
	 * @param tupleCount the number of tuples.
	 * 
	 * @return a <code>TupleList</code>
	 * 
	 * @throws TupleListFactoryException if something goes wrong such as
	 *   another <code>TupleList</code> already being associated with the name, or if an
	 *   I/O error occurs.
	 */
    TupleList createNewTupleList(String name, int tupleLength, int tupleCount) throws TupleListFactoryException;
    
    /**
     * Opens an existing tuple list associated with the specified name.
     * @param name
     * @return
     * @throws TupleListFactoryException if an error occurs, such as no such tuple list existing.
     */
    TupleList openExistingTupleList(String name) throws TupleListFactoryException;

    /**
     * Create a copy of a tuple list and associate it with a different name.
     * 
     * @param nameForCopy the name to associate with the copy.
     * @param original the original <code>TupleList</code>
     * @return a <code>TupleList</code>
	 * @throws TupleListFactoryException if something goes wrong such as
	 *   another <code>TupleList</code> already being associated with the name, or if an
	 *   I/O error occurs.
     */
    TupleList copyTupleList(String nameForCopy, TupleList original) throws TupleListFactoryException;
    
    /**
     * Returns a set containing the names of all <code>TupleList</code>s managed by this factory.
     * 
     * @return
     */
    Set<String> tupleListNames();
    
    /**
     * For checking to see whether or not a tuple list is associated with a name.
     * 
     * @param name
     * @return
     */
    boolean hasTuplesFor(String name);
    
    /**
     * Delete the specified tuple list from this factory.
     * 
     * @param tuples
     * 
     * @throws TupleListFactoryException if an error occurs, such as the tuple list
     *   not being managed by this factory.
     */
    void deleteTupleList(TupleList tuples) throws TupleListFactoryException;
    
    /**
     * Close the specified tuple list without deleting it.  It can later be reopened.
     * If the tuple list is a type that is maintained in memory, closing it with this method
     * should persist its values.
     * 
     * @param tuples
     * 
     * @throws TupleListFactoryException
     */
    void closeTupleList(TupleList tuples) throws TupleListFactoryException;

    /**
     * Closes all the open tuple lists managed by this factory.
     * 
     * @throws TupleListFactoryException
     */
    void closeAll() throws TupleListFactoryException;
    
}
