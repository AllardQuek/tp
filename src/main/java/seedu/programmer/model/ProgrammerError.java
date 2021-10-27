package seedu.programmer.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.programmer.model.student.Lab;
import seedu.programmer.model.student.Student;
import seedu.programmer.model.student.UniqueStudentList;

/**
 * Wraps all data at the programmer-book level
 * Duplicates are not allowed (by .isSameStudent comparison)
 */
public class ProgrammerError implements ReadOnlyProgrammerError {
    private final UniqueStudentList students;

    /*
     * The 'unusual' code block below is a non-static initialization block, sometimes used to avoid duplication
     * between constructors. See https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     *
     * Note that non-static init blocks are not recommended to use. There are other ways to avoid duplication
     *   among constructors.
     */
    {
        students = new UniqueStudentList();

    }

    public ProgrammerError() {}

    /**
     * Creates a ProgrammerError using the Persons in the {@code toBeCopied}
     */
    public ProgrammerError(ReadOnlyProgrammerError toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the student list with {@code students}.
     * {@code students} must not contain duplicate students.
     */
    public void setStudents(List<Student> students) {
        this.students.setStudents(students);
    }

    /**
     * Resets the existing data of this {@code ProgrammerError} with {@code newData}.
     */
    public void resetData(ReadOnlyProgrammerError newData) {
        requireNonNull(newData);
        setStudents(newData.getStudentList());
    }

    //// student-level operations

    /**
     * Returns true if a student with the same identity as {@code student} exists in ProgrammerError.
     */
    public boolean hasStudent(Student student) {
        requireNonNull(student);
        return students.contains(student);
    }

    /**
     * Adds a student to ProgrammerError.
     * The student must not already exist in ProgrammerError.
     */
    public void addStudent(Student p) {
        students.add(p);
    }

    /**
     * Replaces the given student {@code target} in the list with {@code editedStudent}.
     * {@code target} must exist in ProgrammerError.
     * The student identity of {@code editedStudent} must not be the same as another existing student
     * in ProgrammerError.
     */
    public void setStudent(Student target, Student editedStudent) {
        requireNonNull(editedStudent);
        students.setStudent(target, editedStudent);
    }

    /**
     * Removes {@code key} from this {@code ProgrammerError}.
     * {@code key} must exist in ProgrammerError.
     */
    public void removeStudent(Student key) {
        students.remove(key);
    }

    /**
     * Returns the selected student wrapped in an ObservableList.
     */
    public ObservableList<Student> getSelectedStudentWrapper() {
        return students.getSelectedStudentWrapper();
    }

    /**
     * Puts the target student in the wrapper.
     */
    public void setSelectedStudentWrapper(Student target) {
        students.setSelectedStudentWrapper(target);
    }

    /**
     * Clears the selected student in the wrapper.
     */
    public void clearSelectedStudentWrapper() {
        students.clearSelectedStudentWrapper();
    }

    /**
     * Returns the selected labs.
     */
    public ObservableList<Lab> getSelectedLabs() {
        return students.getSelectedLabs();
    }

    /**
     * Changes the selected labs.
     */
    public void setSelectedLabs(List<Lab> labs) {
        students.setSelectedLabs(labs);
    }

    /**
     * Clears the selected labs.
     */
    public void clearSelectedLabs() {
        students.clearSelectedLabs();
    }

    //// util methods

    @Override
    public String toString() {
        return students.asUnmodifiableObservableList().size() + " students";
        // TODO: refine later
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return students.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Lab> showLabResultList(Student target) {
        return target.getLabList();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ProgrammerError // instanceof handles nulls
                && students.equals(((ProgrammerError) other).students));
    }

    @Override
    public int hashCode() {
        return students.hashCode();
    }
}
