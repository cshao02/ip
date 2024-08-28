import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.FormatStyle;
import java.util.Scanner;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Alex {
    private static String line = "____________________________________________________________"; //create separation line

    private static ArrayList<Task> list = new ArrayList<>(); //Create an arrayList to store all the Task objects

    private static int size = 0; //keeps track of the size of the arrayList

    private static String filePath = "./data/Alex.txt";
    public static void main(String[] args) {
        boolean sayHi = true;

        try {
            readFile();
        } catch (FileNotFoundException | AlexException | DateTimeParseException e) {
            System.out.println(e.getMessage());
            return;
        }

        while (true) {
            try {
                run(sayHi);
                break;
            } catch (AlexException e) {
                System.out.println(line + "\n" + e.getMessage() + "\n" + line);
                sayHi = false;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    //Create the tasks arrayList from the contents of the file
    private static void readFile() throws FileNotFoundException, AlexException {
        File f = new File(filePath); // create a File for the given file path
        Scanner s = new Scanner(f); // create a Scanner using the File as the source

        while (s.hasNext()) {
            Scanner lineScanner  = new Scanner(s.nextLine());
            String category = lineScanner.next();

            ArrayList<String> arrOfStr = new ArrayList<>();
            Task task = switch (category) {
                case "[T][" -> {
                    lineScanner.next();
                    yield makeTodoTask(lineScanner, arrOfStr, false);
                }
                case "[T][X]" -> makeTodoTask(lineScanner, arrOfStr, true);
                case "[D][" -> {
                    lineScanner.next();
                    yield makeDeadlineTask(lineScanner, arrOfStr, false);
                }
                case "[D][X]" -> makeDeadlineTask(lineScanner, arrOfStr, true);
                case "[E][" -> {
                    lineScanner.next();
                    yield makeEventTask(lineScanner, arrOfStr, false);
                }
                case "[E][X]" -> makeEventTask(lineScanner, arrOfStr, true);
                default -> new Task(0, "", false);
            };
            list.add(task);
            size++;
        }
    }

    private static void writeToFile() throws IOException {
        FileWriter fw = new FileWriter(filePath);
        for (int i = 0; i < size - 1; i++) {
            fw.write(list.get(i).storageString() + System.lineSeparator());
        }
        fw.write(list.get(size - 1).storageString());
        fw.close();
    }
    private static void run(boolean sayHi) throws AlexException, IOException {
        //Create a Scanner object
        Scanner inputScanner = new Scanner(System.in);

        //Greet user
        String greeting =
                """
                        ____________________________________________________________
                         Hello! I'm Alex, your personal assistant
                         What can I do for you today?
                        ____________________________________________________________""";

        if (sayHi) {
            System.out.println(greeting);
        }

        while(true) {
            //create new scanner for the line of user input
            Scanner lineScanner = new Scanner(inputScanner.nextLine());
            //handle exception for no new line found

            //Obtain the first word of user input
            String response = lineScanner.next();

            //Exit on bye
            if (response.equals("bye")) {
                break;
            } else if (response.equals("list")) {
                //list out all tasks
                System.out.println(line + "\nHere are the tasks in your list: ");
                for (int i = 1; i <= list.size(); i++) {
                    System.out.println(i + "." + list.get(i - 1));
                }
                System.out.println(line);
            } else if (response.equals("mark") || response.equals("unmark")) {  //mark and unmark tasks
                if (!lineScanner.hasNext()) {
                    throw new AlexException("Oh no! Please provide an integer number after 'mark' or 'unmark' " +
                            "indicating the task number to mark or unmark!");
                }
                String taskNumberStr = lineScanner.next();
                int taskNumber = 0;

                //handles exception where user write too much
                if (lineScanner.hasNext()) {
                    throw new AlexException("Wait! Please only provide a number after 'mark' or 'unmark'!");
                }

                //handles case where user doesn't provide a number or not an integer
                try {
                    taskNumber = Integer.valueOf(taskNumberStr);
                } catch (NumberFormatException e) {
                    throw new AlexException("Oh no! Please only provide an integer number after 'mark' or 'unmark' " +
                            "indicating the task number to mark or unmark!");
                }

                if (taskNumber < 1 || taskNumber > size) {
                    throw new AlexException("Oh no! Please provide a correct task number to mark or unmark!");
                }

                Task task = list.get(taskNumber - 1);
                if (response.equals("mark")) {
                    task.markAsDone();
                    writeToFile();
                    System.out.println(line + "\nNice! I've marked this task as done: \n" + task + "\n" + line);
                } else {
                    task.markAsUndone();
                    writeToFile();
                    System.out.println(line + "\nOK, I've marked this task as not done yet: \n" + task + "\n" + line);
                }
            } else if (response.equals("delete")) {
                if (!lineScanner.hasNext()) {
                    throw new AlexException("Oh no! Please provide an integer number after 'delete' indicating the " +
                            "task number to delete!");
                }
                String taskNumberStr = lineScanner.next();
                int taskNumber = 0;

                //handles exception where user write too much
                if (lineScanner.hasNext()) {
                    throw new AlexException("Wait! Please only provide a number after 'delete'!");
                }

                //handles case where user doesn't provide a number or not an integer
                try {
                    taskNumber = Integer.valueOf(taskNumberStr);
                } catch (NumberFormatException e) {
                    throw new AlexException("Oh no! Please only provide an integer number after 'delete' indicating " +
                            "the task number to delete!");
                }

                if (taskNumber < 1 || taskNumber > size) {
                    throw new AlexException("Oh no! Please provide a correct task number to delete!");
                }
                Task task = list.get(taskNumber - 1);
                list.remove(taskNumber - 1);
                size--;
                writeToFile();
                message(line, "Noted. I've removed this task: ", task, size);
            } else {
                ArrayList<String> arrOfStr = new ArrayList<>();
                Task task;

                if (response.equals("todo")) {
                    task = makeTodoTask(lineScanner, arrOfStr, false);
                } else if (response.equals("deadline")) {
                    try {
                        task = makeDeadlineTask(lineScanner, arrOfStr, false);
                    } catch (DateTimeParseException e) {
                        throw new AlexException("Oh no! Please provide the deadline in yyyy-mm-dd HHMM format " +
                                "e.g. 2024-05-19 1800");
                    }
                } else if (response.equals("event")) {
                    try {
                        task = makeEventTask(lineScanner, arrOfStr, false);
                    } catch (DateTimeParseException e) {
                        throw new AlexException("Oh no! Please provide the start and end date and time in yyyy-mm-dd " +
                                "HHMM format e.g. 2024-05-19 1800");
                    }

                } else {
                    throw new AlexException("Sorry! Alex doesn't understand you. Please only start with 'todo', " +
                            "'deadline', 'event', 'mark', 'unmark', 'list' or 'bye'!");
                }
                list.add(task);
                size++;
                writeToFile();
                message(line, "Got it. I've added this task: ", task, size);
            }
        }

        //Print farewell message
        String farewell =
                """
                        ____________________________________________________________
                        Bye. Hope to see you again soon!
                        ____________________________________________________________""";

        System.out.println(farewell);
    }

    private static LocalDateTime convertDateTime(String deadline) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
        return LocalDateTime.parse(deadline, formatter);
    }

    private static Task makeTodoTask(Scanner lineScanner, ArrayList<String> arrOfStr, boolean isDone)
            throws AlexException {
        while (lineScanner.hasNext()) {
            arrOfStr.add(lineScanner.next());
        }
        if (arrOfStr.isEmpty()) {
            throw new AlexException("Oh no! Alex doesn't like that the todo task is blank :( " +
                    "You have to provide a task!");
        }
        return new Todo(size + 1, String.join(" ", arrOfStr), isDone);
    }

    private static Task makeDeadlineTask(Scanner lineScanner, ArrayList<String> arrOfStr, boolean isDone)
            throws AlexException  {
        String description = "";
        String deadline = "";
        boolean hasProvidedDeadline = false;
        while (lineScanner.hasNext()) {
            String next = lineScanner.next();
            if (next.equals("/by")) {
                description = String.join(" ", arrOfStr);
                arrOfStr.clear();
                hasProvidedDeadline = true;
            } else {
                arrOfStr.add(next);
            }
        }

        deadline = String.join(" ", arrOfStr);

        if ((!hasProvidedDeadline && !deadline.isEmpty() && description.isEmpty())
                || (hasProvidedDeadline && !description.isEmpty() && deadline.isEmpty())) {
            throw new AlexException("Oh no! Alex doesn't like that no deadline date is provided :( Please provide a " +
                    "deadline date by writing '/by' followed by the deadline!");
        }
        if (description.isEmpty() ) {
            throw new AlexException("Oh no! Alex doesn't like that the deadline task is blank :( You have to provide " +
                    "a task!");
        }

//        if ((description.isEmpty() && !arrOfStr.isEmpty()) || (!description.isEmpty()) && deadline.isEmpty()) {
//            throw new AlexException("Oh no! Alex doesn't like that no deadline date is provided :( Please provide a " +
//                    "deadline date by writing '/by' followed by the deadline!");
//        }
//        if (description.isEmpty() && deadline.isEmpty()) {
//            throw new AlexException("Oh no! Alex doesn't like that the deadline task is blank :( You have to provide " +
//                    "a task!");
//        }
        return new Deadline(size + 1, description, isDone, convertDateTime(deadline));
    }

    private static Task makeEventTask(Scanner lineScanner, ArrayList<String> arrOfStr, boolean isDone)
            throws AlexException  {
        String description = "";
        String start = "";
        boolean isStart = false;
        boolean isEnd = false;

        if (!lineScanner.hasNext()) {
            throw new AlexException("Oh no! Alex doesn't like that the event task is blank :( You have to provide " +
                    "a task!");
        }

        while (lineScanner.hasNext()) {
            String next = lineScanner.next();
            if (next.equals("/from")) {
                description = String.join(" ", arrOfStr);
                arrOfStr.clear();
                if (lineScanner.hasNext()) {
                    isStart = true;
                }
                if (isEnd) {
                    throw new AlexException("Oh no! Alex doesn't like that /to comes before /from :( You should " +
                            "write the start time first before the end time");
                }
            } else if (next.equals("/to")) {
                start = String.join(" ", arrOfStr);
                arrOfStr.clear();
                if (lineScanner.hasNext()) {
                    isEnd = true;
                }
            } else {
                arrOfStr.add(next);
            }
        }
        if (!isStart || start.isEmpty()) {
            throw new AlexException("Oh no! Alex doesn't like that no start time is provided :( You have to provide a " +
                    "start time with '/from' followed by the time!");
        }
        if (!isEnd) {
            throw new AlexException("Oh no! Alex doesn't like that no end time is provided :( You have to provide " +
                    "an end time with '/to' followed by the time!");
        }
        if (description.isEmpty()) {
            throw new AlexException("Oh no! Alex doesn't like that the event task is blank :( You have to provide " +
                    "a task!");
        }
        return new Event(size + 1, description, isDone, convertDateTime(start),
                convertDateTime(String.join(" ", arrOfStr)));
    }

    private static void message(String line, String str, Task task, int size) {
        System.out.println(line);
        System.out.println(str);
        System.out.println(task);
        System.out.println("Now you have " + size + " tasks in the list");
        System.out.println(line);
    }
}
