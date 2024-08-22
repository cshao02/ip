import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

public class Alex {

    public static void main(String[] args) {
        //Create a Scanner object
        Scanner inputScanner = new Scanner(System.in);

        //Create the separation line variable
        String line = "____________________________________________________________";

        //Create an arrayList to store all the Task objects
        ArrayList<Task> list = new ArrayList<>();

        //keeps track of the size of the arrayList
        int size = 0;

        //Greet user
        String greeting =
                """
                        ____________________________________________________________
                         Hello! I'm Alex
                         What can I do for you?
                        ____________________________________________________________""";

        System.out.println(greeting);


        while(true) {
            //create new scanner for the line of user input
            Scanner lineScanner = new Scanner(inputScanner.nextLine());

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
            } else if (response.equals("mark") || response.equals("unmark")) {
                //mark and unmark tasks
                String taskNumberStr = lineScanner.next();
                int taskNumber = Integer.valueOf(taskNumberStr);
                Task task = list.get(taskNumber - 1);
                if (response.equals("mark")) {
                    task.markAsDone();
                    System.out.println(line + "\nNice! I've marked this task as done: \n" + task + "\n" + line);
                } else {
                    task.markAsUndone();
                    System.out.println(line + "\nOK, I've marked this task as not done yet: \n" + task + "\n" + line);
                }
                //room for error handling
            } else {
                ArrayList<String> arrOfStr = new ArrayList<>();
                Task task = new Task(0, "", false);

                if (response.equals("todo")) {
                    while (lineScanner.hasNext()) {
                        arrOfStr.add(lineScanner.next());
                    }
                    task = new Todo(size + 1, String.join(" ", arrOfStr), false);
                } else if (response.equals("deadline")) {
                    String description = "";
                    while (lineScanner.hasNext()) {
                        String next = lineScanner.next();
                        if (next.equals("/by")) {
                            description = String.join(" ", arrOfStr);
                            arrOfStr.clear();
                        } else {
                            arrOfStr.add(next);
                        }
                    }
                    task = new Deadline(size + 1, description, false, String.join(" ", arrOfStr));
                } else if (response.equals("event")) {
                    String description = "";
                    String start = "";
                    //boolean startTime = true;

                    while (lineScanner.hasNext()) {
                        String next = lineScanner.next();
                        if (next.equals("/from")) {
                            description = String.join(" ", arrOfStr);
                            arrOfStr.clear();
                        } else if (next.equals("/to")) {
                            start = String.join(" ", arrOfStr);
                            arrOfStr.clear();
                        } else {
                            arrOfStr.add(next);
                        }
                    }
                    task = new Event(size + 1, description, false, start, String.join(" ", arrOfStr));
                }
                list.add(task);
                size++;
                message(line, task, size);
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

    private static void message(String line, Task task, int size) {
        System.out.println(line);
        System.out.println("Got it. I've added this task: ");
        System.out.println(task);
        System.out.println("Now you have " + size + " tasks in the list");
        System.out.println(line);
    }
}
