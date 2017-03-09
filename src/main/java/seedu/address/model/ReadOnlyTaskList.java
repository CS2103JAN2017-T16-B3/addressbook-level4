package seedu.address.model;


import javafx.collections.ObservableList;
import seedu.address.model.tag.Tag;
import seedu.address.model.task.ReadOnlyFloatingTask;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyTaskList {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<ReadOnlyFloatingTask> getTaskList();

    /**
     * Returns an unmodifiable view of the tags list.
     * This list will not contain any duplicate tags.
     */
    ObservableList<Tag> getTagList();

}