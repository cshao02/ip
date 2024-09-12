package alex;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import alex.command.AddCommand;
import alex.command.Command;
import alex.command.DeleteCommand;
import alex.command.ExitCommand;
import alex.command.FindCommand;
import alex.command.ListCommand;
import alex.command.MarkCommand;
import alex.task.Deadline;
import alex.task.Event;
import alex.task.Priority;
import alex.task.Task;
import alex.task.Todo;

/**
 * Helps to parse user inputs and convert them into appropriate commands and tasks.
 */
public class Parser {
    /**
     * Parses the user input and returns the corresponding Command object.
     * Throws an exception if the input is invalid.
     *
     * @param fullCommand User input command string.
     * @return Command object corresponding to the user input.
     * @throws AlexException If the input does not match any valid command.
     */
    public static Command parse(String fullCommand) throws AlexException {
        assert !fullCommand.isEmpty() : "Empty command";
        Scanner lineScanner = new Scanner(fullCommand);

        // Obtain the first word of user input
        String response = lineScanner.next();

        // Creates respective command classes to handle different functions based on user input
        switch (response) {
        case "bye":
            return new ExitCommand();
        case "list":
            return new ListCommand();
        case "mark", "unmark":
            return new MarkCommand(lineScanner, response);
        case "delete":
            return new DeleteCommand(lineScanner);
        case "todo", "deadline", "event":
            return new AddCommand(lineScanner, response);
        case "find":
            return new FindCommand(lineScanner);
        default:
            throw new AlexException("Sorry! Alex doesn't understand you. Please only start with 'todo', "
                    + "'deadline', 'event', 'mark', 'unmark', 'list', 'find' or 'bye'!");
        }
    }

    /**
     * Converts a date and time string into a LocalDateTime object.
     *
     * @param deadline Date and time string in the format "yyyy-MM-dd HHmm".
     * @return LocalDateTime object corresponding to the provided date and time string.
     * @throws DateTimeParseException If the date and time string is not in the correct format.
     */
    public static LocalDateTime convertDateTime(String deadline) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
        return LocalDateTime.parse(deadline, formatter);
    }

    public static Priority readTaskPriority(String word) throws AlexException {
        String priorityInput = word.substring(2);

        switch (priorityInput) {
        case "high":
            return Priority.HIGH;
        case "medium":
            return Priority.MEDIUM;
        case "low":
            return Priority.LOW;
        default:
            throw new AlexException("Hey! Please provide a valid priority! Either '//high', "
                    + "'//medium' or '//low'");
        }
    }

    /**
     * Creates a Todo task based on user description.
     *
     * @param lineScanner Scanner object based on the line of text entered by the user.
     * @param arrOfStr ArrayList of strings to store words entered by the user.
     * @param isDone Boolean value indicating whether the task is marked as done.
     * @return A Todo Task object.
     * @throws AlexException If the task description is empty.
     */
    public static Task makeTodoTask(Scanner lineScanner, ArrayList<String> arrOfStr, boolean isDone)
            throws AlexException {
        Priority priority = Priority.NONE;
        while (lineScanner.hasNext()) {
            String word = lineScanner.next();
            if (word.startsWith("//")) {
                priority = readTaskPriority(word);
            } else {
                arrOfStr.add(word);
            }
        }

        if (arrOfStr.isEmpty()) {
            throw new AlexException("Oh no! Alex doesn't like that the todo task is blank :( "
                    + "You have to provide a task!");
        }

        return new Todo(String.join(" ", arrOfStr), isDone, priority);
    }

    /**
     * Creates a Deadline task based on user description and provided deadline.
     *
     * @param lineScanner Scanner object based on the line entered by the user.
     * @param arrOfStr ArrayList of strings to store words entered by the user.
     * @param isDone Boolean value indicating whether the task is marked as done.
     * @return A Deadline Task object.
     * @throws AlexException If no description or deadline is provided, or if provided information is incorrect.
     */
    public static Task makeDeadlineTask(Scanner lineScanner, ArrayList<String> arrOfStr, boolean isDone)
            throws AlexException {
        String description = "";
        boolean hasProvidedDeadline = false;
        Priority priority = Priority.NONE;

        while (lineScanner.hasNext()) {
            String word = lineScanner.next();
            if (word.startsWith("//")) {
                priority = readTaskPriority(word);
            } else if (word.equals("/by")) {
                description = String.join(" ", arrOfStr);
                arrOfStr.clear();
                hasProvidedDeadline = true;
            } else {
                arrOfStr.add(word);
            }
        }

        String deadline = String.join(" ", arrOfStr);

        boolean hasDescriptionButNoSlashBy = !hasProvidedDeadline
                && !deadline.isEmpty() && description.isEmpty();
        boolean hasDescriptionAndSlashByButNoDeadline = hasProvidedDeadline
                && !description.isEmpty() && deadline.isEmpty();

        if ((hasDescriptionButNoSlashBy
                || hasDescriptionAndSlashByButNoDeadline)) {
            throw new AlexException("Oh no! Alex doesn't like that no deadline date is provided :( Please provide a "
                    + "deadline date by writing '/by' followed by the deadline!");
        }
        if (description.isEmpty()) {
            throw new AlexException("Oh no! Alex doesn't like that the deadline task is blank :( You have to provide "
                    + "a task!");
        }
        return new Deadline(description, isDone, priority, convertDateTime(deadline));
    }

    /**
     * Creates an Event task based on user description, start time, and end time.
     *
     * @param lineScanner Scanner object based on the line entered by the user.
     * @param arrOfStr ArrayList of strings to store words entered by the user.
     * @param isDone Boolean value indicating whether the task is completed.
     * @return An Event Task object.
     * @throws AlexException If no description, start time, or end time is provided, or end time is before start time.
     */
    public static Task makeEventTask(Scanner lineScanner, ArrayList<String> arrOfStr, boolean isDone)
            throws AlexException {
        if (!lineScanner.hasNext()) {
            throw new AlexException("Oh no! Alex doesn't like that the event task is blank :( You have to provide "
                    + "a task!");
        }

        String description = "";
        String start = "";
        boolean isStart = false;
        boolean isEnd = false;
        Priority priority = Priority.NONE;

        //creates description, start and end strings based on user input to create event task
        while (lineScanner.hasNext()) {
            String word = lineScanner.next();
            if (word.startsWith("//")) {
                priority = readTaskPriority(word);
            } else if (word.equals("/from")) {
                description = String.join(" ", arrOfStr);
                arrOfStr.clear();
                if (lineScanner.hasNext()) {
                    isStart = true;
                }
                if (isEnd) {
                    throw new AlexException("Oh no! Alex doesn't like that /to comes before /from :( You should "
                            + "write the start time first before the end time");
                }
            } else if (word.equals("/to")) {
                start = String.join(" ", arrOfStr);
                arrOfStr.clear();
                if (lineScanner.hasNext()) {
                    isEnd = true;
                }
            } else {
                arrOfStr.add(word);
            }
        }
        if (!isStart || start.isEmpty()) {
            throw new AlexException("Oh no! Alex doesn't like that no start time is provided :( You have to "
                    + "provide a start time with '/from' followed by the time!");
        }
        if (!isEnd) {
            throw new AlexException("Oh no! Alex doesn't like that no end time is provided :( You have to provide "
                    + "an end time with '/to' followed by the time!");
        }
        if (description.isEmpty()) {
            throw new AlexException("Oh no! Alex doesn't like that the event task is blank :( You have to provide "
                    + "a task!");
        }
        return new Event(description, isDone, priority, convertDateTime(start),
                convertDateTime(String.join(" ", arrOfStr)));
    }
}

